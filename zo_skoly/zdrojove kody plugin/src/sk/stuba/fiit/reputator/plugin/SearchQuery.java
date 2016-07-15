package sk.stuba.fiit.reputator.plugin;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;

import sk.stuba.fiit.reputator.plugin.core.DBConnectionPool;
import sk.stuba.fiit.reputator.plugin.core.HttpRequester;
import sk.stuba.fiit.reputator.plugin.core.JsonConverter;
import sk.stuba.fiit.reputator.plugin.model.FileInfoBean;
import sk.stuba.fiit.reputator.plugin.model.Reputation;
import sk.stuba.fiit.reputator.plugin.model.jsonpojo.Hit;
import sk.stuba.fiit.reputator.plugin.model.jsonpojo.Match;
import sk.stuba.fiit.reputator.plugin.model.jsonpojo.Repo;

public class SearchQuery implements ISearchQuery {
	  
	private final String query; 
	private final String filePath;
	private final boolean ignoreCase;
	private final SearchResult fSearchResult; 
	private final static String SQL_SELECT = "SELECT developers.name, developers.reputation, files_commits.change_count FROM files, files_commits \n" +
            "LEFT JOIN commits\n" +
            "ON files_commits.commit_id = commits.id\n" +
            "LEFT JOIN developers\n" +
            "ON commits.developer_id = developers.id\n" +
            "where files.name like 'FILENAME'\n" +
            "AND files_commits.file_id = files.id\n";
	
	private final static String SQL_CHANGES_INFO = "SELECT * FROM gerrit_change where author = (SELECT id from developers WHERE name='FOO')";
	private Connection connection = null;
	
	public SearchQuery(String filter, String filePath, boolean ignoreCase) {  
		this.query = filter;    
		this.filePath = filePath;
		this.ignoreCase = ignoreCase;
		try {
			this.connection = DBConnectionPool.getInstance().getConnection();
		} catch (SQLException e) {
			System.err.println("Error creating DB connection");
            System.err.println(e);
		} catch(IOException e) {
			System.err.println("Error creating DB connection");
            System.err.println(e);
		}
		this.fSearchResult = new SearchResult(this); 
	}

	@Override
	public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
		 
		 	HttpRequester requester = new HttpRequester();
	        JsonConverter jsonConverter = new JsonConverter();
	        String completeJSON = requester.makeHoundRequest(query, filePath, ignoreCase);

	        // Combine authors and hound results
	        List<Repo> repos = jsonConverter.convertJsonStringToPojo(Repo.class, completeJSON);
	        for(Repo repo : repos) {
	            for(Hit hit : repo.getHits()) {
	                String[] str = hit.getFilename().split("/");
	                Map<String, Integer> authors = new HashMap<>();
	                Map<String, Reputation> reps = new HashMap<>();
	                
	                
	                // Call sql query and save results about author and # of changes he's made to a file 
	                try {
	                    String selectTableSQL = SQL_SELECT.replace("FILENAME", hit.getFilename());
	                    Statement statement = connection.createStatement();
	                    ResultSet rs = statement.executeQuery(selectTableSQL);
	                    
	                    while (rs.next()) {
	                        String username = rs.getString("name");
	                        Double rep = rs.getDouble("reputation");
	                        int changeCount = rs.getInt("change_count") ==  0 ? 1 : rs.getInt("change_count");
	                        
	                        if(authors.get(username) == null) {
	                            authors.putIfAbsent(username, changeCount);
	                        } else {
	                            authors.put(username, authors.get(username) + changeCount);
	                        }
	                        
	                        if(!reps.containsKey(username)){
		                        //Count success rate and decline ratio of author's changes
		                        selectTableSQL = SQL_CHANGES_INFO.replace("FOO", username);
			                    Statement statementChanges = connection.createStatement();
			                    ResultSet rsChanges = statementChanges.executeQuery(selectTableSQL);
			                    int patches = 0, changes = 0, declined = 0;
			                    
			                    while (rsChanges.next()) {
			                    	changes++;
			                    	patches += rsChanges.getInt("patches_count");
			                    	declined += rsChanges.getBoolean("merged") ? 0 : 1;
			                    }
			                    
			                    patches = patches < changes ? changes : patches;
			                    
			                    Reputation r = new Reputation();
			                    r.setReviewer(round(rep, 3));
			                    double patchesReputation = changes != 0 && patches != 0 
			                    		? round((double) changes/(double) patches, 3)
			                    		: 0;
			                    System.out.println("changes "+changes+" "+ "patches "+ patches);
			                    System.out.println("patchesReputation "+patchesReputation);
			                    r.setPatches(patchesReputation);
			                    double declinedReputation = 0;
			                    if(declined == 0 ) {
			                    	declinedReputation = 1;
			                    } else if( declined != 0 && changes != 0) {
			                    	declinedReputation = round((double) 1 - (double) declined/(double) changes, 3);
			                    }
			                    r.setDeclined(declinedReputation);
			                    
			                    reps.put(username, r);
	                        }
	                        
	                    }
	                } catch (SQLException e) {
	                	System.err.println("SQL exception reading result set");
	                	System.err.println(e);
	                	return Status.CANCEL_STATUS;
	                } 
	                
	                // Create string shown in results
	                StringBuilder sb = new StringBuilder(str[str.length - 1]+ " from repository "+str[0]+"\n");
	                for(Map.Entry<String, Integer> entry : authors.entrySet()) {
	                    sb.append(entry.getKey() +": "+entry.getValue()+  " changes made to this file "
	                    +" reputation: (CRB) "
	                +reps.get(entry.getKey()).getReviewer()+"\n(PCB) " +reps.get(entry.getKey()).getPatches()+
	                "\n(DCB) " + (reps.get(entry.getKey()).getDeclined()) + "\n");
	                }
	               
	                FileInfoBean fib = new FileInfoBean(hit.getFilename(), sb.toString());
	                
	                for(Match match : hit.getMatches()) {
	                	fib.getLines().add("Line number: " + match.getLineNumber());
	                }
	                
	                fSearchResult.addFileToTreeResult(fib);
	            }
	        }
	        if(this.connection != null) {
        		try {
					this.connection.close();
				} catch (SQLException e) {
					System.err.println("SQL exception closing connection");
                	System.err.println(e);
                	return Status.CANCEL_STATUS;
				}	
        	}
		 
		 

	        return Status.OK_STATUS;
	}

	@Override
	public String getLabel() {
		return "Reputator search";
	}

	@Override
	public boolean canRerun() {
		return true;
	}

	@Override
	public boolean canRunInBackground() {
		return true;
	}

	@Override
	public ISearchResult getSearchResult() {
		return fSearchResult;
	}
	
	public double round(double d, int decimalPlace){
	    BigDecimal bd = new BigDecimal(Double.toString(d));
	    bd = bd.setScale(decimalPlace,BigDecimal.ROUND_HALF_UP);
	    return bd.doubleValue();
	  }

}

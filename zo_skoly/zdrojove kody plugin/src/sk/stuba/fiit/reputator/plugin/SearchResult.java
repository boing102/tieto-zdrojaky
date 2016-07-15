package sk.stuba.fiit.reputator.plugin;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultListener;

import sk.stuba.fiit.reputator.plugin.model.FileInfoBean;

import java.util.Collection;
import java.util.LinkedHashSet;

public class SearchResult implements ISearchResult {
	
	 private final ISearchQuery fQuery;
	 private final ListenerList fListeners = new ListenerList();

	 private final Collection<FileInfoBean> treeResult = new LinkedHashSet<>();

	 public SearchResult(ISearchQuery query) {
		 fQuery = query;
	 }

	@Override
	public void addListener(ISearchResultListener l) {
		fListeners.add(l); 
	}

	@Override
	public void removeListener(ISearchResultListener l) {
		fListeners.remove(l); 
	}

	@Override
	public String getLabel() {
		return treeResult.size() + " file(s) found";
	}

	@Override
	public String getTooltip() {
		return "Found files";
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public ISearchQuery getQuery() {
		return fQuery; 
	}
	
	private void notifyListeners(FileInfoBean fib) {  
		MySearchResultEvent event = new MySearchResultEvent(this, fib);  
		
		for (Object listener : fListeners.getListeners()) {  
			((ISearchResultListener) listener).searchResultChanged(event);  
		}
	}
		
	public void addFileToTreeResult(FileInfoBean fib) {
		treeResult.add(fib);
		notifyListeners(fib);
	}
}

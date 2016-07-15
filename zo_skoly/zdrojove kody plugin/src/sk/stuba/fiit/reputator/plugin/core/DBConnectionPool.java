package sk.stuba.fiit.reputator.plugin.core;


import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;

public class DBConnectionPool {
   private static DBConnectionPool connectionPool;
   private BasicDataSource bds;
   
   private DBConnectionPool() throws SQLException {
	   bds = new BasicDataSource();
	   bds.setDriverClassName("org.postgresql.Driver");
	   bds.setUsername("wv011400");
	   bds.setPassword("bzodybog");
	   bds.setUrl("jdbc:postgresql://46.229.230.245:5432/wv011401db");
	   
	   bds.setMinIdle(5);
	   bds.setMaxIdle(20);
	   bds.setMaxActive(75);

   }

   public static DBConnectionPool getInstance() throws IOException,SQLException {
       if (connectionPool == null) {
    	   connectionPool = new DBConnectionPool();
           return connectionPool;
       } else {
           return connectionPool;
       }
   }

   public Connection getConnection() throws IOException, SQLException {
       return this.bds.getConnection();
   }
}

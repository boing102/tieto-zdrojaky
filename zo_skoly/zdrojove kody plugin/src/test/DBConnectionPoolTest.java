package test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import sk.stuba.fiit.reputator.plugin.core.DBConnectionPool;

public class DBConnectionPoolTest {
	private Connection connection;
	private List<Connection> connections;
	private static final int MAX_CONNECTIONS = 100;
	
	@Test
	public void testConnection() {
		try {
			connection = DBConnectionPool.getInstance().getConnection();
			assertTrue(connection.isValid(2));
			connection.close();
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Before
	public void setUp() {
		connections = new ArrayList<Connection>(MAX_CONNECTIONS);
	}
	
	@Test
	public void testConnections() {
		for(int i = 0; i < MAX_CONNECTIONS; i++) {
			try {
				connections.add(i, DBConnectionPool.getInstance().getConnection());
				assertTrue(connections.get(i).isValid(2));
				connections.get(i).close();
			} catch (IOException | SQLException e) {
				e.printStackTrace();
			}
		}
	}

}

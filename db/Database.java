package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/**
 * Database connection handling
 * 
 * @author Virginie Collombon, David San
 */
public class Database {
	public static final String DEFAULT_DB_FILE = "default.db";
	public static final int BATCH_SAFE_LIMIT = 1000;
	
	public static Connection connect(String filename) throws SQLException,
			ClassNotFoundException {
		Class.forName("org.sqlite.JDBC");
		return DriverManager.getConnection("jdbc:sqlite:" + filename);
	}

	public static Connection connect() throws SQLException,
			ClassNotFoundException {
		return connect(DEFAULT_DB_FILE);
	}

	public static Connection connectRAM() throws SQLException,
			ClassNotFoundException {
		return connect(":memory:");
	}

}
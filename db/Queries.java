package db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Queries accessor using properties
 * 
 * @author Virginie Collombon
 * @author David San
 */
public class Queries {
	private static final String filename = "db/queries.properties";
	private static Properties properties;

	public static Properties getQueries() throws SQLException {
		InputStream is = Queries.class.getResourceAsStream("/" + filename);
		if (is == null) {
			throw new SQLException("Unable to load property file: " + filename);
		}
		if (properties == null) {
			properties = new Properties();
			try {
				properties.load(is);
			} catch (IOException e) {
				throw new SQLException("Unable to load property file: "
						+ filename + "\n" + e.getMessage());
			}
		}
		return properties;
	}

	public static String getQuery(String query) throws SQLException {
		return getQueries().getProperty(query);
	}

}

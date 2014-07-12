package report;

import java.sql.Connection;
import java.sql.SQLException;

import core.Settings;
import core.SimScenario;
import db.Database;

/**
 * Abstract superclass for all reports using a database. All settings defined in
 * this class can be used for all ReportDB classes.
 * 
 * @author Virginie Collombon, David San
 */
public abstract class ReportDB extends Report {

	private static final String DATABASEDIR = "database";
	public static final String OUT_SUFFIX = ".db";
	private String outFileName;
	private String scenarioName;

	protected Connection connection;

	/**
	 * Constructor.
	 */
	public ReportDB() {
		Settings settings = new Settings();
		scenarioName = settings
				.valueFillString(settings.getSetting(SimScenario.SCENARIO_NS
						+ "." + SimScenario.NAME_S));

		settings = getSettings();

		if (settings.contains(OUTPUT_SETTING)) {
			outFileName = settings.getSetting(OUTPUT_SETTING);
			// fill value place holders in the name
			outFileName = settings.valueFillString(outFileName);
		} else {
			// no output name define -> construct one from report class' name
			settings.setNameSpace(null);
			String outDir = settings.getSetting(REPORTDIR_SETTING);
			if (!outDir.endsWith("/")) {
				outDir += "/"; // make sure dir ends with directory delimiter
			}
			outDir += DATABASEDIR + "/";
			outFileName = outDir + scenarioName + "_"
					+ this.getClass().getSimpleName();
			outFileName += OUT_SUFFIX;
		}

		super.checkDirExistence(outFileName);
		init();
	}

	/**
	 * Initializes the database connection. Method is called in the beginning of
	 * every new database interaction.
	 */
	@Override
	protected void init() {
		try {
			connection = Database.connect(outFileName);
			connection.setAutoCommit(false); // begin transaction
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		initTable();
	}

	/**
	 * Initializes the database table(s).
	 */
	abstract void initTable();

	/**
	 * This method doesn't do anything because multiple databases (splitted by
	 * interval) is not supported
	 */
	@Override
	protected void newEvent() {
	}

	/**
	 * Close the database connection.
	 */
	@Override
	public void done() {
		try {
			connection.commit();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}

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
public abstract class ReportSHM extends Report {

	private static final String DATABASEDIR = "database";
	public static final String OUT_SUFFIX = ".db";
	protected String filename;
	protected String outFileName;
	private String scenarioName;

	protected Connection connection;

	/**
	 * Constructor.
	 */
	public ReportSHM() {
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
			filename = scenarioName + "_" + this.getClass().getSimpleName();
			outFileName = outDir + filename;
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
			connection = Database.connect("/dev/shm/" + filename);
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
			// move from /dev/shm to disk
			// File oldFile = new File("/dev/shm" + filename);
			// if (!oldFile.renameTo(new File(outFileName))) {
			// System.err.println("move from /dev/shm to disk failed");
			// }

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}

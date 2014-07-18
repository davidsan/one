package report;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;

import core.Settings;
import core.SimError;
import core.SimScenario;

/**
 * Abstract superclass for all reports using a CSV file. All settings defined in
 * this class can be used for all ReportCSV classes.
 * 
 * @author Virginie Collombon, David San
 */
public abstract class ReportCSV extends Report {

	private static final String CSVDIR = "csv";
	public static final String OUT_SUFFIX = ".csv";
	private String outFileName;
	private String scenarioName;

	protected Connection connection;
	protected BufferedWriter out;

	/**
	 * Constructor.
	 */
	public ReportCSV() {
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
			outDir += CSVDIR + "/";
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
			this.out = new BufferedWriter(new FileWriter(outFileName));
		} catch (IOException e) {
			throw new SimError("Couldn't open file '" + outFileName
					+ "' for report output\n" + e.getMessage(), e);
		}
		initCSVHeader();
	}

	/**
	 * Initializes the CSV header
	 */
	abstract void initCSVHeader();

	/**
	 * This method doesn't do anything because multiple csv file (splitted by
	 * interval) is not supported
	 */
	@Override
	protected void newEvent() {
	}

	/**
	 * Close the buffered writer
	 */
	@Override
	public void done() {
		super.done();
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}

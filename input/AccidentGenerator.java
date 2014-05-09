package input;

import java.util.Random;

import core.Settings;
import core.SimClock;

/**
 * Accident events generator.
 * 
 * @author Virginie Collombon, David San
 */
public class AccidentGenerator implements EventQueue {
	public static final String ACCIDENT_INTERVAL_S = "interval";
	public static final String ACCIDENT_NROF_S = "nrofAccidents";
	public static final String ACCIDENT_DELAY_S = "delay";
	
	private double nextEventsTime = 0;
	/** Interval between accidents (min, max) */
	private int[] accidentInterval;
	/** Random number generator for this Class */
	protected Random rng;
	private int nrofAccidents;
	private int count;

	public AccidentGenerator(Settings s) {
		// accidentProb = s.getDouble(PROBABILITY_OF_ACCIDENT);
		this.rng = new Random(SimClock.getIntTime());
		this.accidentInterval = s.getCsvInts(ACCIDENT_INTERVAL_S);
		this.nrofAccidents = s.getInt(ACCIDENT_NROF_S);
		this.nextEventsTime = s.getInt(ACCIDENT_DELAY_S);
		this.count = 0;
		if (this.accidentInterval.length == 1) {
			this.accidentInterval = new int[] { this.accidentInterval[0],
					this.accidentInterval[0] };
		} else {
			s.assertValidRange(this.accidentInterval, ACCIDENT_INTERVAL_S);
		}
		this.nextEventsTime += accidentInterval[0]
				+ (accidentInterval[0] == accidentInterval[1] ? 0 : rng
						.nextInt(accidentInterval[1] - accidentInterval[0]));
	}

	/**
	 * Generates a (random) time difference between two events
	 * 
	 * @return the time difference
	 */
	protected int drawNextEventTimeDiff() {
		int timeDiff = accidentInterval[0] == accidentInterval[1] ? 0 : rng
				.nextInt(accidentInterval[1] - accidentInterval[0]);
		return accidentInterval[0] + timeDiff;
	}

	@Override
	public ExternalEvent nextEvent() {
		int interval = drawNextEventTimeDiff();
		AccidentEvent ae = new AccidentEvent(this.nextEventsTime);
		this.nextEventsTime += interval;
		count++;
		if (count > nrofAccidents) {
			this.nextEventsTime += Double.MAX_VALUE;
		}
		return ae;
	}

	@Override
	public double nextEventsTime() {
		return this.nextEventsTime;
	}

}
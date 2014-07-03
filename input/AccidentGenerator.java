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
	public static final String ACCIDENT_PROB_S = "accidentProb";
	public static final String ACCIDENT_NROF_S = "nrofAccidents";
	public static final String ACCIDENT_DELAY_S = "delay";

	private double nextEventsTime = 0;
	protected Random rng;
	private double accidentProb;
	private int nrofAccidents;
	private int count;

	public AccidentGenerator(Settings s) {
		this.rng = new Random(SimClock.getIntTime());
		this.accidentProb = s.getDouble(ACCIDENT_PROB_S);
		this.nrofAccidents = s.getInt(ACCIDENT_NROF_S);
		this.nextEventsTime = s.getInt(ACCIDENT_DELAY_S);
		this.count = 0;

		this.nextEventsTime = 0;

		if (count >= nrofAccidents) {
			this.nextEventsTime += Double.MAX_VALUE;
		}
	}

	@Override
	public ExternalEvent nextEvent() {
		ExternalEvent e;

		if (rng.nextDouble() < accidentProb) {
			e = new AccidentEvent(this.nextEventsTime);
			count++;
		} else {
			e = new EmptyEvent(this.nextEventsTime);
		}

		if (count >= nrofAccidents) {
			this.nextEventsTime += Double.MAX_VALUE;
		}
		return e;
	}

	@Override
	public double nextEventsTime() {
		return this.nextEventsTime;
	}

}
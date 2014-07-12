package input;

import java.util.Random;

import core.Settings;

/**
 * Accident events generator.
 * 
 * @author Virginie Collombon, David San
 */
public class AccidentGenerator implements EventQueue {
	public static final String ACCIDENT_PROB_S = "accidentProb";
	public static final String ACCIDENT_NROF_S = "nrofAccidents";
	public static final String ACCIDENT_DELAY_S = "delay";
	public static final String ACCIDENT_SEED_S = "seed";

	private double nextEventsTime;
	private double accidentProb;
	private int nrofAccidents;
	private int count;
	private int seed;
	private static Random rng;

	public AccidentGenerator(Settings s) {
		this.accidentProb = s.getDouble(ACCIDENT_PROB_S);
		this.nrofAccidents = s.getInt(ACCIDENT_NROF_S);
		this.nextEventsTime = s.getInt(ACCIDENT_DELAY_S);
		this.count = 0;
		if (s.contains(ACCIDENT_SEED_S)) {
			this.seed = s.getInt(ACCIDENT_SEED_S);
		} else {
			this.seed = 0;
		}
		rng = new Random(this.seed);

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
	
	public static Random getRng() {
		return rng;
	}

}
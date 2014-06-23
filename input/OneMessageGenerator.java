package input;

import java.util.ArrayList;
import java.util.List;

import core.Settings;
import core.SettingsError;

/**
 * Simple message creation events generator. Creates one message for every nodes
 * of the simulation with from = to.
 * 
 * @author Virginie Collombon, David San
 */
public class OneMessageGenerator extends MessageEventGenerator {

	public static final String PROBABILITY_TO_BE_PREWARNED_S = "prewarnedProb";

	private List<Integer> candidates;

	public OneMessageGenerator(Settings s) {
		super(s);
		if (toHostRange == null) {
			throw new SettingsError("Destination host (" + TO_HOST_RANGE_S
					+ ") must be defined");
		}
		this.candidates = new ArrayList<Integer>();
		for (int i = hostRange[0]; i <= hostRange[1]; i++) {
			this.candidates.add(i);
		}
	}

	/**
	 * Returns the next message creation event
	 * 
	 * @see input.EventQueue#nextEvent()
	 */
	public ExternalEvent nextEvent() {
		int responseSize = 0; /* no responses requested */
		int from, to;
		int candidate;
		/* compute from and to */
		if (candidates.isEmpty()) {
			this.nextEventsTime = Double.MAX_VALUE; /* no messages left */
			return new ExternalEvent(Double.MAX_VALUE);
		}
		candidate = rng.nextInt(candidates.size());
		from = candidates.remove(candidate);
		to = from;
		MessageCreateEvent mce = new MessageCreateEvent(from, to, getID(),
				drawMessageSize(), responseSize, this.nextEventsTime);

		return mce;
	}

}
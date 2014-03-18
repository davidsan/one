package input;

import java.util.ArrayList;
import java.util.List;

import core.Settings;
import core.SettingsError;

/**
 * Danger message creation events generator. Creates danger message for every
 * nodes of the simulation.
 * 
 * @author Virginie Collombon, David San
 */
public class DangerMessageGenerator extends MessageEventGenerator {

	public static final String PROBABILITY_TO_BE_PREWARNED_S = "prewarnedProb";

	private double prewarnedProb;
	private int countPrewarned;
	private int maxPrewarned;
	private List<Integer> prewarnedCandidates;

	public DangerMessageGenerator(Settings s) {
		super(s);
		this.prewarnedProb = s.getDouble(PROBABILITY_TO_BE_PREWARNED_S);
		if (toHostRange == null) {
			throw new SettingsError("Destination host (" + TO_HOST_RANGE_S
					+ ") must be defined");
		}
		this.maxPrewarned = (int) (prewarnedProb * (hostRange[1] - hostRange[0] + 1));
		this.countPrewarned = 0;
		this.prewarnedCandidates = new ArrayList<Integer>();
		for (int i = hostRange[0]; i <= hostRange[1]; i++) {
			this.prewarnedCandidates.add(i);
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
		if (prewarnedCandidates.isEmpty() || countPrewarned >= maxPrewarned) {
			this.nextEventsTime = Double.MAX_VALUE; /* no messages left */
			return new ExternalEvent(Double.MAX_VALUE);
		}
		candidate = rng.nextInt(prewarnedCandidates.size());
		from = prewarnedCandidates.remove(candidate);
		to = from;
		countPrewarned++;
		MessageCreateEvent mce = new MessageCreateEvent(from, to, getID(),
				drawMessageSize(), responseSize, this.nextEventsTime);

		return mce;
	}

}
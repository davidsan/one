package input;

import core.Settings;
import core.SettingsError;

/**
 * Danger message creation events generator. Creates danger message for every
 * nodes of the simulation.
 * 
 * @author Virginie Collombon, David San
 */
public class DangerMessageGenerator extends MessageEventGenerator {

	private int currentFrom;
	private int currentTo;

	public DangerMessageGenerator(Settings s) {
		super(s);
		if (toHostRange==null) {
			throw new SettingsError("Destination host ("+TO_HOST_RANGE_S
			        +") must be defined");
		}
		this.currentFrom = hostRange[0];
		this.currentTo = toHostRange[0];
	}

	/**
	 * Returns the next message creation event
	 * 
	 * @see input.EventQueue#nextEvent()
	 */
	public ExternalEvent nextEvent() {
		int responseSize = 0; /* no responses requested */

		/* compute from and to */
		if (currentFrom==hostRange[1]&&currentTo==toHostRange[1]) {
			this.nextEventsTime = Double.MAX_VALUE; /* no messages left */
			return new ExternalEvent(Double.MAX_VALUE);
		}

		currentTo++;
		currentFrom++;

		// System.err.println("Danger :"+currentFrom+" -> "+currentTo);

		MessageCreateEvent mce = new MessageCreateEvent(currentFrom, currentTo,
		        "DANGER", drawMessageSize(), responseSize, this.nextEventsTime);

		return mce;
	}

}
package movement;

import core.Settings;

/**
 * A Class to model movement when the node cannot be saved.
 * 
 * @author Virginie Collombon, David San
 */
public class SosMovement extends MapBasedMovement implements SwitchableMovement {
	/**
	 * Creates a new movement model based on a Settings object's settings.
	 * 
	 * @param settings
	 *            The Settings object where the settings are read from
	 */
	public SosMovement(Settings settings) {
		super(settings);
	}

	/**
	 * Copyconstructor.
	 * 
	 * @param mbm
	 *            The SosMovement prototype to base the new object to
	 */
	public SosMovement(SosMovement mbm) {
		super(mbm);
	}

	@Override
	public Path getPath() {
		getHost().setDangerMode(DangerMovement.SOS_MODE);
		return new Path();
	}

	@Override
	public SosMovement replicate() {
		return new SosMovement(this);
	}

}

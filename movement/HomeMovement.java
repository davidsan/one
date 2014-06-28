package movement;

import core.Settings;

/**
 * A Class to model movement at the nodes' house.
 * 
 * @author Virginie Collombon, David San
 */
public class HomeMovement extends MapBasedMovement implements
		SwitchableMovement {
	/**
	 * Creates a new movement model based on a Settings object's settings.
	 * 
	 * @param settings
	 *            The Settings object where the settings are read from
	 */
	public HomeMovement(Settings settings) {
		super(settings);
	}

	/**
	 * Copyconstructor.
	 * 
	 * @param mbm
	 *            The HomeMovement prototype to base the new object to
	 */
	public HomeMovement(HomeMovement mbm) {
		super(mbm);
	}

	@Override
	public Path getPath() {
		getHost().setDangerMode(DangerMovement.HOME_MODE);
		return new Path();
	}

	@Override
	public HomeMovement replicate() {
		return new HomeMovement(this);
	}

}

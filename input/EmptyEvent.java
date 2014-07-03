package input;

/**
 * A Class for event doing nothing
 * 
 * @author Virginie Collombon, David San
 */
public class EmptyEvent extends ExternalEvent {

	private static final long serialVersionUID = 1L;

	public EmptyEvent(double time) {
		super(time);
	}

}

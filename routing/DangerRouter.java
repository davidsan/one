package routing;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimError;

/**
 * Danger Router
 * 
 * @author Virginie Collombon, David San
 */
public class DangerRouter extends EpidemicRouter {

	public static final String KEY_MESSAGE = "DANGER";

	public DangerRouter(Settings s) {
		super(s);

	}

	protected DangerRouter(EpidemicRouter r) {
		super(r);

	}

	@Override
	protected int startTransfer(Message m, Connection con) {
		return super.startTransfer(m, con);
	}

	@Override
	public void sendMessage(String id, DTNHost to) {
		Message m = getMessage(id);
		Message m2;
		if (m == null)
			throw new SimError("no message for id " + id + " to send at "
					+ super.getHost());

		m2 = m.replicate();
		if (super.getHost().isWarned()) {
			m2.addProperty(KEY_MESSAGE, true);
			// la valeur associé à la propriété n'a pas d'importance
		}
		to.receiveMessage(m2, super.getHost());
	}

	public DangerRouter replicate() {
		return new DangerRouter(this);
	}

}

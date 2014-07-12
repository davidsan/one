/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package routing;

import core.Connection;
import core.Settings;

/**
 * EpidemicRouter without tryAllMessagesToAllConnections() in update()
 * 
 * @author Virginie Collombon, David San
 */
public class EpidemicDeliverableRouter extends ActiveRouter {

	/**
	 * Constructor. Creates a new message router based on the settings in the
	 * given Settings object.
	 * 
	 * @param s
	 *            The settings object
	 */
	public EpidemicDeliverableRouter(Settings s) {
		super(s);
		// TODO: read&use epidemic router specific settings (if any)
	}

	/**
	 * Copy constructor.
	 * 
	 * @param r
	 *            The router prototype where setting values are copied from
	 */
	protected EpidemicDeliverableRouter(EpidemicDeliverableRouter r) {
		super(r);
		// TODO: copy epidemic settings here (if any)
	}

	@Override
	public void update() {
		super.update();
		if (isTransferring() || !canStartTransfer()) {
			return; // transferring, don't try other connections yet
		}

		// Try first the messages that can be delivered to final recipient
		if (exchangeDeliverableMessages() != null) {
			return; // started a transfer, don't try others (yet)
		}

	}

	@Override
	protected void transferDone(Connection con) {
		/*
		 * don't leave a copy for the sender if the message was passed to the
		 * final recipient
		 */
		if (con.getMessage().getHops().contains(con.getMessage().getTo())) {
			this.deleteMessage(con.getMessage().getId(), false);
		}
	}

	@Override
	public EpidemicDeliverableRouter replicate() {
		return new EpidemicDeliverableRouter(this);
	}

}
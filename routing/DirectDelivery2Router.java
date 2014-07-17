/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package routing;

import core.Connection;
import core.Settings;


/**
 * Router that will deliver messages only to the final recipient. It will also
 * delete message stored in buffer.
 */
public class DirectDelivery2Router extends ActiveRouter {

	/**
	 * Constructor. Creates a new message router based on the settings in the
	 * given Settings object.
	 * 
	 * @param s
	 *            The settings object
	 */
	public DirectDelivery2Router(Settings s) {
		super(s);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param r
	 *            The router prototype where setting values are copied from
	 */
	protected DirectDelivery2Router(DirectDelivery2Router r) {
		super(r);
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
	public DirectDelivery2Router replicate() {
		return new DirectDelivery2Router(this);
	}

}
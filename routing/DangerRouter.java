/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package routing;

import core.Connection;
import core.Message;
import core.Settings;

/**
 * Danger message router 
 */
public class DangerRouter extends EpidemicRouter {
	
	public static final String KEY_MESSAGE = "DANGER";
	
	/**
	 * Constructor. Creates a new message router based on the settings in
	 * the given Settings object.
	 * @param s The settings object
	 */
	public DangerRouter(Settings s) {
		super(s);
		//TODO: read&use epidemic router specific settings (if any)
	}
	
	/**
	 * Copy constructor.
	 * @param r The router prototype where setting values are copied from
	 */
	protected DangerRouter(DangerRouter r) {
		super(r);
		//TODO: copy epidemic settings here (if any)
	}
	
	@Override
	protected int startTransfer(Message m, Connection con) {
		if(super.getHost().isWarned()){
			if(m.getProperty(KEY_MESSAGE) == null){
				m.addProperty(KEY_MESSAGE, true);
			}
		}
		return super.startTransfer(m, con);
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
		
		// then try any/all message to any/all connection
		this.tryAllMessagesToAllConnections();
	}
	
	
	@Override
	public DangerRouter replicate() {
		return new DangerRouter(this);
	}

}
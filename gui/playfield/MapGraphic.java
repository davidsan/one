/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package gui.playfield;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import movement.map.MapNode;
import movement.map.SimMap;
import core.Coord;

/**
 * PlayfieldGraphic for SimMap visualization
 *
 */
public class MapGraphic extends PlayFieldGraphic {
	private SimMap simMap;
	private final Color PATH_COLOR = Color.LIGHT_GRAY;
	private final Color BG_COLOR = Color.WHITE;
	private final Color CLOSED_COLOR = Color.ORANGE;
		
	public MapGraphic(SimMap simMap) {
		this.simMap = simMap;

	}	
	
	// TODO: draw only once and store to buffer
	@Override
	public void draw(Graphics2D g2) {
		Coord c,c2;
		
		if (simMap == null) {
			return;
		}
		
		g2.setColor(PATH_COLOR);
		g2.setBackground(BG_COLOR);
		
		// draws all edges between map nodes (bidirectional edges twice)
		for (MapNode n : simMap.getNodes()) {
			c = n.getLocation();
			
			// draw a line to adjacent nodes
			for (MapNode n2 : n.getNeighbors()) {
				c2 = n2.getLocation();
				g2.drawLine(scale(c2.getX()), scale(c2.getY()),
						scale(c.getX()), scale(c.getY()));
			}
			
			if(n.isClosed()){
				g2.setColor(CLOSED_COLOR);
				
				int triangleSize = 6;
				int triangleSize2 = triangleSize*2;
				
				/* drawing of the triangle */
				g2.drawLine(scale(c.getX()-triangleSize), scale(c.getY()+triangleSize),
						scale(c.getX()+triangleSize), scale(c.getY()+triangleSize));

				g2.drawLine(scale(c.getX()-triangleSize), scale(c.getY()+triangleSize),
						scale(c.getX()), scale(c.getY()-triangleSize2+triangleSize));

				g2.drawLine(scale(c.getX()), scale(c.getY()-triangleSize2+triangleSize),
						scale(c.getX()+triangleSize), scale(c.getY()+triangleSize));

				/* drawing of the exclamation point */
				Stroke oldStroke = g2.getStroke();
				g2.setStroke(new BasicStroke(2));
				g2.drawLine(scale(c.getX()), scale(c.getY()+ triangleSize - triangleSize/1.5),
						scale(c.getX()), scale(c.getY()- triangleSize + triangleSize/2));
				
				g2.drawLine(scale(c.getX()), scale(c.getY()+ triangleSize - triangleSize/5),
						scale(c.getX()), scale(c.getY()+ triangleSize - triangleSize/6));
				g2.setStroke(oldStroke);
		
				g2.setColor(PATH_COLOR);
			}
		}
	}	

}

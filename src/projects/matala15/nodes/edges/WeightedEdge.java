package projects.matala15.nodes.edges;

import java.awt.Color;
import java.awt.Graphics;

import projects.sample6.nodes.nodeImplementations.TreeNode;
import sinalgo.configuration.Configuration;
import sinalgo.gui.helper.Arrow;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Position;
import sinalgo.nodes.edges.BidirectionalEdge;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.logging.Logging;

public class WeightedEdge extends BidirectionalEdge {
	
	private Logging logger = Logging.getLogger();
	
	private int weight;
	private boolean isDrawWeight = false; 
	private boolean isDrawDirected = false;
	
	// Show message on the edge
	private String message_on_edge = "";
	private int numOfMsgsPreviouslyOnThisEdge = -1; // Used to stop drawing message if no message sent the next round
	
	public void setWeight(int weight) {
		this.weight = weight;
	}
	
	public long getWeight() {
		return weight;
	}
	
	public void setIsDrawWeight(boolean value) {
		this.isDrawWeight = value;
	}
	
	public void setIsDrawDirected(boolean value) {
		this.isDrawDirected = value;
	}
	
	/**
	 * Draw weight on the edge
	 */
	private void drawWeight(Graphics g, PositionTransformation pt) {
		g.setColor(Color.BLACK);
		
		// Draw text weight on the edge
		Position start = this.startNode.getPosition();
		Position end = this.endNode.getPosition();
		
		pt.translateToGUIPosition(start);
		int fromX = pt.guiX, fromY = pt.guiY;
		pt.translateToGUIPosition(end);
		int toX = pt.guiX, toY = pt.guiY;
		
		int x = (int) ((toX + fromX)/2);
		int y = (int) ((toY + fromY)/2);
		
		String nice_weight = String.format("%,d", weight);
		g.drawString(nice_weight, x, y);
	}
	
	/**
	 * Draws arrow head. Taken from source code for Arrow.drawArrowHead()
	 * This function ignores the configuration file, which checks "<drawArrows value="false" />"
	 * And also increase the head size so its easier to see.
	 */
	private void drawArrowHead(Graphics g, PositionTransformation pt) {
		g.setColor(getColor()); // Set color of arrowhead as the edge itself
		// Calculate position of arrow head, where to draw
		// Taken from source code for 'Edge.draw()'
		Position p1 = startNode.getPosition();
		pt.translateToGUIPosition(p1);
		int fromX = pt.guiX, fromY = pt.guiY; // temporarily store
		Position p2 = endNode.getPosition();
		pt.translateToGUIPosition(p2);
		
		// Convert 'Edge.draw()' parameters to 'Arrow.drawArrowHead()' parameters
		int x1 = fromX;
		int y1 = fromY;
		int x2 = pt.guiX;
		int y2 = pt.guiY;
		Color col = getColor();

		// Increases arrow head size
		double headSizeFactor = 4.5;
		
		int arrowX[] = new int[3];
		int arrowY[] = new int[3];
		int unzoomedArrowLength = Configuration.arrowLength;
		int unzoomedArrowWidth = Configuration.arrowWidth;
		
		Color tmpCol = g.getColor();
		g.setColor(col);
				
		// the size of the arrow
		double arrowLength = unzoomedArrowLength * pt.getZoomFactor() * headSizeFactor;
		double arrowWidth = unzoomedArrowWidth * pt.getZoomFactor() * headSizeFactor;
		double lineLength = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));

		// shorten the arrow if the two nodes are very close
		if(2 * arrowLength >= lineLength) {
			arrowLength = lineLength / 3;
		}
		
		double factor = 1/lineLength;
		
		// unit vector in opposite direction of arrow
		double ux = (x1 - x2) * factor;
		double uy = (y1 - y2) * factor;
		
		// intersection point of line and arrow
		double ix = x2 + arrowLength * ux;
		double iy = y2 + arrowLength * uy;
		
		// one end-point of the triangle is (x2,y2), the second end-point (ex1, ey1) and the third (ex2, ey2)
		arrowX[0] = x2;
		arrowY[0] = y2;
		arrowX[1] = (int)(ix + arrowWidth * uy);
		arrowY[1] = (int)(iy - arrowWidth * ux);
		arrowX[2] = (int)(ix - arrowWidth * uy);
		arrowY[2] = (int)(iy + arrowWidth * ux);

		g.fillPolygon(arrowX, arrowY, 3);
		
		g.setColor(tmpCol);
	}
	
	/**
	 * Draw message on the edge (if any are sent).
	 * The sender message will be shown closer to the sender node.
	 */
	private void drawMsgOnEdge(Graphics g, PositionTransformation pt) {
		g.setColor(Color.BLUE);
		
		Position start = this.startNode.getPosition();
		Position end = this.endNode.getPosition();
		
		pt.translateToGUIPosition(start);
		int fromX = pt.guiX, fromY = pt.guiY;
		pt.translateToGUIPosition(end);
		int toX = pt.guiX, toY = pt.guiY;
		
		int x = (int) ((toX + fromX)/2);
		int y = (int) ((toY + fromY)/2);
		
//		if (TreeNode.FLAG_MIS_STARTED_GLOBAL == true) {
//			int x_diff = toX - fromX;
//			int y_diff = toY - fromY;
//			
//			x -= x_diff / 4;
//			y -= y_diff / 4;
//		}
		
		int x_diff = toX - fromX;
		int y_diff = toY - fromY;
		
		x -= x_diff / 4;
		y -= y_diff / 4;
		
		g.drawString(message_on_edge, x, y);
	}
	
	@Override
	public void draw(Graphics g, PositionTransformation pt) {
		super.draw(g, pt);
		
		// Draw weight
		if (weight != 0 && isDrawWeight)
			drawWeight(g, pt);
		
		// Draw arrow head (like directed edge)
		if (isDrawDirected) {
			drawArrowHead(g, pt);
		}
		
		// Draw messages
		int msgs = getNumberOfMessagesOnThisEdge();
		
		// 
		if (msgs == 0 && numOfMsgsPreviouslyOnThisEdge != 0) {
			message_on_edge = "";
		}
		if (message_on_edge != "") {
			drawMsgOnEdge(g, pt);
		}
		
		
		logger.logln("Total messages on edge: " + this + " are: " + msgs);
		if (numOfMsgsPreviouslyOnThisEdge == -1 && msgs > 0)
			numOfMsgsPreviouslyOnThisEdge = msgs;
	}
	
	
	
	@Override
	public void addMessageForThisEdge(Message msg) {
		super.addMessageForThisEdge(msg);
		message_on_edge = msg.toString();
	}
	
	@Override
	public String toString() {
		// isDrawWeight is not relevant, because tooltip shows only visible edge anyway
		String nice_weight = String.format("%,d", weight); // Add commas for big numbers to read better
		return "WeightedEdge("+ nice_weight +", " + startNode.ID + ", " + endNode.ID + ")";
	}
}

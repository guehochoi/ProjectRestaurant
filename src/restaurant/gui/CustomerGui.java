package restaurant.gui;

import restaurant.CustomerAgent;
import restaurant.HostAgent;

import java.awt.*;

public class CustomerGui implements Gui{

	private CustomerAgent agent = null;
	private boolean isPresent = false;
	private boolean isHungry = false;

	//private HostAgent host;
	RestaurantGui gui;
	private Map map = null;

	private int xPos, yPos;
	private int xDestination, yDestination;
	private enum Command {noCommand, GoToSeat, LeaveRestaurant, goToDest};
	private Command command=Command.noCommand;

	public static final int xTable = 200;
	public static final int yTable = 250;
	
	private static final int SIZE_CUSTOMER_X = 20;
	private static final int SIZE_CUSTOMER_Y = 20;
	
	
	
	private String myStr = "";
	
	public CustomerGui(CustomerAgent c, RestaurantGui gui){ //HostAgent m) {
		agent = c;
		xPos = AnimationPanel.WINDOWX + 100;
		yPos = AnimationPanel.WINDOWY;
		xDestination = AnimationPanel.WINDOWX + 100;
		yDestination = AnimationPanel.WINDOWY;
		this.gui = gui;
	}

	public void updatePosition() {
		if (xPos < xDestination)
			xPos++;
		else if (xPos > xDestination)
			xPos--;

		if (yPos < yDestination)
			yPos++;
		else if (yPos > yDestination)
			yPos--;

		if (xPos == xDestination && yPos == yDestination) {
			if (command==Command.GoToSeat) agent.msgAnimationFinishedGoToSeat();
			else if (command==Command.LeaveRestaurant) {
				agent.msgAnimationFinishedLeaveRestaurant();
				System.out.println("about to call gui.setCustomerEnabled(agent);");
				isHungry = false;
				gui.setCustomerEnabled(agent);
			}else if ( command == Command.goToDest ) {
				agent.msgAtDest();
			}
			command=Command.noCommand;
		}
	}

	public void draw(Graphics2D g) {
		g.setColor(Color.GREEN);
		g.fillRect(xPos, yPos, SIZE_CUSTOMER_X, SIZE_CUSTOMER_Y);
		
		g.setColor(Color.DARK_GRAY);
		g.drawString(myStr, xPos+5, yPos-5);
	}
	public void setString(String s) {
		this.myStr = s;
	}

	public boolean isPresent() {
		return isPresent;
	}
	public void setHungry() {
		isHungry = true;
		agent.gotHungry();
		setPresent(true);
	}
	public boolean isHungry() {
		return isHungry;
	}

	public void setPresent(boolean p) {
		isPresent = p;
	}

	public void DoGoToSeat() {//later you will map seatnumber to table coordinates.
		map.positionAvailable(this);
		command = Command.GoToSeat;
	}
	public void DoGoToCashier() {
		map.positionAvailable(this);
		xDestination = AnimationPanel.WINDOWX - SIZE_CUSTOMER_X;
		yDestination = AnimationPanel.WINDOWY - CustomerGui.SIZE_CUSTOMER_Y*2;
		command = Command.goToDest;
	}
	public void DoGoToRestaurant() {
		Point p = map.getWaitingAreaPos(this);
		xDestination = (int)p.getX();
		yDestination = (int)p.getY();
		//xDestination = WaiterGui.waitingArea_x + 20;
		//yDestination = WaiterGui.waitingArea_y;
		command = Command.goToDest;
	}
	public void goTo(Point dest) {
		map.positionAvailable(this);
		xDestination = (int)(dest.getX());
		yDestination = (int)(dest.getY());
	}

	public void DoExitRestaurant() {
		map.positionAvailable(this);
		xDestination = WaiterGui.waitingArea_x + 100;
		yDestination = WaiterGui.waitingArea_y;
		command = Command.LeaveRestaurant;
	}
	
	public void setMap(Map map) {
		this.map = map;
	}
	public CustomerAgent getCustomerAgent() {
		return this.agent;
	}
}

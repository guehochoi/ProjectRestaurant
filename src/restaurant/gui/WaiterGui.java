package restaurant.gui;


import restaurant.CustomerAgent;
import restaurant.HostAgent;
import restaurant.WaiterAgent;
import restaurant.interfaces.Customer;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.awt.*;

public class WaiterGui implements Gui {

    //private HostAgent agent = null;
	private WaiterAgent agent = null;
	private KitchenGui kitchenGui = null;
	private Map map = null;

    private int xPos = -20, yPos = -20;//default waiter position
    private int xDestination = -20, yDestination = -20;//default start position

    private List<Destination> destinations;
    
    public int xTable = 200;
    public int yTable = 250;
    
    public static final int SIZE_HOST_X = 20;
    public static final int SIZE_HOST_Y = 20;
    
    public static final int home_x = AnimationPanel.WINDOWX - 20;
    public static final int home_y = 0;
    
    public static final int waitingArea_x = AnimationPanel.WINDOWX - SIZE_HOST_X*2;
	public static final int waitingArea_y = AnimationPanel.WINDOWY - SIZE_HOST_Y - 100;
    
    private enum Command {noCommand, goToDest, backToCounter, nothingToDo};
    private Command command = Command.noCommand;
    
    
    private boolean isOnBreak = false;
    RestaurantGui gui;
    /*should be documented?*/
    private Collection<Food> foods = 
    		Collections.synchronizedList(new ArrayList<Food>()); 
    
    public enum FoodState { onTable, beingServed };
    class Food {
    	int tablenumber;
    	String food;
    	Point position;
    	FoodState s;
    	Food(String food, int tablenumber, FoodState s) {
    		Point p = AnimationPanel.TableMap.get(tablenumber);
    		this.tablenumber = tablenumber;
    		this.food = food;
    		this.position = p;
    		this.s = s;
    	}
    }
    /* adding class destinations to prevent race condition */
    class Destination {
    	Point p;
    	Command c;
    	public Destination(Point p, Command c) {
    		this.p = p;
    		this.c = c;
    	}
    }
    
    
    
    public WaiterGui(WaiterAgent agent, RestaurantGui gui) {
        this.agent = agent;
        this.gui = gui;
        destinations = new ArrayList<Destination>();
    }
    
    //notice added two methods
    public boolean isWaiterOnBreak() {
    	return this.isOnBreak;
    }
    public void setOnBreak() {
    	isOnBreak = true;
    	agent.msgWantBreak();
    }
    public void setOffBreak() {
    	isOnBreak = false;
    	agent.msgBackToWork();
    }
    public void enableBreak() {
    	gui.setWaiterBreakEnabled(agent);
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

        if (xPos == xDestination && yPos == yDestination
        		& (xDestination == xTable + 20) & (yDestination == yTable - 20)) {
        	// Animation has arrived at the table destination
        	
        	if (command==Command.goToDest) {
        		agent.msgAtDestination();
        		command = Command.noCommand;
        	}
        	
           if (!destinations.isEmpty()) {
        	   Destination dest = destinations.remove(0);
        	   xDestination = (int)dest.p.getX();
        	   yDestination = (int)dest.p.getY();
        	   command = dest.c;
           }
        }
        
        
        if (xPos == xDestination && yPos == yDestination) {
        	// Animation has arrived at off screen
        	if (command == Command.backToCounter) {
        	//if (command != Command.noCommand) {
        		agent.msgAtDestination();
        		command = Command.noCommand;
        	}
        	if (command == Command.goToDest) {
        		agent.msgAtDestination();
        		command = Command.noCommand;
        	}
        	
        	if (!destinations.isEmpty()) {
         	   Destination dest = destinations.remove(0);
         	   xDestination = (int)dest.p.getX();
         	   yDestination = (int)dest.p.getY();
         	   command = dest.c;
            }
        }
    }
    
    public void draw(Graphics2D g) {
        g.setColor(Color.MAGENTA);
        g.fillRect(xPos, yPos, SIZE_HOST_X, SIZE_HOST_Y);
        
        synchronized ( foods ) {
		for (Food f : foods) {
			if (f.s == FoodState.beingServed) {
				g.setColor(Color.cyan);
				g.drawString(f.food, xPos+3, yPos-3);
			}
			if (f.s == FoodState.onTable) {
				g.setColor(Color.cyan);
				g.drawString(f.food, (int)f.position.getX(), (int)f.position.getY()+30);
			}
		}//foods
        }//sync

    }

    public boolean isPresent() {
        return true;
    }

    public void bringFood(String s, int tablenumber) {
    	foods.add(new Food(s, tablenumber, FoodState.beingServed));
    }
    public void dropFood(int tablenumber) {
    	synchronized ( foods ) {
    	for (Food f : foods) {
    		if (f.tablenumber == tablenumber) {
    			f.s = FoodState.onTable;
    		}
    	}//foods
    	}//sync
    }
    public void DoBringToTable(Customer c, int seatnumber) {
    	Point position = AnimationPanel.TableMap.get(seatnumber);
    	xTable = (int)(position.getX());
    	yTable = (int)(position.getY());
    	
    	destinations.add(new Destination(new Point(xTable+20, yTable-20), Command.goToDest));
    	c.getGui().goTo(position);
        
    }

    public void DoGoToTable(int seatnumber) {    	
    	Point position = AnimationPanel.TableMap.get(seatnumber);
    	xTable = (int)(position.getX());
    	yTable = (int)(position.getY());
    	destinations.add(new Destination (new Point(xTable+20, yTable-20), Command.goToDest));
    }
    
    public void DoCleanTable(int tablenumber) {
    	synchronized ( foods ) {
    	for (Food f : foods) {
    		if (f.tablenumber == tablenumber) {
    			foods.remove(f);
    			break;
    		}
    	}//foods
    	}//sync
    }
    
    public void DoGoToCook() {
    	map.positionAvailable(this);
    	int xDes = AnimationPanel.WINDOWX/2;
    	int yDes = AnimationPanel.WINDOWY-30;
    	destinations.add(new Destination(new Point(xDes, yDes), Command.goToDest));
    }
    
    public void DoGoBackToCounter() {
    	map.positionAvailable(this);
    	destinations.add(new Destination (map.getWaiterHomePos(this), Command.backToCounter));
    	//destinations.add(new Destination (new Point(home_x, home_y), Command.backToCounter));
    	
    }
    public void nothingToDo() {
    	map.positionAvailable(this);
    	destinations.add(new Destination (map.getWaiterHomePos(this) , Command.nothingToDo ));
    	//destinations.add(new Destination (new Point(home_x, home_y) , Command.noCommand ));
    }
    public void DoGoToWaitingArea(Customer c) {
    	map.positionAvailable(this);
    	Point temp = map.getCustomerPosition(c);
    	Point p = new Point((int)temp.getX()-20, (int)temp.getY());
    	destinations.add(new Destination (p, Command.goToDest));
    	//destinations.add(new Destination (new Point(waitingArea_x, waitingArea_y), Command.goToDest));
    }

	public void DoGoToPlatingArea() {
		map.positionAvailable(this);
		destinations.add(new Destination (new Point(kitchenGui.getPlatingX(), kitchenGui.getPlatingYWaiter()), Command.goToDest));
	}
    
    public int getXPos() {
        return xPos;
    }

    public int getYPos() {
        return yPos;
    }

	public void takeFoodFromPlatingArea(String choice) {
		kitchenGui.takeFoodFromPlatingArea(choice);
	}
	
	public void setKitchenGui(KitchenGui kitchenGui) {
		this.kitchenGui = kitchenGui;
	}
    public void setMap(Map map) {
		this.map = map;
	}
    
    
}

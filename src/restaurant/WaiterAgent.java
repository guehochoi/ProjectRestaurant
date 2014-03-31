package restaurant;

import agent.Agent;
import restaurant.interfaces.*;
import restaurant.gui.KitchenGui;
//import restaurant.HostAgent.Table;
import restaurant.gui.WaiterGui;

import java.util.*;
import java.util.concurrent.Semaphore;
/*
 * Temporary Implementation Note and Decisions:
 * 1. I took all the state changes of MyCustomer in action ABOVE all other actions.
 *  	I think this will prevent the race conditions
 * */


public class WaiterAgent extends Agent implements Waiter {
	
	private List<MyCustomer> customers = 
			Collections.synchronizedList(new ArrayList<MyCustomer>());
	private Cook cook; // hack -- only one cook assumed
	private Host host; // hack --   "  "   host   "
	private Cashier cashier;
	
	private Collection<MyFood> foods = 
			Collections.synchronizedList(new ArrayList<MyFood>());
	
	private Semaphore atDest = new Semaphore(0,true);

	public WaiterGui waiterGui = null;
	private Menu menu;
	private String name;
	
	private boolean wantABreak = false;	//implementation detail 
	
	public enum CustomerState {
		waiting, seated, askedToOrder, asked, ordered, waitingForFood, eating,
		orderOut, doneEating, leaving, checkBeingIssued, checkIssued, checkDelivered
	}
	
	public enum FoodState {
		toBeServed
	}
	private class MyCustomer {
		Customer c;
		int table;
		CustomerState s;
		String choice;
		Check check;
		
		MyCustomer(Customer c, int table, CustomerState s) {
			this.c = c;
			this.table = table;
			this.s = s;
		}
	}
	
	private class MyFood {
		String choice;
		int table;
		FoodState s;
		
		MyFood (String choice, int table, FoodState s) {
			this.choice = choice;
			this.table = table;
			this.s = s;
		}
	}
	boolean backToWork = false;
	
	
	
	
	/* Messages */
	
	public void sitAtTable(Customer c, int table) {
		customers.add(new MyCustomer(c,table,CustomerState.waiting));
		stateChanged();
	}
	
	
	public void readyToOrder(Customer c) {
		synchronized ( customers ) {
		for (MyCustomer mc : customers) {
			if (mc.c.equals(c)) {
				customers.remove(mc);
				mc.s = CustomerState.askedToOrder;
				customers.add(0, mc);
				break;
			}
		}//customers
		}//sync
		stateChanged();
	}
	
	public void hereIsMyChoice(Customer c, String choice) {
		synchronized ( customers ) {
		for (MyCustomer mc : customers) {
			if (mc.c.equals(c)) {
				mc.choice = choice;
				mc.s = CustomerState.ordered;
			}
		}//customers
		}//sync
		stateChanged();
	}
	
	public void orderIsReady(String choice, int table) {
		foods.add(new MyFood(choice, table, FoodState.toBeServed));
		stateChanged();
	}
	public void doneEating(Customer c) {
		synchronized ( customers ) {
		for (MyCustomer mc : customers) {
			if (mc.c.equals(c)) {
				mc.s = CustomerState.doneEating;
			}
		}//customers
		}//sync
		stateChanged();
	}
	public void leaving(Customer c) {
		synchronized ( customers ) {
		for (MyCustomer mc : customers) {
			if (mc.c.equals(c)) {
				mc.s = CustomerState.leaving;
			}
		}//customers
		}//sync
		stateChanged();
	}
	
	public void outOf(String choice, int table) {
		synchronized ( customers ) {
		for (MyCustomer mc : customers) {
			if (mc.table == table && mc.choice.equalsIgnoreCase(choice)) {
				mc.s = CustomerState.orderOut;
			}
		}//customers
		}//sync
		stateChanged();
	}
	
	public void hereIsCheck(Check check, Customer c) {
		synchronized ( customers ) {
		for (MyCustomer mc : customers) {
			if (mc.c.equals(c)) {
				mc.check = check;
				mc.s = CustomerState.checkIssued;
			}
		}//customers
		}//sync
		stateChanged();
	}
	
	//implementation detail, not documented
	public void msgAtDestination() {//from animation
		//print("msgAtTable() called");
		atDest.release();// = true;
		stateChanged();
	}

	public void msgWantBreak() {
		wantABreak = true;
		stateChanged();
	}
	
	public void msgBackToWork() {
		backToWork = true;
		stateChanged();
	}
	
	/* Scheduler */
	
	protected boolean pickAndExecuteAnAction() {
		if (wantABreak) {
			requestBreak();
			wantABreak = false;
			return true;
		}
		if (backToWork) {
			notifyHost();
			backToWork = false;
			return true;
		}
		
		try {
		
		
		for (MyCustomer c : customers) {
			if (c.s == CustomerState.orderOut) {
				requestReorder(c);
				return true;
			}
		}
		
		for (MyFood f : foods) {
			if (f.s == FoodState.toBeServed) {
				serveFood(f);
				return true;
			}
		}
		for (MyCustomer c : customers) {
			if (c.s == CustomerState.ordered) {
				placeOrder(c);
				return true;
			}
		}
		for (MyCustomer c : customers) {
			if (c.s == CustomerState.doneEating) {
				requestCheck(c); 	
				return true;
			}
		}
		for (MyCustomer c : customers) {
			if (c.s == CustomerState.checkIssued) {
				bringCheckToCustomer(c); 	
				return true;
			}
		}
		for (MyCustomer c : customers) {
			if (c.s == CustomerState.leaving) {
				cleanTable(c); 	//table number -> MyCustomer
				return true;
			}
		}
		for (MyCustomer c : customers) {
			if (c.s == CustomerState.askedToOrder) {
				takeOrder(c);
				return true;
			}
		}
		for (MyCustomer c : customers) {
			if (c.s == CustomerState.waiting) {
				seatCustomer(c);
				return true;
			}
		}
		
		} catch (ConcurrentModificationException cme) {
			return false;
		}
		
		
		//implementation detail, not documented
		waiterGui.nothingToDo();
		return false;
		
	}
	
	/* Actions */
	
	private void notifyHost() {
		host.readyToWork(this);
	}

	private void requestBreak() {
		print("I want a break!!!");
		host.wantToGoOnBreak(this);
	}
	
	private void seatCustomer(MyCustomer c) {
		
		/*This is first because it will make more 
		 * sense to go to counter and take customer to the table */
		waiterGui.DoGoToWaitingArea(c.c);
		c.s = CustomerState.seated;
		try {
			atDest.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		print("Seating customer " + c.c.getName());
		c.c.followMe(this, menu); // changed from new Menu()
		//NOTE: parameter seatnumber is not in design yet
		
		DoSeatCustomer(c);
		
		try {
			atDest.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// hack to test break function
		//print("I Want A BREAK!!");
		//msgWantBreak();
		
		//waiterGui.DoLeaveCustomer();
		
	}
	// Implementation detail, not documented
	private void DoSeatCustomer(MyCustomer c) {
		//Notice how we print "customer" directly. It's toString method will do it.
		//Same with "table"
		
		waiterGui.DoBringToTable(c.c, c.table);

	}
	// Implementation detail, not documented
	private void goBackToCounter() {
		//waiterGui.DoGoBackToCounter();
		waiterGui.DoGoBackToCounter();
	}
	
	private void takeOrder(MyCustomer c) {
		waiterGui.DoGoToTable(c.table); // animation
		c.s = CustomerState.asked;
		try {
			atDest.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		print(c.c + ", what would you like?");
		c.c.whatWouldYouLike();
	}
	
	private void placeOrder(MyCustomer c) {
		goBackToCounter(); // goes off screen
		c.s = CustomerState.waitingForFood;
		try {
			atDest.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		print("here is order " + c.choice + " for table " + c.table);
		cook.hereIsOrder(this, c.choice, c.table);
	}
	
	private void serveFood(MyFood f) {
		waiterGui.DoGoToPlatingArea();	//animation
		//goBackToCounter(); // goes off the screen
		try {
			atDest.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		waiterGui.takeFoodFromPlatingArea(f.choice);
		//kitchenGui.takeFoodFromPlatingArea(f.choice);
		waiterGui.bringFood(f.choice, f.table); // animation
		synchronized ( customers ) {
		for (MyCustomer c : customers) {
			if (c.table == f.table && c.choice.equalsIgnoreCase(f.choice)) {
				
				waiterGui.DoGoToTable(c.table); // animation
				c.s = CustomerState.eating;
				try {
					atDest.acquire();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				print("here is your food, " + c.c);
				
				c.c.hereIsYourFood();
				waiterGui.dropFood(c.table);
				
			}
		}//customers
		}//sync
		foods.remove(f);
	}
	
	private void cleanTable(MyCustomer c) { //int table
		waiterGui.DoGoToTable(c.table); // animation
		try {
			atDest.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		waiterGui.DoCleanTable(c.table); // animation
		MyCustomer del = null;
		print ("clean table" + c.table);
		for(MyCustomer mc: customers) {
			if (mc.table == c.table) {
				del = mc;
			}
		}
		customers.remove(del);
		host.tableIsFree(c.table);
	}
	
	private void requestReorder(MyCustomer c) {
		waiterGui.DoGoToTable(c.table); // animation
		c.s = CustomerState.seated;
		try { 
			atDest.acquire();
		}catch(InterruptedException ex) {
			ex.printStackTrace();
		}
		print(c.c + ", I am sorry, we are out of " + c.choice + " today");
		// Deletion of outOfStocked menu is handled here
		Menu m = new Menu();
		m.removeItemFromMenu(c.choice);
		//menu.removeItemFromMenu(c.choice);\
		c.c.pleaseOrderAgain(m);
		
		// Notice the state change of customer, goes back to normative scenario
		
	}
	
	private void requestCheck(MyCustomer c) {
		print (cashier + ", can i have check for " + c.c);
		c.s = CustomerState.checkBeingIssued;
		cashier.produceCheck( c.c, c.choice, this);	
	}
	
	private void bringCheckToCustomer(MyCustomer c) {
		print(c.c + ", here is your check");
		c.s = CustomerState.checkDelivered;
		c.c.hereIsCheck(c.check);
	}
	
	
	/* Utilities */
	
	public WaiterAgent(String name) {
		super();
		this.name = name;
		// notice that I added different menu structure
		menu = new Menu();
	}
	
	public void setBreakEnabled() {
		waiterGui.enableBreak();
	}
	public String getName() {
		return this.name;
	}
	public String toString() {
		return "waiter " + getName();
	}
	public void setGui(WaiterGui gui) {
		waiterGui = gui;
	}
	public WaiterGui getGui() {
		return waiterGui;
	}
	public void setHost(Host host) {
		this.host = host;
		notifyHost(); // notify that I am ready to work
	}
	public void setCook(Cook cook) {
		this.cook = cook;
	}
	public void setCashier(Cashier cashier) {
		this.cashier = cashier;
	}

	
}

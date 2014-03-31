package restaurant;

import agent.Agent;
import restaurant.interfaces.*;
import restaurant.gui.WaiterGui;

import java.util.*;
import java.util.concurrent.Semaphore;
import restaurant.gui.RestaurantGui;
/**
 * Restaurant Host Agent
 */
//We only have 2 types of agents in this prototype. A customer and an agent that
//does all the rest. Rather than calling the other agent a waiter, we called him
//the HostAgent. A Host is the manager of a restaurant who sees that all
//is proceeded as he wishes.
public class HostAgent extends Agent implements Host {
	RestaurantGui restGui;
	// NTABLES changed to public
	public static int NTABLES = 5;//a global for the number of tables.
	public final static int MAXNTABLES = 9;
	//Notice that we implement waitingCustomers using ArrayList, but type it
	//with List semantics.
	public List<MyCustomer> customers
	= Collections.synchronizedList(new ArrayList<MyCustomer>());
	
	class MyCustomer{
		Customer c;
		CustomerState s;
		MyCustomer (Customer c, CustomerState s) {
			this.c = c;
			this.s = s;
		}
	}
	public enum CustomerState {
		wantFood, checking, checked, informed, waiting, kickOut
	}
	private Cashier cashier;
	
	public Collection<Table> tables;
	//note that tables is typed with Collection semantics.
	//Later we will see how it is implemented
	
	
	private List<MyWaiter> waiters = 
			Collections.synchronizedList(new ArrayList<MyWaiter>());
	public enum WaiterState {
		available, breakRequested, onBreak
	}
	
	private String name;
	boolean isRestaurantOpen = false;
	
	private class Table {
		Customer occupiedBy;
		int tableNumber;

		Table(int tableNumber) {
			this.tableNumber = tableNumber;
		}

		void setOccupant(Customer cust) {
			occupiedBy = cust;
		}

		void setUnoccupied() {
			occupiedBy = null;
		}

		Customer getOccupant() {
			return occupiedBy;
		}

		boolean isOccupied() {
			return occupiedBy != null;
		}

		public String toString() {
			return "table " + tableNumber;
		}
	}
	
	private class MyWaiter{
		Waiter w;
		WaiterState s;
		
		MyWaiter(Waiter w, WaiterState s) {
			this.w = w;
			this.s = s;
		}
	}
	
	
	
	
	// Messages

	public void msgIWantFood(Customer cust) {
		customers.add(new MyCustomer(cust, CustomerState.wantFood));
		stateChanged();
	}
	
	public void tableIsFree(int t) {
		synchronized ( tables ) {
			for(Table table : tables) {
				if (table.tableNumber == t) {
					table.setUnoccupied();
				}
			}
		}
		stateChanged();
	}
	
	public void readyToWork(Waiter w) {
		boolean found = false;
		synchronized ( waiters ) {
		for (MyWaiter mw : waiters) {
			if (mw.w.equals(w)) {
				mw.s = WaiterState.available;
				waiters.remove(mw);
				waiters.add(0, mw);
				found = true;
				break;
			}
		}
		}
		if (!found)
			waiters.add(0, new MyWaiter(w, WaiterState.available));
		stateChanged();
	}
	
	public void wantToGoOnBreak(Waiter w){
		synchronized ( waiters ) {
			for (MyWaiter mw : waiters) {
				if (mw.w.equals(w)) {
					mw.s = WaiterState.breakRequested;
				}
			}
		}
		stateChanged();
	}
	
	public void customerClear(Customer c, boolean clear) {
		synchronized ( customers ) {
		for (MyCustomer mc : customers) {
			if (mc.c.equals(c)) {
				if (clear) {
					mc.s = CustomerState.checked;
				}else {
					mc.s = CustomerState.kickOut;
				}
			}
		}//customers
		}//sync
		stateChanged();
	}
	
	public void iAm(boolean staying, Customer c) {
		synchronized ( customers )  {
		for (MyCustomer mc : customers) {
			if ( mc.c.equals(c)) {
				if (staying) {
					mc.s = CustomerState.waiting;
				}else {
					customers.remove(mc);
					break;
				}
			}
		}//customers
		}//sync
		stateChanged();
	}
	public void takeCustomers() {
		isRestaurantOpen = true;
		stateChanged();
	}
	
	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		/* Think of this next rule as:
            Does there exist a table and customer,
            so that table is unoccupied and customer is waiting.
            If so seat him at the table.
		 */
		if (!isRestaurantOpen) {
			return false;
		}
		
		synchronized ( waiters ) {
			for (MyWaiter w : waiters) {
				if (w.s == WaiterState.breakRequested) {
					acceptOrDenyBreak(w);
					return true;
				}
			}
		}
		
		synchronized ( customers )  {
		for (MyCustomer mc : customers) {
			if ( mc.s == CustomerState.checked ) {
				informAvailability(mc);
				return true;
			}
		}//customers
		}//sync
		
		synchronized ( tables ) {
		for (Table table : tables) {
			if (!table.isOccupied()) {
				synchronized ( customers )
				{
				for (MyCustomer c : customers) {
					if ( c.s == CustomerState.waiting ) {
						synchronized ( waiters ) {
						for (MyWaiter w : waiters) {
							if (w.s == WaiterState.available) {
								takeCustomerToTable(c, table, w);
								return true;//return true to the abstract agent to reinvoke the scheduler.
							}
						}//waiters
						}//sync
					}
					
				}//customers
				}// sync
				
			}
		}//tables
		}//sync
		
		synchronized ( customers ) {
		for (MyCustomer c : customers) {
			if (c.s == CustomerState.wantFood) {
				requestHistoryCheck(c);
				return true;
			}
		}//customers
		}//sync
		
		synchronized ( customers ) {
		for (MyCustomer c : customers) {
			if (c.s == CustomerState.kickOut) {
				kickOutCustomer(c);
				return true;
			}
		}//customers
		}//sync
		
		
		return false;
		//we have tried all our rules and found
		//nothing to do. So return false to main loop of abstract agent
		//and wait.
	}

	// Actions
	
	private void takeCustomerToTable (MyCustomer c, Table table, MyWaiter w) {
		print("hey " + w.w + " take customer " + c.c +" to the table " + table.tableNumber);
		table.setOccupant(c.c);
		customers.remove(c); 
		//waiter.sitAtTable(c, table.tableNumber);
		w.w.sitAtTable(c.c, table.tableNumber);
		waiters.remove(w);
		waiters.add(w);		//creates the working turns for waiters
		
	}
	
	private void acceptOrDenyBreak(MyWaiter w) {
		// TODO: eligibility check can be updated to more acknowledge-able 
		int count = 0 ;
		if (w.s != WaiterState.breakRequested)
			return;
		for (MyWaiter mw : waiters) {
			if (mw.s == WaiterState.available)
				count ++;
		}
		if (count > 0) {
			// notice that waiter who requested break is on 'breakRequested' state
			// Accepted
			w.s = WaiterState.onBreak;
			print(w.w + ", go on your break");
		}else {
			// Denied
			w.s = WaiterState.available;
			print(w.w + ", your break has been denied");
			restGui.setWaiterBreakEnabled((WaiterAgent)w.w); // TODO: hack

		}
		// On break simply means host will not assign anymore customer to him
	}
	
	private void requestHistoryCheck(MyCustomer c) {
		print (cashier + ", please check the history of " + c.c); 
		cashier.historyCheck(c.c);
		c.s = CustomerState.checking;
	}
	
	private void kickOutCustomer(MyCustomer c) {
		print ("Get out of here, you cannot eat here before you pay!!");
		c.c.getOut();
		customers.remove(c);
	}
	
	private void informAvailability( MyCustomer c ) {
		synchronized ( tables ) {
		for (Table table : tables) {
			if (!table.isOccupied()) {
				c.s = CustomerState.waiting;
				return;
			}
		}//tables
		}//sync
		print(c.c + ", our restaurant is currently full. Would you like to wait?");
		c.c.restaurantIsFull();
		c.s = CustomerState.informed;
		
	}
	
	/* Utilities */


	public HostAgent(String name) {
		super();

		this.name = name;
		// make some tables
		tables = Collections.synchronizedList(new ArrayList<Table>(NTABLES));
		for (int ix = 1; ix <= NTABLES; ix++) {
			tables.add(new Table(ix));//how you add to a collections
		}
	}

	public String getMaitreDName() {
		return name;
	}

	public String getName() {
		return name;
	}
	public String toString() {
		return this.name;
	}

	public void setCashier(Cashier cashier) {
		this.cashier = cashier;
	}
	public List getWaitingCustomers() {
		return customers;
	}

	public Collection getTables() {
		return tables;
	}
	public void setRestGui(RestaurantGui gui) {
		this.restGui = gui;
	}
	
	public void addTable() {
		NTABLES = NTABLES + 1;
		tables.add(new Table(NTABLES));
	}
	
	
}


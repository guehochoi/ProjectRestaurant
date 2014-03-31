package restaurant;

import restaurant.gui.CookGui;
import restaurant.gui.CustomerGui;
import restaurant.interfaces.*;
import restaurant.gui.RestaurantGui;
import agent.Agent;

import java.util.*;
import java.util.concurrent.Semaphore;

public class CookAgent extends Agent implements Cook {
	
	CookGui cookGui;
	
	private List<Order> orders = 
			Collections.synchronizedList(new ArrayList<Order>());
	public enum OrderState {
		pending, cooking, done
	}
	
	private Timer timer;
	private Map<String, Food> foods = new HashMap<String, Food>();
	
	private String name;
	
	private List<MyMarket> markets = 
			Collections.synchronizedList(new ArrayList<MyMarket>());
	
	private Semaphore atDest = new Semaphore(0, true);
	
	enum DeliveryState { onDelivery, confirmed, delivered };
	
	enum AgentState { sleeping, atWork, openingRestaurant, initStocked, opened } 
	AgentState state = AgentState.sleeping;
	Host host;
	
	public class Order {
		Waiter w;
		String choice;
		int table;
		OrderState s;
		
		
		Order(Waiter w, String choice, int table, OrderState s) {
			this.w = w; 
			this.choice = choice;
			this.table = table;
			this.s = s;
		}
	}
	private class Food {
		String type;
		int cookingTime;
		int amount;
		int low;
		int restockAmount;
		boolean isOrdered;
		int incomingOrder;
		
		Food(String type, int cookingTime, int amount, int low, int restockAmount) {
			this.type = type; 
			this.cookingTime = cookingTime;
			this.amount = amount;
			this.low = low;
			this.restockAmount = restockAmount;
			this.isOrdered = false;
			incomingOrder = 0;
		}
		synchronized public void setIncomingOrder(int incomingOrder) {
			this.incomingOrder = incomingOrder;
		}
	}
	
	public class MyMarket {
		Market m;
		List<String> availableList = 
				Collections.synchronizedList(new ArrayList<String>());
		List<MarketOrder> orders = 
				Collections.synchronizedList(new ArrayList<MarketOrder>());
		
		MyMarket(Market m) {
			this.m = m;
			availableList.add("Chicken");availableList.add("Beef");
			availableList.add("Pork");availableList.add("Turkey");
			availableList.add("duck");
		}
	}

	class MarketOrder {
		String choice;
		int quantity;
		DeliveryState s;
		
		MarketOrder(String choice, int quantity, DeliveryState s) {
			this.choice = choice;
			this.quantity = quantity;
			this.s = s;
		}
	}
	
	
	/* Messages */
	
	public void hereIsOrder(Waiter w, String choice, int table) {
		orders.add(new Order(w, choice, table, OrderState.pending));
		stateChanged();
	}
	
	public void foodDone(Order o) {
		o.s = OrderState.done;
		stateChanged();
	}
	
	public void weAreOutof(String choice, Market m) {
		synchronized ( markets ) {
		for (MyMarket myM : markets) {
			if (myM.m.equals(m)) {
				myM.availableList.remove(choice);
			}
		}//markets
		}//sync
		stateChanged();
	}
	
	public void deliveryScheduled(String choice, int quantity, Market m) {
		synchronized ( markets ) {
		for (MyMarket myM : markets) {
			if (myM.m.equals(m)) {
				myM.orders.add( new MarketOrder (choice, quantity, DeliveryState.onDelivery));
			}
		}//markets
		}//sync
		stateChanged();
	}
	
	public void deliveryFor(String choice, Market m) {
		synchronized ( markets ) {
		for (MyMarket myM : markets) {
			if (myM.m.equals(m)) {
				synchronized (myM.orders) {
				for (MarketOrder mo : myM.orders){
					if (mo.choice.equalsIgnoreCase(choice)) {
						mo.s = DeliveryState.delivered;
					}
				}//myM.orders
				}//sync
			}
		}//markets
		}//sync
		stateChanged();
	}
	
	public void goToWork() {
		state = AgentState.atWork;
		stateChanged();
	}
	
	public void msgAtDestination() {
		atDest.release();
		stateChanged();
	}
	
	
	/* Scheduler */
	protected boolean pickAndExecuteAnAction() {
		
		if (state == AgentState.atWork) {
			openRestaurant();
			return true;
		}
		if (state == AgentState.initStocked) {
			tellHostToTakeCustomers();
			return true;
		}
		
		synchronized ( orders ) {
		for( Order o : orders ) {
			if (o.s == OrderState.done) {
				plateIt(o);
				return true;
			}
		}//orders
		}//sync

		synchronized ( orders ) {
		for( Order o : orders ) {
			if (o.s == OrderState.pending) {
				cookIt(o);
				return true;
			}
		}//orders
		}//sync
		
		synchronized ( markets ) {
		for (MyMarket m : markets) {
			for (MarketOrder o : m.orders) {
				if (o.s == DeliveryState.onDelivery) {
					confirmOrder(m);
					return true;
				}
			}
		}//markets
		}//sync
		
		synchronized ( markets ) {
		for (MyMarket m : markets) {
			for (MarketOrder o : m.orders) {
				if (o.s == DeliveryState.delivered) {
					restock(m);
					return true;
				}
			}
		}//markets
		}//sync
		
		cookGui.moveToHome();
		
		return false;
	}
	
	
	
	/* Actions */
	private void cookIt(Order o) {
		Food f = foods.get(o.choice);
		if (f.amount <= f.low && !f.isOrdered) {
			synchronized ( markets ) {
			for (MyMarket m : markets) {
				if (m.availableList.contains(f.type)) {
					print(m.m.getName() + ", i need " + f.type + " asap");
					m.m.orderFor(f.type, f.restockAmount);
				}
			}//markets
			}//sync
			f.setIncomingOrder(0);
			//f.incomingOrder = 0;// this is bug-prone, think about "Late Delivery"
			f.isOrdered = true; // These two lines need better way
		}
		if (f.amount <= 0) {
			print(o.w + " " + o.choice + " is out of stock");
			o.w.outOf(o.choice, o.table);
			orders.remove(o);
			return;
		}
		f.amount--;
		
		final Order fo = o;	
		fo.s = OrderState.cooking;
		print("I am cooking " + o.choice + " for " + o.table);
		cookGui.DoCooking(o.choice);
		try{
			atDest.acquire();
		}catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		
		timer.schedule(new TimerTask() {
			public void run() {
				foodDone(fo);
				//o.s = OrderState.done;
			}
		}, f.cookingTime);

	}
	private void plateIt(Order o) {
		cookGui.DoPlating(o.choice); // Animation
		try{
			atDest.acquire();
		}catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		
		print("done cooking, " + o.choice);
		o.w.orderIsReady(o.choice, o.table);
		orders.remove(o);
	}
	
	private void confirmOrder(MyMarket m ) {
		
		synchronized ( m.orders ) {
		for (MarketOrder o : m.orders) {
			if (o.s == DeliveryState.onDelivery) {
				Food f = foods.get(o.choice);
				if (o.quantity <= f.restockAmount - f.incomingOrder) {
					//f.setIncomingOrder(f.incomingOrder + o.quantity);
					f.incomingOrder += o.quantity;
					o.s = DeliveryState.confirmed;
					print(m.m + ", I confirm the order of "+o.choice +" amount "+ o.quantity +", send me asap");
					m.m.confirmation(true, o.choice);
				}else {
					print("I do not confirm the order. I already filled my " + o.choice);
					m.orders.remove(o);
					m.m.confirmation(false, o.choice);
				}
				return;
			}
		}//m.orders
		}//sync
		
	}
	private void restock(MyMarket m){
		if (state == AgentState.openingRestaurant)
			state = AgentState.initStocked;
		synchronized ( m.orders ) {
		for (MarketOrder o : m.orders) {
			if (o.s == DeliveryState.delivered) {
				Food f = foods.get(o.choice);
				f.amount = f.amount + o.quantity;
				f.isOrdered = false;
				print(f.amount + " of " +o.choice + " have been restocked.");
				m.orders.remove(o);
				return;
			}
		}//m.orders
		}//sync
		
	}
	private void openRestaurant() {
		state = AgentState.openingRestaurant;
		print("Initial restocking foods");
		boolean nothingToRestock = true;
		Food f;
		for (Menu.Type t : Menu.Type.values()) {
			f = foods.get(t.toString());
			if (f.amount <= f.low && !f.isOrdered) {
				nothingToRestock = false;

				for (MyMarket m : markets) {
					print(m.m.getName() + ", i need " + f.type + " to open the restaurant");
					m.m.orderFor(f.type, f.restockAmount);

				}//sync
				f.setIncomingOrder(0);
				//f.incomingOrder = 0;// this is bug-prone, think about "Late Delivery"
				f.isOrdered = true; // These two lines need better way
			}
		}
		if (nothingToRestock) {
			print("there is nothing to restock");
			tellHostToTakeCustomers();
		}
	}
	private void tellHostToTakeCustomers() {
		print(host + ", let's start working ");
		state = AgentState.opened;
		host.takeCustomers();
	}
	
	
	
	/* utilities */
	
	public CookAgent(String name) {
		super();
		this.name = name;
		timer = new Timer();
		// foods cooking time, amount, low preset, restockAmount
		foods.put("Chicken", new Food("Chicken", 3000, 10, 4, 10));
		foods.put("Beef", new Food("Beef", 2000, 0, 4, 5));
		foods.put("Turkey", new Food("Turkey", 4000, 10, 4, 10));
		foods.put("Pork", new Food("Pork", 5000, 10, 4, 10));
		foods.put("Duck", new Food("Duck", 4000, 10, 4, 10));
		
	}
	
	public String getName() {
		return this.name;
	}
	public String toString() {
		return this.name;
	}
	public void addMarket(Market m) {
		this.markets.add(new MyMarket(m));
	}
	public void setHost(Host host) {
		this.host = host;
	}
	public void setGui(CookGui cookGui) {
		this.cookGui = cookGui;
	}

	/* HACKS */
	public void gotNoChicken() {
		foods.get("Chicken").amount = 0;
	}

	
	
	
}


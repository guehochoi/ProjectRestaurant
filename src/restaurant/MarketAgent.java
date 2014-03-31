package restaurant;

import restaurant.CashierAgent.PaymentState;
import restaurant.CookAgent.MyMarket;
import restaurant.gui.CustomerGui;
import restaurant.interfaces.*;
import restaurant.gui.RestaurantGui;
import agent.Agent;

import java.util.*;

public class MarketAgent extends Agent implements Market {
	
	private List<Order> orders = 
			Collections.synchronizedList(new ArrayList<Order>());
	public enum OrderState {
		orderReceived, preparing, toBeDelivered
	}
	
	Cook cook;
	Cashier cashier;
	private Timer timer = new Timer();
	private Map<String, Item> inventory = new HashMap<String, Item>();
	
	private String name;
	
	private class Order {
		String choice;
		OrderState s;
		int quantity;
		
		
		Order(String choice, int quantity, OrderState s) {
			this.choice = choice;
			this.quantity = quantity;
			this.s = s;
		}
	}
	private class Item {
		String type;
		int deliveryTime;
		int stockAmount;
		
		Item(String type, int deliveryTime, int stockAmount) {
			this.type = type; 
			this.deliveryTime = deliveryTime;
			this.stockAmount = stockAmount;
		}
	}
	
	private double marketBudget = 0;
	
	private List<Payment> payments = 
			Collections.synchronizedList(new ArrayList<Payment>());
			
	public enum PaymentState {
		pending, paid, complete, unpaid, deferred
	}
	private class Payment {
		Check check;
		Cashier c;
		double cash;
		PaymentState s;
		double interest;
		
		Payment(Check check, double cash, Cashier c, PaymentState s) {
			this.check = check;
			this.cash = cash;
			this.c = c;
			this.s = s;
		}
		Payment ( Check check, Cashier c, PaymentState s) {
			this.check = check;
			this.c = c;
			this.s = s;
		}
	}
	
	
	/* Messages */
	
	public void orderFor(String choice, int quantity) {
		orders.add(new Order(choice, quantity, OrderState.orderReceived));
		stateChanged();
	}
	
	public void confirmation(boolean approval, String choice) {
		synchronized ( orders ) {
		for (Order o : orders) {
			if (o.s == OrderState.preparing && o.choice.equalsIgnoreCase(choice)) {
				if(approval) {
					o.s = OrderState.toBeDelivered;
				}else {
					orders.remove(o);
					break;
				}
			}
		}//orders
		}//sync
		stateChanged();
	}
	
	public void hereIsPayment(Check check, double cash, Cashier c) {
		payments.add( new Payment ( check, cash, c, PaymentState.paid ));
		stateChanged();
	}
	public void iAmShort(Check check, Cashier c) {
		Payment p = new Payment (check, c, PaymentState.unpaid );
		p.interest = 0.5;
		payments.add(p);
		stateChanged();
	}
	
	
	/* Scheduler */
	protected boolean pickAndExecuteAnAction() {
		
		synchronized ( orders ) {
		for( Order o : orders ) {
			if (o.s == OrderState.orderReceived) {
				processOrder(o);
				return true;
			}
		}//orders
		}//sync

		synchronized ( orders ) {
		for( Order o : orders ) {
			if (o.s == OrderState.toBeDelivered) {
				deliver(o);
				return true;
			}
		}//orders
		}//sync
		
		synchronized ( payments ) {
		for( Payment p : payments ) {
			if (p.s == PaymentState.pending) {
				requestPayment(p);
				return true;
			}
		}//orders
		}//sync
		
		synchronized ( payments ) {
		for( Payment p : payments ) {
			if (p.s == PaymentState.paid) {
				processPayment(p);
				return true;
			}
		}//orders
		}//sync
	
		return false;
	}
	
	
	/* Actions */
	public void processOrder(Order o) {
		Item i = inventory.get(o.choice);
		if (i.stockAmount <= 0) {
			print("We are out of " + o.choice);
			cook.weAreOutof(o.choice, this);
			orders.remove(o);
		}else { // if ( i.stockAmount > 0 )
			o.s = OrderState.preparing;
			print("Please confirm your order for " + o.choice);
			if (i.stockAmount < o.quantity) {
				o.quantity = i.stockAmount;
				cook.deliveryScheduled(o.choice, i.stockAmount, this);
			}
			else { // (i.stockAmount >= o.quantity) 
				cook.deliveryScheduled(o.choice, o.quantity, this);
			}
			
		}
	}
	public void deliver(Order o) {
		orders.remove(o);
		Item i = inventory.get(o.choice);
		i.stockAmount -= o.quantity;
		final Order fo = o;
		final MarketAgent fm = this;
		Check check = new Check();
		check.addItem(o.choice, o.quantity);
		final Check fc = check;
		timer.schedule(new TimerTask() {
			public void run() {
				print("hey, " + cook + " here is order of " + fo.choice + " " + fo.quantity);
				cook.deliveryFor(fo.choice, fm);
				payments.add(new Payment(fc, cashier, PaymentState.pending));
				stateChanged();
			}
		}, inventory.get(o.choice).deliveryTime);
	}
	
	private void requestPayment( Payment p ) {
		//TODO: check on the payments to make sure he doesn't have deferred payment
		for (Payment p1 : payments) {
			if (p1.s == PaymentState.unpaid && p1.c.equals(p.c)) {
				p.check.appendCheckWithInterest(p1.check, p1.interest);
				payments.remove(p1);
			}
		}
		print (p.c + ", here is check, please pay asap");
		p.c.hereIsCheck( p.check, this);
		payments.remove(p);
	}
	
	private void processPayment (Payment p) {
		marketBudget += p.cash;
		print("Payment from " + p.c + " processed, budget: " + marketBudget);
		p.s = PaymentState.complete;
	}
	
	/* utilities */
	public MarketAgent(String name) {
		super();
		this.name = name;
		//this.cook = c;
		// inventory: String, Item ( type, deliveryTime, stockAmount )
		inventory.put("Chicken", new Item("Chicken", 10000, 10));
		inventory.put("Beef", new Item("Beef", 10000, 5));
		inventory.put("Turkey", new Item("Turkey", 10000, 10));
		inventory.put("Pork", new Item("Pork", 10000, 10));
		inventory.put("Duck", new Item("Duck", 10000, 10));
	}
	
	public String getName() {
		return this.name;
	}
	public String toString() {
		return this.name;
	}
	public void setCook(Cook c) {
		this.cook = c;
	}
	public void setCashier(Cashier c) {
		this.cashier = c;
	}
	
	
}


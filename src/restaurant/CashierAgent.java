package restaurant;

import agent.Agent;
import restaurant.interfaces.*;
import restaurant.test.mock.EventLog;
import restaurant.test.mock.LoggedEvent;
import restaurant.HostAgent;

import java.util.*;

public class CashierAgent extends Agent implements Cashier {
	
	public EventLog log = new EventLog();
	
	private String name;
	public Host host;
	public List<CheckOrder> checkOrders = 
			Collections.synchronizedList(new ArrayList<CheckOrder>());
	class CheckOrder {
		Customer c;
		String choice;
		Waiter w;
		CheckOrderState s;

		CheckOrder(Customer c, String choice, Waiter w, CheckOrderState s) {
			this.c= c; this.choice = choice; this.w = w; this.s = s;
		}
	}
	public enum CheckOrderState {
		requested
	}
	
	public List<Payment> payments = 
			Collections.synchronizedList(new ArrayList<Payment>());
	public class Payment {
		public Check check;
		public double cash;
		public Customer c;
		public Market m;
		public PaymentState s;
		
		Payment(Check check, double cash, Customer c, PaymentState s) {
			this.check = check; this.cash = cash; this.c = c; this.s = s;
		}
		Payment(Check check, Market m, PaymentState s) {
			this.check = check; this.m = m; this.s = s;
		}
	}
	public enum PaymentState {
		pending, paid, unpaidPending, unpaid, unpaidRevisit, unpaidProcessing, unpaidPaid, unpaidPendingAgain,
		marketPending, paidMarket
	}
	
	public double restaurantBudget = 1000;
	
	public List<Customer> cleanCustomers = 
			Collections.synchronizedList(new ArrayList<Customer>());;
 	
	/* Messages */
	
	public void produceCheck(Customer c, String choice, Waiter w) {
		checkOrders.add ( new CheckOrder(c, choice, w, CheckOrderState.requested));
		stateChanged();
		log.add(new LoggedEvent("Received produceCheck"));
	}
	
	public void payment(Check check, double cash, Customer c) {
		payments.add(new Payment(check, cash, c, PaymentState.pending));
		stateChanged();
		log.add(new LoggedEvent("Received payment"));
	}
	
	public void cannotPayBill(Check check, Customer c) {
		boolean found = false;
		synchronized ( payments ) {
		for (Payment p:payments ) {
			if (p.c.equals(c) && p.s == PaymentState.unpaidProcessing) {
				p.s = PaymentState.unpaidPendingAgain;
				found = true;
			}
		}//payments
		}//sync
		if (!found) {
			payments.add(new Payment(check, 0, c, PaymentState.unpaidPending));
		}
		stateChanged();
		log.add(new LoggedEvent("Received cannotPayBill"));
	}
	
	public void historyCheck(Customer c) {
		boolean found = false;
		if (!payments.isEmpty()) {
		synchronized ( payments ) {
		for (Payment p:payments ) {
			if (p.s == PaymentState.unpaid) {
				if (p.c.equals(c)) {
					p.s = PaymentState.unpaidRevisit;
					found = true;
				}
			}
		}//payments
		}//sync
		}
		if (!found)
			cleanCustomers.add(c);
		stateChanged();
		log.add(new LoggedEvent("Received historyCheck"));
	}
	
	public void paymentForDeferredPayment(double cash, Customer c ) {
		synchronized ( payments ) {
		for (Payment p:payments ) {
			if ( p.s == PaymentState.unpaidProcessing) {
				if (p.c.equals(c)) {
					p.s = PaymentState.unpaidPaid;
					p.cash = cash;
				}
			}
		}//payments
		}//sync
		stateChanged();
		log.add(new LoggedEvent("Received paymentForDeferredPayment"));
	}
	
	public void hereIsCheck( Check check, Market m ) {
		payments.add(new Payment (check, m, PaymentState.marketPending));
		stateChanged();
		log.add(new LoggedEvent("Received hereIsCheck"));
	}
	
	
	
	/* Scheduler */
	
	public boolean pickAndExecuteAnAction() {
		
		if (!cleanCustomers.isEmpty()) {
			clearCustomer(cleanCustomers.remove(0));
			return true;
		}
		
		
		synchronized ( checkOrders ) {
		for (CheckOrder o : checkOrders) {
			if (o.s == CheckOrderState.requested ) {
				deliverCheck(o);
				return true;
			}
		}//checkOrders
		}//sync
		
		synchronized ( payments ) {
		for (Payment p : payments) {
			if (p.s == PaymentState.pending) {
				processPayment(p);
				return true;
			}
		}//payments
		}//sync
		
		synchronized ( payments ) {
		for (Payment p : payments) {
			if (p.s == PaymentState.unpaidPending || p.s == PaymentState.unpaidPendingAgain) {
				payNextVisit(p);
				return true;
			}
		}//payments
		}//sync
		
		synchronized ( payments ) {
		for (Payment p:payments ) {
			if (p.s == PaymentState.unpaidRevisit) {
				requestDeferredPayment(p);
				return true;
			}
		}//payments
		}//sync
		
		synchronized ( payments ) {
		for (Payment p:payments ) {
			if (p.s == PaymentState.unpaidPaid) {
				processDeferredPayment(p);
				return true;
			}
		}//payments
		}//sync
		
		synchronized ( payments ) {
		for (Payment p:payments ) {
			if (p.s == PaymentState.marketPending) {
				makePaymentToMarket(p);
				return true;
			}
		}//payments
		}//sync
		
		
		
		
		return false;
	}

	
	/* Action */
	
	private void deliverCheck(CheckOrder o) {
		Check check = new Check();
		check.addItem(o.choice);
		print(o.w + ", here is check for " + o.c);
		o.w.hereIsCheck(check, o.c);
		checkOrders.remove(o);
	}
	
	private void processPayment(Payment p) {
		double change = p.cash - p.check.getTotal();
		print ("Thank you, come again. Change: " + change);
		p.s = PaymentState.paid;
		p.c.hereIsChange(change);
	}
	
	private void payNextVisit(Payment p) {
		print(p.c + ", You can pay next time of your visit");
		if (p.s == PaymentState.unpaidPendingAgain) {
			print(host + ", the customer hasn't paid yet, kick out " + p.c);
			host.customerClear(p.c, false);
		}
		p.s = PaymentState.unpaid;
		p.c.payNextTime();
	}
	
	private void requestDeferredPayment(Payment p) {
		p.s = PaymentState.unpaidProcessing;
		print(p.c + ", you have deferred payment to make");
		p.c.pleasePayDeferredPayment(p.check);
	}
	
	private void processDeferredPayment(Payment p) {
		double change =  p.cash - p.check.getTotal();
		print ("Thank you, you may now go back to your line. Change: " + change);
		p.s = PaymentState.paid;
		p.c.hereIsChange(change);
		host.customerClear(p.c, true);
	}
	
	private void clearCustomer (Customer c) {
		print (c + " is clean, over");
		host.customerClear(c, true);
	}
	
	private void makePaymentToMarket (Payment p) {
		if (restaurantBudget < p.check.getTotal() ) {
			print("hey, " + p.m +" Our restaurant is short, rain check please");
			p.m.iAmShort(p.check, this);
			payments.remove(p);
		}else {
			print("hey, " + p.m +" here is payment : " + p.check.getTotal());
			p.m.hereIsPayment(p.check, p.check.getTotal(), this);
			restaurantBudget -= p.check.getTotal();
			p.s = PaymentState.paidMarket; // not removing for the record
		}
	}

	/* Utilities */
	public CashierAgent(String name) {
		this.name = name;
	}
	public void setHost(Host host) {
		this.host = host;
	}
	public String getName() {
		return this.name;
	}
	public String toString() {
		return getName();
	}
	public void setRestaurantBudget( double bd) {
		this.restaurantBudget = bd;
	}

	
}

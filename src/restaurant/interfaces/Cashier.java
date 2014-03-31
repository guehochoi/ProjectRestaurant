package restaurant.interfaces;

import restaurant.Check;

public interface Cashier {

	/* Messages */
	
	public void produceCheck(Customer c, String choice, Waiter w) ;
	
	public void payment(Check check, double cash, Customer c) ;
	
	public void cannotPayBill(Check check, Customer c) ;
	
	public void historyCheck(Customer c) ;
	
	public void paymentForDeferredPayment(double cash, Customer c );
	
	public void hereIsCheck( Check check, Market m );
	
	
	/* Utilities */
	
	public void setHost(Host host) ;
	
	public String getName();
	
	public String toString();
	
}

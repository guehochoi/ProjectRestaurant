package restaurant.interfaces;

import restaurant.Check;


public interface Market {
	
	
	/* Messages */
	
	public void orderFor(String choice, int quantity);
	
	public void confirmation(boolean approval, String choice);
	
	public void hereIsPayment(Check check, double cash, Cashier c) ;
	
	public void iAmShort(Check check, Cashier c) ;
	
	/* utilities */
	
	public String getName();
	
	public String toString();
	
	public void setCook(Cook c);
	
}

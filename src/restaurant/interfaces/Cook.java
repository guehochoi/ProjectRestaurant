package restaurant.interfaces;


import restaurant.CookAgent.Order;

public interface Cook {
	
	
	/* Messages */
	
	public void hereIsOrder(Waiter w, String choice, int table);
	
	public void foodDone(Order o);
	
	public void weAreOutof(String choice, Market m);
	
	public void deliveryScheduled(String choice, int quantity, Market m);
	
	public void deliveryFor(String choice, Market m);
	
	public void goToWork();
	
	
	/* Utilities */
	
	public String getName();
	
	public String toString();
	
	public void addMarket(Market m);
	
	public void setHost(Host host);
	
	/* HACKS */
	
	public void gotNoChicken();
	
	

	
}

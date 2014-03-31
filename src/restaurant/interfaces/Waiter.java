package restaurant.interfaces;

import restaurant.Check;
import restaurant.gui.WaiterGui;

public interface Waiter {
	
	/* Messages */
	
	public void sitAtTable(Customer c, int table);
	
	public void readyToOrder(Customer c);
	
	public void hereIsMyChoice(Customer c, String choice);
	
	public void orderIsReady(String choice, int table);
	
	public void doneEating(Customer c);
	
	public void leaving(Customer c);
	
	public void outOf(String choice, int table);
	
	public void hereIsCheck(Check check, Customer c);
	
	
	public void msgAtDestination();

	public void msgWantBreak();
	
	public void msgBackToWork();
	
	/* Utilities */
	
	public void setBreakEnabled();
	
	public String getName();
	
	public String toString();
	
	public void setGui(WaiterGui gui);
	
	public WaiterGui getGui();
	
	public void setHost(Host host);
	
	public void setCook(Cook cook);
	
	public void setCashier(Cashier cashier);
	
}

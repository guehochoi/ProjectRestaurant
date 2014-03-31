package restaurant.interfaces;

import java.util.Collection;
import java.util.List;

import restaurant.gui.RestaurantGui;

public interface Host {

	/* Messages */

	public void msgIWantFood(Customer cust);
	
	public void tableIsFree(int t);
	
	public void readyToWork(Waiter w);
	
	public void wantToGoOnBreak(Waiter w);
	
	public void customerClear(Customer c, boolean clear);
	
	public void iAm(boolean staying, Customer c);
	
	public void takeCustomers();
	
	/* Utilities */
	
	public String getMaitreDName();

	public String getName();
	
	public String toString();

	public void setCashier(Cashier cashier);
	
	public List getWaitingCustomers();

	public Collection getTables();
	
	public void setRestGui(RestaurantGui gui);
	
	public void addTable();
	
	
}

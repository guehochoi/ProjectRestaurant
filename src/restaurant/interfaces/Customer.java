package restaurant.interfaces;

import restaurant.interfaces.Cashier;
import restaurant.Check;
import restaurant.interfaces.Host;
import restaurant.Menu;
import restaurant.interfaces.Waiter;
//import restaurant.CustomerAgent.AgentEvent;
import restaurant.gui.CustomerGui;

/**
 * A sample Customer interface built to unit test a CashierAgent.
 *
 * @author Monroe Ekilah
 *
 */
public interface Customer {
	
	/**
	 * @param total The cost according to the cashier
	 *
	 * Sent by the cashier prompting the customer's money after the customer has approached the cashier.
	 */
	//public abstract void HereIsYourTotal(double total);

	/**
	 * @param total change (if any) due to the customer
	 *
	 * Sent by the cashier to end the transaction between him and the customer. total will be >= 0 .
	 */
	//public abstract void HereIsYourChange(double total);


	/**
	 * @param remaining_cost how much money is owed
	 * Sent by the cashier if the customer does not pay enough for the bill (in lieu of sending {@link #HereIsYourChange(double)}
	 */
	//public abstract void YouOweUs(double remaining_cost);
	
	/*					MESSAGES					*/
	
	
	public abstract void gotHungry();

	public abstract void followMe(Waiter w, Menu m);

	public abstract void whatWouldYouLike();
	
	public abstract void hereIsYourFood();
	
	public abstract void pleaseOrderAgain(Menu menu);

	public abstract void hereIsCheck( Check c );
	
	public abstract void hereIsChange( double change );
	
	public abstract void payNextTime();
	
	public abstract void pleasePayDeferredPayment(Check check);
	
	public abstract void getOut();
	
	public abstract void restaurantIsFull();
	
	
	public abstract void msgAnimationFinishedGoToSeat();
	
	public abstract void msgAnimationFinishedLeaveRestaurant();
	
	public abstract void msgAtDest();
	
	
	
	/* 					Utilities 					*/

	public void setHost(Host host);
	
	public void setCash(double cash);
	
	public void setDecent(boolean isDecent);
	
	public String getCustomerName();
	
	public void setMoney(double cash);
	
	public void setCashier(Cashier cashier);
	
	public String getName();
	
	public void setLeavingIfFull();
	
	public int getHungerLevel();
	
	public void setHungerLevel(int hungerLevel);

	public String toString();

	public void setGui(CustomerGui g);

	public CustomerGui getGui();
	
}
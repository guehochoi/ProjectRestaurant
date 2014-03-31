package restaurant.test.mock;


import restaurant.Check;
import restaurant.Menu;
import restaurant.gui.CustomerGui;
import restaurant.interfaces.Cashier;
import restaurant.interfaces.Customer;
import restaurant.interfaces.Host;
import restaurant.interfaces.Waiter;

/**
 * A sample MockCustomer built to unit test a CashierAgent.
 *
 * @author Monroe Ekilah
 *
 */
public class MockCustomer extends Mock implements Customer {

	/**
	 * Reference to the Cashier under test that can be set by the unit test.
	 */
	public Cashier cashier;
	public EventLog log = new EventLog();

	public MockCustomer(String name) {
		super(name);

	}
	/*
	@Override
	public void HereIsYourTotal(double total) {
		log.add(new LoggedEvent("Received HereIsYourTotal from cashier. Total = "+ total));

		if(this.name.toLowerCase().contains("thief")){
			//test the non-normative scenario where the customer has no money if their name contains the string "theif"
			cashier.IAmShort(this, 0);

		}else if (this.name.toLowerCase().contains("rich")){
			//test the non-normative scenario where the customer overpays if their name contains the string "rich"
			cashier.HereIsMyPayment(this, Math.ceil(total));

		}else{
			//test the normative scenario
			cashier.HereIsMyPayment(this, total);
		}
	}

	@Override
	public void HereIsYourChange(double total) {
		log.add(new LoggedEvent("Received HereIsYourChange from cashier. Change = "+ total));
	}

	@Override
	public void YouOweUs(double remaining_cost) {
		log.add(new LoggedEvent("Received YouOweUs from cashier. Debt = "+ remaining_cost));
	}

	*/
	@Override
	public void gotHungry() {
		// TODO Auto-generated method stub
		log.add(new LoggedEvent("Received Got Hungry"));
	}

	@Override
	public void followMe(Waiter w, Menu m) {
		log.add(new LoggedEvent("Received follow me from waiter, " + w));
	}

	@Override
	public void whatWouldYouLike() {
		log.add(new LoggedEvent("Received whatWouldYouLike"));
	}

	@Override
	public void hereIsYourFood() {
		log.add(new LoggedEvent("Received hereIsYourFood"));
	}

	@Override
	public void pleaseOrderAgain(Menu menu) {
		log.add(new LoggedEvent("Received pleaseOrderAgain, with new menu"));
	}

	@Override
	public void hereIsCheck(Check c) {
		log.add(new LoggedEvent("rReceived hereIsCheck, checkTotal : " + c.getTotal()));
	}

	@Override
	public void hereIsChange(double change) {
		log.add(new LoggedEvent("Received hereIsChange, change: "+ change));
	}

	@Override
	public void payNextTime() {
		log.add(new LoggedEvent("Received payNextTime"));
	}

	@Override
	public void pleasePayDeferredPayment(Check check) {
		log.add(new LoggedEvent("Received pleasePayDeferredPayment, total: " + check.getTotal()));
	}

	@Override
	public void getOut() {
		log.add(new LoggedEvent("Received getOut"));
	}

	@Override
	public void restaurantIsFull() {
		log.add(new LoggedEvent("Received restaurantIsFull"));
	}

	@Override
	public void msgAnimationFinishedGoToSeat() {
		log.add(new LoggedEvent("Received msgAnimationFinishedGoToSeat"));
	}

	@Override
	public void msgAnimationFinishedLeaveRestaurant() {
		log.add(new LoggedEvent("Received msgAnimationFinishedLeaveRestaurant"));
	}

	@Override
	public void msgAtDest() {
		log.add(new LoggedEvent("Received msgAtDest"));
	}

	@Override
	public void setHost(Host host) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCash(double cash) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDecent(boolean isDecent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getCustomerName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMoney(double cash) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCashier(Cashier cashier) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLeavingIfFull() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getHungerLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setHungerLevel(int hungerLevel) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setGui(CustomerGui g) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CustomerGui getGui() {
		// TODO Auto-generated method stub
		return null;
	}

}

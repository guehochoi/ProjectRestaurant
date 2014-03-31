package restaurant.test.mock;

import restaurant.Check;
import restaurant.gui.WaiterGui;
import restaurant.interfaces.Cashier;
import restaurant.interfaces.Cook;
import restaurant.interfaces.Customer;
import restaurant.interfaces.Host;
import restaurant.interfaces.Waiter;

public class MockWaiter extends Mock implements Waiter {

	public EventLog log = new EventLog();
	
	public MockWaiter(String name) {
		super(name);
	}

	@Override
	public void sitAtTable(Customer c, int table) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void readyToOrder(Customer c) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hereIsMyChoice(Customer c, String choice) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void orderIsReady(String choice, int table) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doneEating(Customer c) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void leaving(Customer c) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void outOf(String choice, int table) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hereIsCheck(Check check, Customer c) {
		log.add(new LoggedEvent("Received hereIsCheck"));
	}

	@Override
	public void msgAtDestination() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgWantBreak() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgBackToWork() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBreakEnabled() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setGui(WaiterGui gui) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public WaiterGui getGui() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHost(Host host) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCook(Cook cook) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCashier(Cashier cashier) {
		// TODO Auto-generated method stub
		
	}

	
}

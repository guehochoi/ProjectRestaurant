package restaurant.test.mock;

import restaurant.Check;
import restaurant.interfaces.Cashier;
import restaurant.interfaces.Cook;
import restaurant.interfaces.Market;

public class MockMarket extends Mock implements Market{

	public EventLog log = new EventLog();
	public Cashier cashier;
	
	public MockMarket(String name) {
		super(name);
	}

	@Override
	public void orderFor(String choice, int quantity) {
		log.add(new LoggedEvent("Received orderFor " + choice + " " + quantity));
	}

	@Override
	public void confirmation(boolean approval, String choice) {
		log.add(new LoggedEvent("Received confirmation " + approval + " " + choice));
	}

	@Override
	public void hereIsPayment(Check check, double cash, Cashier c) {
		log.add(new LoggedEvent("Received hereIsPayment " + check.getTotal() + " " + cash));
	}

	@Override
	public void iAmShort(Check check, Cashier c) {
		log.add(new LoggedEvent("Received iAmShort " + check.getTotal()));
	}

	@Override
	public void setCook(Cook c) {
		// TODO Auto-generated method stub
		
	}

}

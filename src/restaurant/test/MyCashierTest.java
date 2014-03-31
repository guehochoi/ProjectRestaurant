package restaurant.test;

import restaurant.CashierAgent;
import restaurant.Check;
import restaurant.CashierAgent.PaymentState;
import restaurant.test.mock.MockCustomer;
import restaurant.test.mock.MockHost;
import restaurant.test.mock.MockMarket;
import restaurant.test.mock.MockWaiter;
import junit.framework.TestCase;

public class MyCashierTest extends TestCase {
	CashierAgent cashier;
	MockCustomer customer;
	MockWaiter waiter;
	MockHost host;
	MockMarket market;
	
	public void setUp() throws Exception{
		super.setUp();		
		cashier = new CashierAgent("cashier");		
		customer = new MockCustomer("mockcustomer");		
		waiter = new MockWaiter("mockwaiter");
		host = new MockHost("mockhost");
		market = new MockMarket("mockmarket");
		
		customer.cashier = cashier;
		waiter.setCashier(cashier);
		host.setCashier(cashier);
		cashier.host = host;
		market.cashier = cashier;
	}
	
	/**
	 * This is test of normative scenario
	 * 0: doneEating(Customer c1) [customer -> waiter]
	 * 1: produceCheck(Customer c1, String choice) [waiter -> cashier]
	 * 2: hereIsCheck(Check check) [cashier -> waiter]
	 * 3: hereIsCheck(Check check) [waiter -> customer]
	 * 4: payment(Check check, double cash, Customer c1) [customer->cashier]
	 * 5: hereIsChange(double change) [cashier->customer]
	 */
	public void testNormativeScenario() {
		
		// Test 1: message reception to action for interaction with waiter
		//check preconditions
		assertEquals("Cashier should have 0 checkOrders. It doesn't.",cashier.checkOrders.size(), 0);
		assertEquals("Cashier should have 0 payments in it. It doesn't.", cashier.payments.size(), 0);
		assertEquals("Cashier should have 0 cleanCustomers in it. It doesn't.", cashier.cleanCustomers.size(), 0);

		//before receiving message from waiter
		assertFalse("Cashier's scheduler should have returned false, but didn't.", cashier.pickAndExecuteAnAction());
		assertEquals(
				"MockWaiter should have an empty event log after the Cashier's scheduler is called for the first time. Instead, the MockWaiter's event log reads: "
						+ waiter.log.toString(), 0, waiter.log.size());
		assertEquals(
				"MockCustomer should have an empty event log after the Cashier's scheduler is called for the first time. Instead, the MockCustomer's event log reads: "
						+ customer.log.toString(), 0, customer.log.size());
		//receives message from waiter
		cashier.produceCheck(customer, "Chicken", waiter);
		//check conditions
		assertTrue("Cashier should have logged \"Received produceCheck\" but didn't. His log reads instead: " 
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received produceCheck"));
		assertEquals("Cashier should have 1 checkOrders. It doesn't.", cashier.checkOrders.size(), 1);
		assertTrue("Cashier's scheduler should have returned true (deliverCheck), but it didn't", cashier.pickAndExecuteAnAction());
		//check the result of the message produceCheck
		assertEquals("Cashier checkOrders no longer has the order above", cashier.checkOrders.size(), 0);
		assertTrue("Waiter should have logged \"Received hereIsCheck\" but didn't. His log reads instead: "
				+ waiter.log.getLastLoggedEvent().toString(), waiter.log.containsString("Received hereIsCheck"));
		assertFalse("Cashier's scheduler should have returned false (no actions left to do), but didn't.", 
				cashier.pickAndExecuteAnAction());
		
		// Test 2: message reception to action for interaction with customer
		assertEquals("Cashier should have 0 checkOrders. It doesn't.",cashier.checkOrders.size(), 0);
		assertEquals("Cashier should have 0 payments in it. It doesn't.", cashier.payments.size(), 0);
		assertEquals("Cashier should have 0 cleanCustomers in it. It doesn't.", cashier.cleanCustomers.size(), 0);
		//before receiving message from customer 
		assertFalse("Cashier's scheduler should have returned false. It didn't.", cashier.pickAndExecuteAnAction());
		assertEquals(
				"MockCustomer should have an empty event log after the Cashier's scheduler is called for the first time. Instead, the MockCustomer's event log reads: "
						+ customer.log.toString(), 0, customer.log.size());
		//receives message from customer
		Check check = new Check();
		check.addItem("Chicken");
		cashier.payment(check, check.getTotal(), customer);
		//check conditions
		assertTrue("Cashier should have logged \"Received payment\" but didn't. Instead, " 
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received payment"));
		assertEquals("Cashier should have 1 in payments, but it didn't", cashier.payments.size(), 1);
		assertTrue("CashierPayment should contain pending state, but it didn't", cashier.payments.get(0).s == PaymentState.pending);
		assertTrue("CashierPayment should have " + check.getTotal() + " for cash. But it didn't", cashier.payments.get(0).cash == check.getTotal());
		assertTrue("Cashier's scheduler should have returned true (processPayment). But it didn't", cashier.pickAndExecuteAnAction());
		//check the result of the message payment
		assertTrue("CashierPayment should contain paid state, but it didn't", cashier.payments.get(0).s == PaymentState.paid);
		assertEquals(
				"MockCustomer should have 1 event log after the cashier's scheduler. ", 1, customer.log.size());
		assertTrue("MockCustomer should have logged \"Received hereIsChange, change: \". But it didn't. Instead, "
				+ customer.log.getLastLoggedEvent().toString(), customer.log.containsString("Received hereIsChange, change: " + Double.toString(check.getTotal()-check.getTotal())));
		assertFalse("Cashier's scheduler should have returned false (no actions left to do), but didn't.", 
				cashier.pickAndExecuteAnAction());
	}//end of Normative Scenario
	
	/**
	 * Normative scenario, but there is a change.
	 * My code generally makes payment with right amount.
	 * However, that's not always the case, therefore, the test is necessary
	 * */
	public void testNormativeScenarioWithChange() {
		// Test : message reception to action for interaction with customer
		assertEquals("Cashier should have 0 checkOrders. It doesn't.",cashier.checkOrders.size(), 0);
		assertEquals("Cashier should have 0 payments in it. It doesn't.", cashier.payments.size(), 0);
		assertEquals("Cashier should have 0 cleanCustomers in it. It doesn't.", cashier.cleanCustomers.size(), 0);
		//before receiving message from customer 
		assertFalse("Cashier's scheduler should have returned false. It didn't.", cashier.pickAndExecuteAnAction());
		assertEquals(
				"MockCustomer should have an empty event log after the Cashier's scheduler is called for the first time. Instead, the MockCustomer's event log reads: "
						+ customer.log.toString(), 0, customer.log.size());
		//receives message from customer
		Check check = new Check();
		check.addItem("Chicken");
		cashier.payment(check, 100, customer);
		//check conditions
		assertTrue("Cashier should have logged \"Received payment\" but didn't. Instead, " 
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received payment"));
		assertEquals("Cashier should have 1 in payments, but it didn't", cashier.payments.size(), 1);
		assertTrue("CashierPayment should contain pending state, but it didn't", cashier.payments.get(0).s == PaymentState.pending);
		assertTrue("CashierPayment should have " + 100 + " for cash. But it didn't", cashier.payments.get(0).cash == 100);
		assertTrue("Cashier's scheduler should have returned true (processPayment). But it didn't", cashier.pickAndExecuteAnAction());
		//check the result of the message payment
		assertTrue("CashierPayment should contain paid state, but it didn't", cashier.payments.get(0).s == PaymentState.paid);
		assertEquals(
				"MockCustomer should have 1 event log after the cashier's scheduler. ", 1, customer.log.size());
		assertTrue("MockCustomer should have logged \"Received hereIsChange, change: \". But it didn't. Instead, "
				+ customer.log.getLastLoggedEvent().toString(), customer.log.containsString("Received hereIsChange, change: " + Double.toString(100-check.getTotal())));
		assertFalse("Cashier's scheduler should have returned false (no actions left to do), but didn't.", 
				cashier.pickAndExecuteAnAction());
	}//end of Normative Scenario with change
	
	public void testNotEnoughMoney() {
		//check preconditions
		assertEquals("Cashier should have 0 checkOrders. It doesn't.",cashier.checkOrders.size(), 0);
		assertEquals("Cashier should have 0 payments in it. It doesn't.", cashier.payments.size(), 0);
		assertEquals("Cashier should have 0 cleanCustomers in it. It doesn't.", cashier.cleanCustomers.size(), 0);

		//before receiving message from waiter
		assertFalse("Cashier's scheduler should have returned false, but didn't.", cashier.pickAndExecuteAnAction());
		assertEquals(
				"MockWaiter should have an empty event log after the Cashier's scheduler is called for the first time. Instead, the MockWaiter's event log reads: "
						+ waiter.log.toString(), 0, waiter.log.size());
		assertEquals(
				"MockCustomer should have an empty event log after the Cashier's scheduler is called for the first time. Instead, the MockCustomer's event log reads: "
						+ customer.log.toString(), 0, customer.log.size());
		//receives message from waiter
		cashier.produceCheck(customer, "Chicken", waiter);
		//check conditions
		assertTrue("Cashier should have logged \"Received produceCheck\" but didn't. His log reads instead: " 
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received produceCheck"));
		assertEquals("Cashier should have 1 checkOrders. It doesn't.", cashier.checkOrders.size(), 1);
		assertTrue("Cashier's scheduler should have returned true (deliverCheck), but it didn't", cashier.pickAndExecuteAnAction());
		//check the result of the message produceCheck
		assertEquals("Cashier checkOrders no longer has the order above", cashier.checkOrders.size(), 0);
		assertTrue("Waiter should have logged \"Received hereIsCheck\" but didn't. His log reads instead: "
				+ waiter.log.getLastLoggedEvent().toString(), waiter.log.containsString("Received hereIsCheck"));
		assertFalse("Cashier's scheduler should have returned false (no actions left to do), but didn't.", 
				cashier.pickAndExecuteAnAction());
		
		// Test 2: message reception to action for interaction with customer
		assertEquals("Cashier should have 0 checkOrders. It doesn't.",cashier.checkOrders.size(), 0);
		assertEquals("Cashier should have 0 payments in it. It doesn't.", cashier.payments.size(), 0);
		assertEquals("Cashier should have 0 cleanCustomers in it. It doesn't.", cashier.cleanCustomers.size(), 0);
		//before receiving message from customer 
		assertFalse("Cashier's scheduler should have returned false. It didn't.", cashier.pickAndExecuteAnAction());
		assertEquals(
				"MockCustomer should have an empty event log after the Cashier's scheduler is called for the first time. Instead, the MockCustomer's event log reads: "
						+ customer.log.toString(), 0, customer.log.size());
		//receives message from customer
		Check check = new Check();
		check.addItem("Chicken");
		cashier.cannotPayBill(check, customer);
		assertTrue("Cashier should have logged \"Received cannotPayBill\", but it didn't. Instead, "
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received cannotPayBill"));
		assertEquals("Cashier should have 1 in payments, but it didn't", cashier.payments.size(), 1);
		assertTrue("CashierPayment should contain unpaidPending state, but it didn't", cashier.payments.get(0).s == PaymentState.unpaidPending);
		assertTrue("Cashier's scheduler should return true (payNextVisit). But it didn't. ", cashier.pickAndExecuteAnAction());
		//check the result of the message cannotPayBill
		assertFalse("CashierPayment should not contain unpaidPending state anymore, but it does", cashier.payments.get(0).s == PaymentState.unpaidPending);
		assertTrue("CashierPayment should contain unpaid state, but it didn't", cashier.payments.get(0).s == PaymentState.unpaid);
		assertEquals("Cashier should only have one in the payments but it didn't", cashier.payments.size(), 1);
		assertTrue("Customer should have logged \"Received payNextTime\" but it didn't. Instead "
				+ customer.log.getLastLoggedEvent().toString(), customer.log.containsString("Received payNextTime"));
		assertFalse("Cashier's scheduler should return false now", cashier.pickAndExecuteAnAction());
		
	}//end of Non-Normative: customer has not enough money to pay bill (but ate)
	
	/**
	 * The customer who has not paid the bill is back to the restaurant.
	 * Note that waiter's case is tested enough and we do not need the interaction with waiter in this case
	 * host asks for checking the customer's background to cashier. This is case where customer is not clean.
	 */
	public void testNotPaidBack() {
		assertEquals("Cashier should have 0 checkOrders. It doesn't.",cashier.checkOrders.size(), 0);
		assertEquals("Cashier should have 0 payments in it. It doesn't.", cashier.payments.size(), 0);
		assertEquals("Cashier should have 0 cleanCustomers in it. It doesn't.", cashier.cleanCustomers.size(), 0);
		//before receiving message from customer 
		assertFalse("Cashier's scheduler should have returned false. It didn't.", cashier.pickAndExecuteAnAction());
		assertEquals(
				"MockCustomer should have an empty event log after the Cashier's scheduler is called for the first time. Instead, the MockCustomer's event log reads: "
						+ customer.log.toString(), 0, customer.log.size());
		//receives message from customer
		Check check = new Check();
		check.addItem("Chicken");
		cashier.cannotPayBill(check, customer);
		assertTrue("Cashier should have logged \"Received cannotPayBill\", but it didn't. Instead, "
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received cannotPayBill"));
		assertEquals("Cashier should have 1 in payments, but it didn't", cashier.payments.size(), 1);
		assertTrue("CashierPayment should contain unpaidPending state, but it didn't", cashier.payments.get(0).s == PaymentState.unpaidPending);
		assertTrue("Cashier's scheduler should return true (payNextVisit). But it didn't. ", cashier.pickAndExecuteAnAction());
		//check the result of the message cannotPayBill
		assertFalse("CashierPayment should not contain unpaidPending state anymore, but it does", cashier.payments.get(0).s == PaymentState.unpaidPending);
		assertTrue("CashierPayment should contain unpaid state, but it didn't", cashier.payments.get(0).s == PaymentState.unpaid);
		assertEquals("Cashier should only have one in the payments but it didn't", cashier.payments.size(), 1);
		assertTrue("Customer should have logged \"Received payNextTime\" but it didn't. Instead "
				+ customer.log.getLastLoggedEvent().toString(), customer.log.containsString("Received payNextTime"));
		assertFalse("Cashier's scheduler should return false now", cashier.pickAndExecuteAnAction());
		
		//message from Host, to check customer
		cashier.historyCheck(customer);
		assertTrue("Cashier should have logged \"Received historyCheck\", but it didn't. Instead, "
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received historyCheck"));
		assertFalse("Cashier should not contain unpaid anymore, but it does", cashier.payments.get(0).s == PaymentState.unpaid);
		assertTrue("CashierPayment should contain unpaidRevisit state, but it didn't", cashier.payments.get(0).s == PaymentState.unpaidRevisit);
		assertTrue("Cashier's scheduler should return true (requestDeferredPayment), but it didn't", cashier.pickAndExecuteAnAction());
		//check result of the action requestDeferredPayment
		assertFalse("Cashier should not contain unpaidRevisit state anymore, but it does", cashier.payments.get(0).s == PaymentState.unpaidRevisit);
		assertTrue("CashierPayment should contain unpaidProcessing state, but it didn't", cashier.payments.get(0).s == PaymentState.unpaidProcessing);
		assertEquals("Cashier should still have only one payments, but it didn't", cashier.payments.size(), 1);
		assertTrue("Customer should have logged \"Received pleasePayDeferredPayment, total: "+ cashier.payments.get(0).check.getTotal() +"\", but it didn't. Instead, "
				+ customer.log.getLastLoggedEvent().toString(), customer.log.containsString("Received pleasePayDeferredPayment, total: " + cashier.payments.get(0).check.getTotal()));
		assertFalse("Cashier's scheduler should return false now", cashier.pickAndExecuteAnAction());
		//message from customer paymentforDeferredPayment
		cashier.paymentForDeferredPayment(cashier.payments.get(0).check.getTotal(), customer);
		assertTrue("Cashier should have logged \"Received paymentForDeferredPayment\" but it didn't. Instead, "
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received paymentForDeferredPayment"));
		assertFalse("CasierPayment should have no more unpaidProcessing, but it does", cashier.payments.get(0).s == PaymentState.unpaidProcessing);
		assertEquals("CashierPayment should have unpaidPiad state, but it didn't", cashier.payments.get(0).s, PaymentState.unpaidPaid);
		assertTrue("Cashier's scheduler should return true (processDeferredPayment)", cashier.pickAndExecuteAnAction());
		//check result of action
		assertFalse("CasierPayment should have no more unpaidPaid, but it does", cashier.payments.get(0).s == PaymentState.unpaidPaid);
		assertEquals("CashierPayment should have paid state, but it didn't", cashier.payments.get(0).s, PaymentState.paid);
		assertTrue("Customer should have logged \"Received hereIsChange, change: \", but it didn't. Instead, "
				+ customer.log.getLastLoggedEvent().toString(), customer.log.containsString("Received hereIsChange, change: "));
		assertTrue("Host should have logged \"Receive customerClear\", but it didn't. Instead,"
				+ host.log.getLastLoggedEvent().toString(), host.log.containsString("Receive customerClear"));
		assertEquals("CashierPayment still has to have one payment in payments, but it didn't", cashier.payments.size(), 1);
		assertFalse("Cashier's scheduler should return false", cashier.pickAndExecuteAnAction());
		
	}//end of Non-Normative: customer didn't pay came back, paid bill.
	
	/**
	 * Milestone v2.2A 
	 * One order, fulfilled by the market, bill paid in full
	 */
	public void testNormativePaymentFromMarket() {
		//check preconditions
		assertEquals("Cashier's payments should have nothing in it, but it does. ", cashier.payments.size(), 0);
		assertFalse("Cashier's scheduler should return false, but it returns true", cashier.pickAndExecuteAnAction());
		assertEquals("Cashier should have no log but it does", cashier.log.size(), 0);
		assertEquals("MockMarket should have no log but it does", market.log.size(), 0);
		//message sent from market
		Check check = new Check();
		check.addItem("Beef", 20); 
		// Beef price = 12.99, quantity = 20
		// Therefore, 20 * 12.99 + tax = 281.88
		cashier.hereIsCheck(check, market);
		assertTrue("Cashier should have logged \"Received hereIsCheck\", but it didn't. Instead, "
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received hereIsCheck"));
		assertEquals("CashierPayment should have one payment in the payments, but it doesn't", cashier.payments.size(), 1);
		assertTrue("CashierPayment should have the same check as passed, but it doesn't", cashier.payments.get(0).check.equals(check));
		assertEquals("CashierPayment should have the same market as passed, but it doesn't", cashier.payments.get(0).m, market);
		assertEquals("CashierPayment should have marketPending state, but it doesn't", cashier.payments.get(0).s, PaymentState.marketPending);
		cashier.restaurantBudget = 10000; // enough money to pay bill
		double budgetCopy = cashier.restaurantBudget;
		assertTrue("Cashier's scheduler should return true (makePaymentToMarket), but it returns false", cashier.pickAndExecuteAnAction());
		//check the result of action
		assertTrue("Market should have logged \"Received hereIsPayment " + Double.toString(check.getTotal()) + " " + Double.toString(check.getTotal())+ ", but it didn't. Instead, "+
				market.log.getLastLoggedEvent().toString(), market.log.containsString("Received hereIsPayment " + Double.toString(check.getTotal()) + " " + Double.toString(check.getTotal())));
		assertEquals("Cashier should have restaurant budget: " + Double.toString(budgetCopy - check.getTotal())
				+ ", but it doesn't. ", cashier.restaurantBudget, budgetCopy-check.getTotal());
		assertEquals("CashierPayment should have paidMarket state, but it didn't", cashier.payments.get(0).s, PaymentState.paidMarket);
		assertFalse("Cashier's scheduler should return fasle", cashier.pickAndExecuteAnAction());
		
	}//end of normative scenario paying market
	
	/**
	 * Non-norm: market does not have enough money to pay bill
	 * Extra credit scenario
	 */
	public void testCashierShort() {
		//check preconditions
		assertEquals("Cashier's payments should have nothing in it, but it does. ", cashier.payments.size(), 0);
		assertFalse("Cashier's scheduler should return false, but it returns true", cashier.pickAndExecuteAnAction());
		assertEquals("Cashier should have no log but it does", cashier.log.size(), 0);
		assertEquals("MockMarket should have no log but it does", market.log.size(), 0);
		//message sent from market
		Check check = new Check();
		check.addItem("Beef", 20); 
		// Beef price = 12.99, quantity = 20
		// Therefore, 20 * 12.99 + tax = 281.88
		cashier.hereIsCheck(check, market);
		assertTrue("Cashier should have logged \"Received hereIsCheck\", but it didn't. Instead, "
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received hereIsCheck"));
		assertEquals("CashierPayment should have one payment in the payments, but it doesn't", cashier.payments.size(), 1);
		assertTrue("CashierPayment should have the same check as passed, but it doesn't", cashier.payments.get(0).check.equals(check));
		assertEquals("CashierPayment should have the same market as passed, but it doesn't", cashier.payments.get(0).m, market);
		assertEquals("CashierPayment should have marketPending state, but it doesn't", cashier.payments.get(0).s, PaymentState.marketPending);
		cashier.restaurantBudget = 0; // NOT enough money to pay bill
		assertTrue("Cashier's scheduler should return true (makePaymentToMarket), but it returns false", cashier.pickAndExecuteAnAction());
		//check the result of action
		assertTrue("Market should have logged \"Received iAmShort " + Double.toString(check.getTotal()) + ", but it didn't. Instead, "
				+ market.log.getLastLoggedEvent().toString(), market.log.containsString("Received iAmShort " + Double.toString(check.getTotal())));
		assertFalse("Cashier's scheduler should return false, but it didn't", cashier.pickAndExecuteAnAction());
		assertEquals("Cashier should have no payment in payments, but it does", cashier.payments.size(), 0);
		//it is necessary for market to have another order in order to charge the interest
		Check check2 = new Check();
		check2.addItem("Duck", 10);
		check2.appendCheckWithInterest(check, 0.5); // this is expected total with interest charged
		//message from market
		cashier.hereIsCheck(check2, market);
		assertTrue("Cashier should have logged \"Received hereIsCheck\", but it didn't. Instead, "
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received hereIsCheck"));
		assertEquals("Cashier should have one payment in payments, but it doesn't", cashier.payments.size(), 1);
		assertTrue("CashierPayment should have the same check as passed, but it doesn't", cashier.payments.get(0).check.equals(check2));
		assertEquals("CashierPayment should have the same market as passed, but it doesn't", cashier.payments.get(0).m, market);
		assertEquals("CashierPayment should have marketPending state, but it doesn't", cashier.payments.get(0).s, PaymentState.marketPending);
		cashier.restaurantBudget = 10000; //enough money to pay bill
		double budgetCopy = cashier.restaurantBudget;
		assertTrue("Cashier's scheduler should return true (makePaymentToMarket), but it returns false", cashier.pickAndExecuteAnAction());
		//check the result of action
		assertTrue("Market should have logged \"Received hereIsPayment " + Double.toString(check2.getTotal()) + " " + Double.toString(check2.getTotal())+ ", but it didn't. Instead, "+
				market.log.getLastLoggedEvent().toString(), market.log.containsString("Received hereIsPayment " + Double.toString(check2.getTotal()) + " " + Double.toString(check2.getTotal())));
		assertEquals("Cashier should have restaurant budget: " + Double.toString(budgetCopy - check2.getTotal())
				+ ", but it doesn't. ", cashier.restaurantBudget, budgetCopy-check2.getTotal());
		assertEquals("CashierPayment should have paidMarket state, but it didn't", cashier.payments.get(0).s, PaymentState.paidMarket);
		assertFalse("Cashier's scheduler should return fasle", cashier.pickAndExecuteAnAction());
		
	}//end of non-normative scenario: cashier not enough money
	
	/**
	 * One order, fulfilled by TWO markets, 2 bills paid in full.
	 * Normative scenario, but we order from two markets and therefore we
	 * have two markets to pay bill
	 */
	public void testNormativeTwoMarkets() {
		Check check = new Check();
		check.addItem("Turkey", 19);
		Check check2 = new Check();
		check2.addItem("Turkey", 1);
		MockMarket market2 = new MockMarket("mockmarket2");
		market2.cashier = cashier;
		
		//check preconditions
		assertEquals("Cashier's payments should have nothing in it, but it does. ", cashier.payments.size(), 0);
		assertFalse("Cashier's scheduler should return false, but it returns true", cashier.pickAndExecuteAnAction());
		assertEquals("Cashier should have no log but it does", cashier.log.size(), 0);
		assertEquals("MockMarket should have no log but it does", market.log.size(), 0);
		assertEquals("MockMarket2 should have no log but it does", market2.log.size(), 0);
		//message reception from first market
		cashier.hereIsCheck(check, market);
		assertTrue("Cashier should have logged \"Received hereIsCheck\", but it didn't. Instead, "
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received hereIsCheck"));
		assertEquals("CashierPayment should have one payment in the payments, but it doesn't", cashier.payments.size(), 1);
		assertTrue("CashierPayment should have the same check as passed, but it doesn't", cashier.payments.get(0).check.equals(check));
		assertEquals("CashierPayment should have the same market as passed, but it doesn't", cashier.payments.get(0).m, market);
		assertEquals("CashierPayment should have marketPending state, but it doesn't", cashier.payments.get(0).s, PaymentState.marketPending);
		//message reception from second market
		cashier.hereIsCheck(check2, market2);
		assertTrue("Cashier should have logged \"Received hereIsCheck\", but it didn't. Instead, "
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received hereIsCheck"));
		assertEquals("CashierPayment should have two payment in the payments, but it doesn't", cashier.payments.size(), 2);
		assertTrue("CashierPayment should have the same check2 as passed, but it doesn't", cashier.payments.get(1).check.equals(check2));
		assertEquals("CashierPayment should have the same market2 as passed, but it doesn't", cashier.payments.get(1).m, market2);
		assertEquals("CashierPayment should have marketPending state, but it doesn't", cashier.payments.get(1).s, PaymentState.marketPending);
		assertFalse("CashierPayment shouldn't have overwritten the market's object of another, but it did", cashier.payments.get(1).m.equals(market));
		assertFalse("CashierPayment shouldn't have overwritten the market's object of another, but it did", cashier.payments.get(0).m.equals(market2));
		cashier.restaurantBudget = 10000; // enough money to pay bill
		double budgetCopy = cashier.restaurantBudget;
		assertTrue("Cashier's scheduler should return true (makePaymentToMarket), but it returns false", cashier.pickAndExecuteAnAction());
		//check the result of the action
		assertFalse("Market should NOT have logged \"Received hereIsPayment " + Double.toString(check2.getTotal()) + " " + Double.toString(check2.getTotal())+ " because first one in the payments list should be dealt first, but it did. Instead, "+
				market.log.getLastLoggedEvent().toString(), market.log.containsString("Received hereIsPayment " + Double.toString(check2.getTotal()) + " " + Double.toString(check2.getTotal())));
		assertTrue("Market should have logged \"Received hereIsPayment " + Double.toString(check.getTotal()) + " " + Double.toString(check.getTotal())+ ", but it didn't. Instead, "+
				market.log.getLastLoggedEvent().toString(), market.log.containsString("Received hereIsPayment " + Double.toString(check.getTotal()) + " " + Double.toString(check.getTotal())));
		assertEquals("Cashier should have restaurant budget: " + Double.toString(budgetCopy - check.getTotal())
				+ ", but it doesn't. ", cashier.restaurantBudget, budgetCopy-check.getTotal());
		assertTrue("Cashier's scheduler should return true (makePaymentToMarket) : notice there is another market to pay, second market", cashier.pickAndExecuteAnAction());
		assertEquals("CashierPayment should have paidMarket state, but it didn't", cashier.payments.get(1).s, PaymentState.paidMarket);
		assertTrue("Market should have logged \"Received hereIsPayment " + Double.toString(check2.getTotal()) + " " + Double.toString(check2.getTotal())+ ", but it didn't. Instead, "+
				market2.log.getLastLoggedEvent().toString(), market2.log.containsString("Received hereIsPayment " + Double.toString(check2.getTotal()) + " " + Double.toString(check2.getTotal())));
		assertEquals("Cashier should have restaurant budget: " + Double.toString(budgetCopy - check.getTotal()- check2.getTotal())
				+ ", but it doesn't. ", cashier.restaurantBudget, budgetCopy-check.getTotal()- check2.getTotal());
		assertEquals("CashierPayment should still have two payment in the payments, but it doesn't", cashier.payments.size(), 2);
		assertFalse("Cashier's scheduler should return false, but it didn't", cashier.pickAndExecuteAnAction());
		
	}//end of fulfilling by two markets scenario.
	

}


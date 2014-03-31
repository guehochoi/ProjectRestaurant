package restaurant;

import restaurant.gui.CustomerGui;

import restaurant.interfaces.*;
import restaurant.gui.RestaurantGui;
import agent.Agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

/**
 * Restaurant customer agent.
 */
public class CustomerAgent extends Agent implements Customer {
	private String name;
	private int hungerLevel = 5;        // determines length of meal
	Timer timer = new Timer();
	private CustomerGui customerGui;
	//private int seatnumber;
	
	// agent correspondents
	private Host host;
	private Waiter waiter;
	private Cashier cashier;
	
	Check check;
	double cash = 100;	//All customers starts with 100 cash
	private Menu menu;

	private Semaphore atDest = new Semaphore(0, true);

	public enum AgentState
	{DoingNothing, WaitingInRestaurant, BeingSeated, Seated, Eating, DoneEating, Leaving, calledWaiter, ordered
		,checkRequested, paying, wantChange, payingDeferred, deciding
	};
	private AgentState state = AgentState.DoingNothing;//The start state

	public enum AgentEvent 
	{none, gotHungry, followWaiter, seated, doneEating, doneLeaving, waiterCame, foodCame,
		reorderRequested, checkCame, paid, changeCame, deferredPaymentRequested, kickedOut, restaurantFull
	};
	AgentEvent event = AgentEvent.none;
	List<AgentEvent> events = new ArrayList<AgentEvent>();
	
	private boolean stayIfFull = true;
	private boolean isDecent = true;
	

	/* Messages */

	public void gotHungry() {//from animation
		print("I'm hungry");
		events.add(AgentEvent.gotHungry);
		stateChanged();
	}

	public void followMe(Waiter w, Menu m) {
		waiter = w;
		menu = m;
		events.add(AgentEvent.followWaiter);
		stateChanged();
	}

	public void whatWouldYouLike() {
		events.add(AgentEvent.waiterCame);
		stateChanged();
	}
	
	public void hereIsYourFood() {
		events.add(AgentEvent.foodCame);
		stateChanged();
	}
	
	public void pleaseOrderAgain(Menu menu) {
		this.menu = menu;
		events.add(AgentEvent.reorderRequested);
		stateChanged();
		
	}

	public void hereIsCheck( Check c ) {
		this.check = c;
		events.add(AgentEvent.checkCame);
		stateChanged();
	}
	
	public void hereIsChange( double change ) {
		this.cash += change;
		events.add(AgentEvent.paid);
		stateChanged();
	}
	public void payNextTime() {
		events.add(AgentEvent.paid);
		stateChanged();
	}
	public void pleasePayDeferredPayment(Check check) {
		this.check = check;
		events.add(AgentEvent.deferredPaymentRequested);
		stateChanged();
	}
	public void getOut(){
		events.add(AgentEvent.kickedOut);
		stateChanged();
	}
	public void restaurantIsFull() {
		events.add(AgentEvent.restaurantFull);
		stateChanged();
	}
	
	// Implementation detail, not documented
	public void msgAnimationFinishedGoToSeat() {
		//from animation
		events.add(AgentEvent.seated);
		stateChanged();
	}
	// Implementation detail, not documented
	public void msgAnimationFinishedLeaveRestaurant() {
		//from animation
		events.add(AgentEvent.doneLeaving);
		stateChanged();
	}
	public void msgAtDest() {
		atDest.release();
		stateChanged();
	}
	
	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		//	CustomerAgent is a finite state machine
		
		// Get the first thing in the event list
		if (events.isEmpty())
			return false;
		event = events.remove(0);
		
		if (state == AgentState.DoingNothing && event == AgentEvent.gotHungry ){
			state = AgentState.WaitingInRestaurant;
			goToRestaurant();
			return true;
		}
		if (state == AgentState.WaitingInRestaurant && event == AgentEvent.deferredPaymentRequested ){
			state = AgentState.payingDeferred;
			payDeferred();
		}
		if (state == AgentState.payingDeferred && event == AgentEvent.kickedOut ){
			state = AgentState.Leaving;
			leaveRestaurant();
		}
		if (state == AgentState.WaitingInRestaurant && event == AgentEvent.restaurantFull) {
			state = AgentState.deciding;
			stayOrLeave();
		}
		if (state == AgentState.WaitingInRestaurant && event == AgentEvent.followWaiter ){
			state = AgentState.BeingSeated;
			SitDown();
			return true;
		}
		if (state == AgentState.BeingSeated && event == AgentEvent.seated){
			state = AgentState.calledWaiter;
			callWaiter();
			return true;
		}
		if (state == AgentState.calledWaiter && event == AgentEvent.waiterCame){
			state = AgentState.ordered;
			orderFood();
			return true;
		}
		if (state == AgentState.ordered && event == AgentEvent.foodCame){
			state = AgentState.Eating;
			EatFood();
			return true;
		}
		if (state == AgentState.ordered && event == AgentEvent.reorderRequested){
			state = AgentState.calledWaiter; // notice the reuse of state & event
			callWaiter();
			return true;
		}
		
		if (state == AgentState.Eating && event == AgentEvent.doneEating){
			state = AgentState.checkRequested;
			requestCheck();
			return true;
		}
		if (state == AgentState.checkRequested && event == AgentEvent.checkCame){
			state = AgentState.paying;
			pay();
			return true;
		}
		if (state == AgentState.paying && event == AgentEvent.paid){
			state = AgentState.Leaving;
			leaveTable();
			return true;
		}
		if (state == AgentState.Leaving && event == AgentEvent.doneLeaving){
			state = AgentState.DoingNothing;
			//no action
			return true;
		}
		return false;
	}

	// Actions

	private void goToRestaurant() {
		Do("Going to "
				+ "restaurant");
		customerGui.DoGoToRestaurant();
		try {
			atDest.acquire();
		}catch(InterruptedException ex) {
			ex.printStackTrace();
		}
		host.msgIWantFood(this);//send our instance, so he can respond to us
	}

	private void SitDown() {
		Do("Being seated.");
		customerGui.DoGoToSeat();
	}
	
	private void callWaiter() {
		print(waiter + ", I am ready to order");
		waiter.readyToOrder(this);
		/*
		final CustomerAgent cCopy = this; //TODO: Change this
		timer.schedule(new TimerTask(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				print(waiter + ", I am ready to order");
				waiter.readyToOrder(cCopy);
			}
			
		}, 10000); // This has to be changed, now it is just ticking the timer
					// to send a readyToOrder to waiter 
					// but waiter's seatCustomer waits for the semaphore
					// therefore we need timer for now
		 */
		
	}
	private void orderFood() {
		String choice = menu.getRandom();
		
		if (isDecent) {
			choice = menu.getRandomAffordable(cash);
			if (choice == null) {
				print ("Sigh ... I cannot afford anything..");
				state = AgentState.Leaving;
				leaveTable();
				return;
			}
		}
		
		// Debug use:////////////////////////////////////////////////////
			for (Menu.Item item : menu.availableItems) {
				if (getName().equalsIgnoreCase(item.type.toString())) {
					choice = item.type.toString();
				}
			}
		/////////////////////////////////////////////////////////////////
		print("here is my choice " + choice);
		customerGui.setString(choice); //animation to show the string
		waiter.hereIsMyChoice(this, choice);
	}
	private void EatFood() {
		Do("Eating Food");
		customerGui.setString("");
		//This next complicated line creates and starts a timer thread.
		//We schedule a deadline of getHungerLevel()*1000 milliseconds.
		//When that time elapses, it will call back to the run routine
		//located in the anonymous class created right there inline:
		//TimerTask is an interface that we implement right there inline.
		//Since Java does not all us to pass functions, only objects.
		//So, we use Java syntactic mechanism to create an
		//anonymous inner class that has the public method run() in it.
		timer.schedule(new TimerTask() {
			Object cookie = 1;
			public void run() {
				print("Done eating, cookie=" + cookie);
				events.add(AgentEvent.doneEating);
				//isHungry = false;
				stateChanged();
			}
		},
		5000);//getHungerLevel() * 1000);//how long to wait before running task
	}

	private void leaveTable() {
		print("leaving table");
		//Do("Leaving.");
		waiter.leaving(this);
		//host.msgLeavingTable(this);
		customerGui.DoExitRestaurant();
	}

	private void requestCheck() {
		print (waiter + ", I am done eating, can I have check?");
		waiter.doneEating(this);
	}
	private void pay() {
		customerGui.DoGoToCashier(); // animation
		try {
			atDest.acquire();
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		if (check.getTotal() <= cash) {
			print ("I have " + cash + " and check's total is " + check.getTotal());
			print (cashier + ", here is my payment");
			cash -= check.getTotal();
			cashier.payment(check, check.getTotal(), this);
		}else {
			print (cashier + ", I have not enough money, can I pay next time?");
			cashier.cannotPayBill(check, this);
		}
	}
	private void payDeferred() {
		customerGui.DoGoToCashier();
		try {
			atDest.acquire();
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		if (check.getTotal() <= cash) {
			print ("I have " + cash + " and check's total is " + check.getTotal());
			print (cashier + ", here is my payment");
			cash -= check.getTotal();
			cashier.paymentForDeferredPayment(check.getTotal(), this);
			//customerGui.doGoBackToLine();	//animation
			state = AgentState.WaitingInRestaurant;
			//TODO:
		}else {
			print (cashier + ", I have not enough money, can I pay next time?");
			cashier.cannotPayBill(check, this);
		}
	}
	
	private void leaveRestaurant() {
		if (event == AgentEvent.kickedOut)
			print("Do you know who I am !!? You kick me out? Screw you!!");
		customerGui.DoExitRestaurant(); //animation
	}
	
	private void stayOrLeave() {
		host.iAm(stayIfFull, this);
		if (stayIfFull) {
			print("I will wait");
			state = AgentState.WaitingInRestaurant;
		}else {
			print("I am not waiting, good bye");
			state = AgentState.Leaving;
			leaveRestaurant();
		}
	}
	
	
	/* Utilities */

	public CustomerAgent(String name){
		super();
		this.name = name;
	}

	/**
	 * hack to establish connection to Host agent.
	 */
	public void setHost(Host host) {
		this.host = host;
	}
	public void setCash(double cash) {
		this.cash = cash;
	}
	public void setDecent(boolean isDecent) {
		this.isDecent = isDecent;
	}
	public String getCustomerName() {
		return name;
	}
	public void setMoney(double cash) {
		this.cash = cash;
	}
	public void setCashier(Cashier cashier) {
		this.cashier = cashier;
	}
	public String getName() {
		return name;
	}
	
	public void setLeavingIfFull() {
		this.stayIfFull = false;
	}
	public int getHungerLevel() {
		return hungerLevel;
	}

	public void setHungerLevel(int hungerLevel) {
		this.hungerLevel = hungerLevel;
		//could be a state change. Maybe you don't
		//need to eat until hunger lever is > 5?
	}

	public String toString() {
		return "customer " + getName();
	}

	public void setGui(CustomerGui g) {
		customerGui = g;
	}

	public CustomerGui getGui() {
		return customerGui;
	}
}


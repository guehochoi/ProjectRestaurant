package restaurant.gui;

import restaurant.CashierAgent;
import restaurant.CookAgent;
import restaurant.CustomerAgent;
import restaurant.HostAgent;
import restaurant.MarketAgent;
import restaurant.WaiterAgent;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

/**
 * Panel in frame that contains all the restaurant information,
 * including host, cook, waiters, and customers.
 */
public class RestaurantPanel extends JPanel {

    //Host, cook, waiters and customers
    private HostAgent host = new HostAgent("Sarah");
    //private WaiterAgent waiter = new WaiterAgent("Waiter");
    //private HostGui hostGui = new HostGui(host);
    private CookAgent cook = new CookAgent("Cook");
    private CashierAgent cashier = new CashierAgent("Cashier");
    //private WaiterGui waiterGui = new WaiterGui(waiter);
    private KitchenGui kitchenGui = new KitchenGui();
    private Map map = new Map();
    
    private Vector<CustomerAgent> customers = new Vector<CustomerAgent>();
    private Vector<WaiterAgent> waiters = new Vector<WaiterAgent>();

    private JPanel restLabel = new JPanel();
    private ListPanel customerPanel = new ListPanel(this, "Customers");
    // Added
    private ListPanel waiterPanel = new ListPanel(this, "Waiters");
    private JPanel group = new JPanel();

    private RestaurantGui gui; //reference to main gui

    public RestaurantPanel(RestaurantGui gui) {
        this.gui = gui;
        host.setCashier(cashier);
        host.setRestGui(gui);
        cook.setHost(host);
        cashier.setHost(host);
        CookGui cookGui = new CookGui(cook, kitchenGui);
        gui.animationPanel.addGui(kitchenGui);
        gui.animationPanel.addGui(cookGui);
        cook.setGui(cookGui);
        host.startThread();
        cook.startThread();
        cashier.startThread();
        
        /***********/
        MarketAgent m1 = new MarketAgent("WallMart");
        MarketAgent m2 = new MarketAgent("Target");
        MarketAgent m3 = new MarketAgent("Vons");
        m1.setCook(cook);m2.setCook(cook);m3.setCook(cook);
        m1.setCashier(cashier);m2.setCashier(cashier);m3.setCashier(cashier);
        cook.addMarket(m1);cook.addMarket(m2);cook.addMarket(m3);
        m1.startThread();m2.startThread();m3.startThread();
        cook.goToWork(); 
        /***********/
        
        setLayout(new GridLayout(1, 2, 20, 20));
        group.setLayout(new GridLayout(1, 2, 10, 10));

        group.add(customerPanel);

        //initRestLabel();
        //add(restLabel);
        add(waiterPanel);
        add(group);
    }

    /**
     * Sets up the restaurant label that includes the menu,
     * and host and cook information
     */
    private void initRestLabel() {
        JLabel label = new JLabel();
        //restLabel.setLayout(new BoxLayout((Container)restLabel, BoxLayout.Y_AXIS));
        restLabel.setLayout(new BorderLayout());
        label.setText(
                "<html><h3><u>Tonight's Staff</u></h3><table><tr><td>host:</td><td>" + host.getName() + "</td></tr></table><h3><u> Menu</u></h3><table><tr><td>Steak</td><td>$15.99</td></tr><tr><td>Chicken</td><td>$10.99</td></tr><tr><td>Salad</td><td>$5.99</td></tr><tr><td>Pizza</td><td>$8.99</td></tr></table><br></html>");

        restLabel.setBorder(BorderFactory.createRaisedBevelBorder());
        restLabel.add(label, BorderLayout.CENTER);
        restLabel.add(new JLabel("               "), BorderLayout.EAST);
        restLabel.add(new JLabel("               "), BorderLayout.WEST);
    }

    /**
     * When a customer or waiter is clicked, this function calls
     * updatedInfoPanel() from the main gui so that person's information
     * will be shown
     *
     * @param type indicates whether the person is a customer or waiter
     * @param name name of person
     */
    public void showInfo(String type, String name) {

        if (type.equals("Customers")) {

            for (int i = 0; i < customers.size(); i++) {
                CustomerAgent temp = customers.get(i);
                if (temp.getName() == name)
                    gui.updateInfoPanel(temp);
            }
        }else if (type.equals("Waiters")) {
        	for (WaiterAgent w : waiters) {
        		if (w.getName() == name)
        			gui.updateInfoPanel(w);
        	}
        }
    }

    /**
     * Adds a customer or waiter to the appropriate list
     *
     * @param type indicates whether the person is a customer or waiter (later)
     * @param name name of person
     */
    public void addPerson(String type, String name) {

    	if (type.equals("Customers")) {
    		CustomerAgent c = new CustomerAgent(name);	
    		CustomerGui g = new CustomerGui(c, gui);

    		gui.animationPanel.addGui(g);// dw
    		c.setHost(host);
    		c.setGui(g);
    		g.setMap(map);
    		c.setCashier(cashier);
    		customers.add(c);
    		c.startThread();
    		/* RestPanel tries to access ListPanel here */
        	if (customerPanel.hungryCheck()) {
        		c.getGui().setHungry();
        	}
    	}else if (type.equals("Waiters")) {
    		WaiterAgent w = new WaiterAgent(name);
    		WaiterGui g = new WaiterGui(w, gui);
    		//connection to gui added
    		
    		w.setCashier(cashier);
    		gui.animationPanel.addGui(g);
    		w.setHost(host);
    		w.setGui(g);
    		w.setCook(cook);
    		g.setKitchenGui(kitchenGui);
    		g.setMap(map);
    		waiters.add(w);
    		//waiters.add(w);
    		w.startThread();
    	}
    	
    }
    public void pauseAgents() {
    	host.pause();
    	cook.pause();
    	for (WaiterAgent w : waiters)
    		w.pause();
    	for (CustomerAgent c : customers)
    		c.pause();
    }
    public void resumeAgents() {
    	host.resume();
    	cook.resume();
    	for (WaiterAgent w : waiters)
    		w.resume();
    	for (CustomerAgent c : customers)
    		c.resume();
    }

    public boolean addTable() {
    	if (host.NTABLES == host.MAXNTABLES) {
    		return false;
    	}
    	host.addTable();
    	return true;
    }

    
    /* HACKS */
	public void hack_addPersonLeavingIfFull(String type, String name, JButton button) {
		
		if (type.equals("Customers")) {
    		CustomerAgent c = new CustomerAgent(name);	
    		CustomerGui g = new CustomerGui(c, gui);

    		gui.animationPanel.addGui(g);// dw
    		g.setMap(map);
    		c.setHost(host);
    		c.setGui(g);
    		c.setCashier(cashier);
    		customers.add(c);
    		c.setLeavingIfFull();//////HACK
    		c.startThread();
    		customerPanel.hack_getList().add(button);
    		customerPanel.hack_getView().add(button);
    		customerPanel.validate();
    		/* RestPanel tries to access ListPanel here */
        	
        		c.getGui().setHungry(); ////// HACK
    	}else if (type.equals("Waiters")) {
    		WaiterAgent w = new WaiterAgent(name);
    		WaiterGui g = new WaiterGui(w, gui);
    		//connection to gui added
    		
    		w.setCashier(cashier);
    		gui.animationPanel.addGui(g);
    		w.setHost(host);
    		w.setGui(g);
    		w.setCook(cook);
    		g.setKitchenGui(kitchenGui);
    		g.setMap(map);
    		waiters.add(w);
    		//waiters.add(w);
    		w.startThread();
    		waiterPanel.hack_getList().add(button);
    		waiterPanel.hack_getView().add(button);
    		waiterPanel.validate();
    	}
		
	}

	public void hack_addPersonWithMoney(String type, String name, JButton button, double cash, boolean decent) {
		CustomerAgent c = new CustomerAgent(name);	
		CustomerGui g = new CustomerGui(c, gui);

		gui.animationPanel.addGui(g);// dw
		c.setHost(host);
		c.setGui(g);
		g.setMap(map);
		c.setCashier(cashier);
		customers.add(c);
		c.setCash(cash);//////HACK
		c.setDecent(decent);
		c.startThread();
		/* RestPanel tries to access ListPanel here */
    	c.getGui().setHungry(); ////// HACK
    	customerPanel.hack_getList().add(button);
		customerPanel.hack_getView().add(button);
		customerPanel.validate();
	}

	public void hack_allCustomersHave100() {
		for (CustomerAgent c : customers) {
			c.setCash(100);
		}
	}

	public void hack_cookGotNoChicken() {
		cook.gotNoChicken();
		
	}

	public void hack_restaurantBudget0() {
		cashier.setRestaurantBudget(0);
	}
	
	public void hack_restaurantBudget1000() {
		cashier.setRestaurantBudget(1000);
	}
    
}

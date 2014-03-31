package restaurant.gui;

import restaurant.CustomerAgent;
import restaurant.HostAgent;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Subpanel of restaurantPanel.
 * This holds the scroll panes for the customers and, later, for waiters
 */
public class ListPanel extends JPanel implements ActionListener, DocumentListener, KeyListener {

    public JScrollPane pane =
            new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    private JPanel view = new JPanel();
    private List<JButton> list = new ArrayList<JButton>();
    private JButton addPersonB = new JButton("Add");
    private JTextField nameField = new JTextField(10);
    private JCheckBox hungryCheck = new JCheckBox();

    private RestaurantPanel restPanel;
    private String type;

    /**
     * Constructor for ListPanel.  Sets up all the gui
     *
     * @param rp   reference to the restaurant panel
     * @param type indicates if this is for customers or waiters
     */
    public ListPanel(RestaurantPanel rp, String type) {
        restPanel = rp;
        this.type = type;
        
        //setLayout(new BoxLayout((Container) this, BoxLayout.Y_AXIS));
        setLayout(new GridBagLayout());
        GridBagConstraints constraint = new GridBagConstraints();
        constraint.gridx = 0;
        constraint.gridy = 0;
        constraint.gridwidth = 3;
        constraint.gridheight = 1;
        constraint.fill = GridBagConstraints.HORIZONTAL;
        add(new JLabel("<html><pre> <u>" + type + "</u><br></pre></html>"), constraint);
        
        // Textbox to enter the name of person
        constraint.gridx = 0;
        constraint.gridy = 1;
        constraint.gridwidth = 1;
        constraint.weightx = .5;
        nameField.getDocument().addDocumentListener(this);
        nameField.setFocusable(true);
        nameField.addKeyListener(this);
        add(nameField, constraint);
        
        constraint.gridx = 1;
        constraint.gridy = 1;
        constraint.weightx = .1;
        hungryCheck.setToolTipText("Is the customer hungry?");
        //hungryCheck.addActionListener(this);
        hungryCheck.setEnabled(false);
        add(hungryCheck, constraint);
        
        constraint.gridx = 2;
        constraint.gridy = 1;
        constraint.weightx = .3;
        addPersonB.addActionListener(this);
        addPersonB.setEnabled(false);
        add(addPersonB, constraint);
        
        
        constraint.gridx = 0;
        constraint.gridy = 2;
        constraint.gridwidth = 3;
        constraint.weightx = 1;
        constraint.weighty = 1;
        constraint.fill = GridBagConstraints.BOTH;
        view.setLayout(new BoxLayout((Container) view, BoxLayout.Y_AXIS));
        pane.setViewportView(view);
        add(pane, constraint);
        
    }
    public boolean hungryCheck() {
    	return hungryCheck.isSelected();
    }

    /**
     * Method from the ActionListener interface.
     * Handles the event of the add button being pressed
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addPersonB) {
        	
        	try {
        		//Debug purpose: adding # of customers if entered number between 1~10
        		if (Integer.parseInt(nameField.getText()) <= 10 
            			&& Integer.parseInt(nameField.getText()) > 0) {
            		for (int i = 0; i < Integer.parseInt(nameField.getText()); i ++) {
            			addPerson(Integer.toString(i));
            		}
            	}
        	}catch (NumberFormatException ex) {
        		addPerson(nameField.getText());
        	}
        	
        	nameField.setText(null);
        	hungryCheck.setSelected(false);
        }
        else {
        	// Isn't the second for loop more beautiful?
            /*for (int i = 0; i < list.size(); i++) {
                JButton temp = list.get(i);*/
        	for (JButton temp:list){
                if (e.getSource() == temp)
                    restPanel.showInfo(type, temp.getText());
            }
        }
    }

    /**
     * If the add button is pressed, this function creates
     * a spot for it in the scroll pane, and tells the restaurant panel
     * to add a new person.
     *
     * @param name name of new person
     */
    public void addPerson(String name) {
        if (name != null) {
            JButton button = new JButton(name);
            button.setBackground(Color.white);

            Dimension paneSize = pane.getSize();
            Dimension buttonSize = new Dimension(paneSize.width - 20,
                    (int) (paneSize.height / 7));
            button.setPreferredSize(buttonSize);
            button.setMinimumSize(buttonSize);
            button.setMaximumSize(buttonSize);
            button.addActionListener(this);
            list.add(button);
            view.add(button);
            restPanel.addPerson(type, name);//puts customer on list
            restPanel.showInfo(type, name);//puts hungry button on panel
            validate();
        }
    }

	@Override
	public void changedUpdate(DocumentEvent e) {
		if (!hungryCheck.isEnabled()) {
			hungryCheck.setEnabled(true);
			addPersonB.setEnabled(true);
		}
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		if (!hungryCheck.isEnabled()) {
			hungryCheck.setEnabled(true);
			addPersonB.setEnabled(true);
		}
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		if (nameField.getText().isEmpty()) {
			hungryCheck.setEnabled(false);
			addPersonB.setEnabled(false);
		}
	}
	@Override
	public void keyPressed(KeyEvent e) {}
	@Override
	public void keyReleased(KeyEvent e) {
		if (e.isControlDown()) {
			int keyCode = e.getKeyCode();
			//HACKS---
			switch(keyCode) {
			case KeyEvent.VK_0: 
				/* leaving if restaurant is full */
				System.out.println("HACK: leaving if restaurant is full");
				for (int i=0; i < 5; i++) {
					hack_addPerson0("W"+Integer.toString(i), "Waiters");
				}
				for (int i=0; i < 7; i++) {
					hack_addPerson0("C"+Integer.toString(i), "Customers");
				}
				break;
			case KeyEvent.VK_1: 
				/* add customer with $1, cannot afford anything, decent customer leaves */
				System.out.println("HACK: creating customer with only $1");
				hack_addPerson1("C$1", "Customers", 1, true);
				break;
			case KeyEvent.VK_2:
				/* add customer with $10, can only afford chicken decent customer orders affordables */
				System.out.println("HACK: creating customer with only $10");
				hack_addPerson1("C$10", "Customers", 10, true);
				break;
			case KeyEvent.VK_3: 
				/* Cook will run out of chicken */
				System.out.println("HACK: Cook's chicken is now 0");
				restPanel.hack_cookGotNoChicken();
				break;
			case KeyEvent.VK_4: 
				/* add customer with $1, cannot afford anything. indecent customer orders! */
				System.out.println("HACK: creating customer with only $1");
				hack_addPerson1("C$10Indecent", "Customers", 1, false);
				break;
			case KeyEvent.VK_5: 
				/* All customers will have $100 bucks */
				System.out.println("HACK: all customers now have $100");
				restPanel.hack_allCustomersHave100();
				break;
			case KeyEvent.VK_6:  // doc below
				/* Restaurant will have 0 budget */
				System.out.println("HACK: Restaurant will now have $0");
				restPanel.hack_restaurantBudget0(); break;
			case KeyEvent.VK_7: 
				/* Restaurant will have 1 budget */
				System.out.println("HACK: Restaurant will now have $1000");
				restPanel.hack_restaurantBudget1000(); break;
			case KeyEvent.VK_8: System.out.println("8"); break;
			case KeyEvent.VK_9: System.out.println("9"); break;
			default: break;
			}
		}
	}
	@Override
	public void keyTyped(KeyEvent e) {}
	
	/* HACKS */
	
	public void hack_addPerson0(String name, String type) {
        if (name != null) {
            JButton button = new JButton(name);
            button.setBackground(Color.white);

            Dimension paneSize = pane.getSize();
            Dimension buttonSize = new Dimension(paneSize.width - 20,
                    (int) (paneSize.height / 7));
            button.setPreferredSize(buttonSize);
            button.setMinimumSize(buttonSize);
            button.setMaximumSize(buttonSize);
            button.addActionListener(this);
            //list.add(button);
            //view.add(button);
            restPanel.hack_addPersonLeavingIfFull(type, name, button);//puts customer on list
            restPanel.showInfo(type, name);//puts hungry button on panel
            validate();
        }
    }
	public void hack_addPerson1(String name, String type, double cash, boolean decent) {
		if (name != null) {
            JButton button = new JButton(name);
            button.setBackground(Color.white);

            Dimension paneSize = pane.getSize();
            Dimension buttonSize = new Dimension(paneSize.width - 20,
                    (int) (paneSize.height / 7));
            button.setPreferredSize(buttonSize);
            button.setMinimumSize(buttonSize);
            button.setMaximumSize(buttonSize);
            button.addActionListener(this);
            //list.add(button);
            //view.add(button);
            restPanel.hack_addPersonWithMoney(type, name, button, cash, decent);//puts customer on list
            restPanel.showInfo(type, name);//puts hungry button on panel
            validate();
        }
	}
	
	public List<JButton> hack_getList() {
		return this.list;
	}
	public JPanel hack_getView() {
		return this.view;
	}
}

package restaurant.gui;

import restaurant.CustomerAgent;
import restaurant.WaiterAgent;

import javax.swing.*;

import agent.Agent;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
/**
 * Main GUI class.
 * Contains the main frame and subsequent panels
 */
public class RestaurantGui extends JFrame implements ActionListener {
    /* The GUI has two frames, the control frame (in variable gui) 
     * and the animation frame, (in variable animationFrame within gui) to be removed
     */
	//JFrame animationFrame = new JFrame("Restaurant Animation");
	AnimationPanel animationPanel = new AnimationPanel();
	
    /* restPanel holds 2 panels
     * 1) the staff listing, menu, and lists of current customers all constructed
     *    in RestaurantPanel()
     * 2) the infoPanel about the clicked Customer (created just below)
     */    
    private RestaurantPanel restPanel = new RestaurantPanel(this);
    private JPanel optionPanel = new JPanel();
    
    /* infoPanel holds information about the clicked customer, if there is one*/
    private JPanel infoPanel;
    private JLabel infoLabel; //part of infoPanel
    private JCheckBox stateCB;//part of infoLabel

    private Object currentPerson;/* Holds the agent that the info is about.
    								Seems like a hack */

    /**
     * Constructor for RestaurantGui class.
     * Sets up all the gui components.
     */
    public RestaurantGui() {
        int WINDOWX = 600;
        int WINDOWY = 550;

        setBounds(100, 100, WINDOWX*2, WINDOWY);
        
        /* To create a frame that combines the initial two frames 
         * (rest frame and control frame) into one frame*/

    	// Instead of BoxLayout, use GridBagLayout
    	setLayout(new GridBagLayout());
    	GridBagConstraints constraint = new GridBagConstraints();
        
        // configure the animationPanel, top-left
    	
    	constraint.gridx = 0;
    	constraint.gridy = 0;
    	constraint.weightx = 1;
    	constraint.weighty = 1;
    	constraint.gridheight = 2;
    	constraint.fill = GridBagConstraints.BOTH;
        
    	Dimension animationDim = new Dimension(WINDOWX, WINDOWY);
    	animationPanel.setPreferredSize(animationDim);
    	animationPanel.setMinimumSize(animationDim);
    	animationPanel.setMaximumSize(animationDim);
    	
    	add(animationPanel, constraint);
    	
    	/* Creating animationFrame to be removed
        animationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        animationFrame.setBounds(100+WINDOWX, 50 , WINDOWX+100, WINDOWY+100);
        animationFrame.setVisible(true);
    	animationFrame.add(animationPanel); 
    	
    	setBounds(50, 50, WINDOWX, WINDOWY);
		
    	/*
        setLayout(new BoxLayout((Container) getContentPane(), 
        		BoxLayout.Y_AXIS));
		*/
    	
    	
    	// configure restPanel top-right
    	constraint.gridx = 1;
    	constraint.gridy = 0;    	
    	constraint.gridheight = 1;
    	constraint.weightx = 0.8D;
    	constraint.weighty = 0.6D;
    	
    	
        Dimension restDim = new Dimension(WINDOWX, (int) (WINDOWY * .6));
        restPanel.setPreferredSize(restDim);
        restPanel.setMinimumSize(restDim);
        restPanel.setMaximumSize(restDim);
        //add(restPanel); // instead, add constraint
        add(restPanel, constraint);
        
        
        
        JPanel rightBotWrapper = new JPanel();
        rightBotWrapper.setLayout(new GridBagLayout());
        
        constraint.gridx = 0;
        constraint.gridy = 0;
        constraint.weightx = 1D;
        constraint.fill = GridBagConstraints.BOTH;
        
        /* Options and its configurations */
        Dimension optionDim = new Dimension((int)(WINDOWX * .3), (int) (WINDOWY * .25));
        optionPanel.setPreferredSize(optionDim);
        optionPanel.setMinimumSize(optionDim);
        optionPanel.setMaximumSize(optionDim);
        optionPanel.setBorder(BorderFactory.createTitledBorder("Option"));
	        // 1: pause and release
	        final JButton p_rB = new JButton("Pause");
	        p_rB.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (p_rB.getText().equalsIgnoreCase("Pause")) {
						restPanel.pauseAgents();
						System.out.println("pause");
						p_rB.setText("Resume");
					}else if (p_rB.getText().equalsIgnoreCase("Resume")) {
						restPanel.resumeAgents();
						System.out.println("resume");
						p_rB.setText("Pause");
					}
				}
	        });
	        optionPanel.add(p_rB);
	        // 2: Table Control
	        final JButton addTableB = new JButton("Add Table");
	        addTableB.addActionListener(new ActionListener() {
	        	@Override
	        	public void actionPerformed(ActionEvent e) {
	        		if (!restPanel.addTable()) {
	        			addTableB.setVisible(false);
	        		}
	        	}
	        });
	        optionPanel.add(addTableB);
        
        rightBotWrapper.add(optionPanel, constraint);
        
        // Now, setup the info panel
        Dimension infoDim = new Dimension((int)(WINDOWX * .7), (int) (WINDOWY * .25));
        infoPanel = new JPanel();
        infoPanel.setPreferredSize(infoDim);
        infoPanel.setMinimumSize(infoDim);
        infoPanel.setMaximumSize(infoDim);
        infoPanel.setBorder(BorderFactory.createTitledBorder("Information"));

        stateCB = new JCheckBox();
        stateCB.setVisible(false);
        stateCB.addActionListener(this);

        infoPanel.setLayout(new GridLayout(1, 2, 30, 0));
        
        infoLabel = new JLabel(); 
        infoLabel.setText("<html><pre><i>Click Add to make customers</i></pre></html>");
        infoPanel.add(infoLabel);
        infoPanel.add(stateCB);
        
        constraint.gridx = 1;
        constraint.gridy = 0;
        constraint.weightx = .7D;
        rightBotWrapper.add(infoPanel, constraint);
        
        //add(infoPanel); // instead, use constraints
        // configure restPanel bot-right
        constraint.gridx = 1;
        constraint.gridy = 1;
        constraint.weighty = 0.25D;
        //add(infoPanel, constraint);
        add(rightBotWrapper, constraint);
        pack();
        
        /* My own panel with my name and my image, is to be removed
        constraint.gridx = 0;
        constraint.gridy = 2;
        
        JPanel myPanel = new JPanel();
        myPanel.setLayout(new FlowLayout());
        
        JLabel messageLabel = new JLabel("Ryan & Ashley in Korea :D");
        
        ImageIcon myImage = new ImageIcon("C:/Users/GChoi/Eclipse Java/agents/resources/me.jpg");
        //resizing
        // save this resizing for later
        BufferedImage resizedImg = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(myImage.getImage(), 0, 0, 50, 50, null);
        g2.dispose();
        
        JLabel imageLabel = new JLabel(new ImageIcon(resizedImg));
        //JLabel imageLabel = new JLabel(myImage);
        myPanel.add(messageLabel);
        myPanel.add(imageLabel);
        
        add(myPanel, constraint);
        */
    }
    /**
     * updateInfoPanel() takes the given customer (or, for v3, Host) object and
     * changes the information panel to hold that person's info.
     *
     * @param person customer (or waiter) object
     */
    public void updateInfoPanel(Object person) {
        stateCB.setVisible(true);
        currentPerson = person;

        if (person instanceof CustomerAgent) {
            CustomerAgent customer = (CustomerAgent) person;
            stateCB.setText("Hungry?");
          //Should checkmark be there? 
            stateCB.setSelected(customer.getGui().isHungry());
          //Is customer hungry? Hack. Should ask customerGui
            stateCB.setEnabled(!customer.getGui().isHungry());
          // Hack. Should ask customerGui
            infoLabel.setText(
               "<html><pre>     Name: " + customer.getName() + " </pre></html>");
        }else if (person instanceof WaiterAgent) {
        	/*This is added for lab 6*/
        	WaiterAgent waiter = (WaiterAgent) person;
        	if (waiter.getGui().isWaiterOnBreak()) {
        		stateCB.setText("BackToWork");
            	stateCB.setSelected(!waiter.getGui().isWaiterOnBreak());
            	stateCB.setEnabled(waiter.getGui().isWaiterOnBreak());
        	}else {
	        	stateCB.setText("WantABreak?");
	        	stateCB.setSelected(waiter.getGui().isWaiterOnBreak());
	        	stateCB.setEnabled(!waiter.getGui().isWaiterOnBreak());
        	}
        	infoLabel.setText(
        			"<html><pre>	Name: " + waiter.getName() + "</pre></html>");
        }
        infoPanel.validate();
    }
    /**
     * Action listener method that reacts to the checkbox being clicked;
     * If it's the customer's checkbox, it will make him hungry
     * For v3, it will propose a break for the waiter.
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == stateCB) {
            if (currentPerson instanceof CustomerAgent) {
                CustomerAgent c = (CustomerAgent) currentPerson;
                c.getGui().setHungry();
                stateCB.setEnabled(false);
            }else if ( currentPerson instanceof WaiterAgent) {
            	WaiterAgent w = (WaiterAgent) currentPerson;
            	if (stateCB.getText().equals("BackToWork")) {
            		w.getGui().setOffBreak();
            		stateCB.setEnabled(false);
            	}else {
	            	w.getGui().setOnBreak();
	            	stateCB.setEnabled(false);
            	}
            }
        }
    }
    /**
     * Message sent from a customer gui to enable that customer's
     * "I'm hungry" checkbox.
     *
     * @param c reference to the customer
     */
    public void setCustomerEnabled(CustomerAgent c) {
        if (currentPerson instanceof CustomerAgent) {
            CustomerAgent cust = (CustomerAgent) currentPerson;
            if (c.equals(cust)) {
                stateCB.setEnabled(true);
                stateCB.setSelected(false);
            }
        }
    }
    public void setWaiterBreakEnabled(WaiterAgent w) {
    	if (currentPerson instanceof WaiterAgent) {
    		WaiterAgent waiter = (WaiterAgent) currentPerson;
    		if (w.equals(waiter)) {
    			stateCB.setEnabled(true);
    			stateCB.setSelected(false);
    			w.waiterGui.setOffBreak();
    		}
    	}
    }
    public void waiterWentOnBreak(WaiterAgent w) {
    	if (currentPerson instanceof WaiterAgent) {
    		WaiterAgent waiter = (WaiterAgent) currentPerson;
    		
    	}
    }
    
    /**
     * Main routine to get gui started
     */
    public static void main(String[] args) {
        RestaurantGui gui = new RestaurantGui();
        gui.setTitle("csci201 Restaurant");
        gui.setVisible(true);
        //gui.setResizable(false);
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}

package restaurant.gui;

import javax.swing.*;

import restaurant.HostAgent;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class AnimationPanel extends JPanel implements ActionListener {

    public final static int WINDOWX = 600;
    public final static int WINDOWY = 550;
    
    // Added constants list
    public static final int SIZE_TABLE_X = 50;
    public static final int SIZE_TABLE_Y = 50;
    public static final int TABLE_GAP = 50;
    public static Map<Integer, Point> TableMap
    				= new HashMap<Integer, Point>();
    
    private final int DELAY = 8;
    
    private Image bufferImage;
    private Dimension bufferSize;

    private List<Gui> guis = new ArrayList<Gui>();
    

    public AnimationPanel() {
    	setSize(WINDOWX, WINDOWY);
        setVisible(true);
        
        /* TableMap */
        for (int i = 0; i < HostAgent.MAXNTABLES; i++) {
        	TableMap.put(i+1, 
        			new Point((int)((i%3)*(SIZE_TABLE_X+ TABLE_GAP))+TABLE_GAP , 
        					(int)((i/3)*(SIZE_TABLE_Y+TABLE_GAP))+TABLE_GAP));
        }
        
        
        bufferSize = this.getSize();
 
    	Timer timer = new Timer(DELAY, this );
    	timer.start();
    }

	public void actionPerformed(ActionEvent e) {
		repaint();  //Will have paintComponent called
	}

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;

        //Clear the screen by painting a rectangle the size of the frame
        g2.setColor(getBackground());
        g2.fillRect(0, 0, WINDOWX, WINDOWY );

        
        for (int i = 0; i < HostAgent.NTABLES; i++) {
	        //Here is the table
	        g2.setColor(Color.ORANGE);
	        Point position = TableMap.get(i+1);
	        g2.fillRect( (int)position.getX(), (int)position.getY(),
	        		SIZE_TABLE_X, SIZE_TABLE_Y);
	        //200 and 250 need to be table params --> now table's location is set by HostGui
	        // it would make much more sense that the host knows where the table is
	        // may be changed later if more tables come in
	        // I could possibly change it using tables per row with windowsize, which is better
        }

        for(Gui gui : guis) {
            if (gui.isPresent()) {
                gui.updatePosition();
            }
        }

        for(Gui gui : guis) {
            if (gui.isPresent()) {
                gui.draw(g2);
            }
        }
    }

    public void addGui(CustomerGui gui) {
        guis.add(gui);
    }

    public void addGui(WaiterGui gui) {
        guis.add(gui);
    }
    public void addGui(CookGui gui) {
    	guis.add(gui);
    }
    public void addGui(KitchenGui gui) {
    	guis.add(gui);
    }
}

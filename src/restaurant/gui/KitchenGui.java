package restaurant.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import restaurant.CookAgent;
import restaurant.gui.CookGui.FoodState;

public class KitchenGui implements Gui {
	
	
	/* Kitchen will be more like a static
	 * it does not have anything moving component,
	 * every components are made upon the instantiation.
	 * All the necessary components such as refrigerator, cooking station,
	 * and plating area must be accessed through public method
	 * One exception to the moving component might be the plates that are ready 
	 * 
	 * cooking idea: C Ch Chi Chic Chick Chicke Chicken
	 * plating idea: [] [] [] [] [Turkey] [Beef] [Chicken] <= waiter
	 * queue cannot work since there are more than one waiters, */
	
	private final static int x_leftbound = 0;
	private final static int x_rightbound = AnimationPanel.WINDOWX-100;
	private final static int y_upperbound = AnimationPanel.WINDOWY-100;
	private final static int y_lowerbound = AnimationPanel.WINDOWY;
	
	private final static int refrigerator_x = x_leftbound;
	private final static int refrigerator_y = y_upperbound;
	private final static int ref_size_x = 20;
	private final static int ref_size_y = y_lowerbound - y_upperbound;
	
	/* upper left point of cooking area*/
	private final static int cookingArea_x = x_leftbound + 70;
	private final static int cookingArea_y = y_lowerbound - 20;
	private final static int cookingArea_width = x_rightbound/2;
	private final static int cookingArea_height= 20;
	/* upper left point of plating area*/
	private final static int platingArea_x = x_rightbound / 2;
	private final static int platingArea_y = y_upperbound;
	private final static int platingArea_width = x_rightbound /2;
	private final static int platingArea_height = 20;
	
	private List<CookGui.Food> cookingFoods
		= Collections.synchronizedList(new ArrayList<CookGui.Food>());
	
	private List<MyFood> platedFoods
		= Collections.synchronizedList(new ArrayList<MyFood>());
		
	private class MyFood {
		String type;
		
		MyFood(String type) {
			this.type = type;
		}
	}
	
	
	public KitchenGui () {
		
	}
	
	@Override
	public void updatePosition() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void draw(Graphics2D g) {
		
		g.setColor(Color.lightGray);
		g.fillRect(x_leftbound, y_upperbound, x_rightbound-x_leftbound, y_lowerbound-y_upperbound);
		
		g.setColor(Color.blue);
		g.fillRect(refrigerator_x, refrigerator_y, ref_size_x, ref_size_y);
		
		g.setColor(Color.gray);
		g.fillRect(cookingArea_x, cookingArea_y, cookingArea_width, cookingArea_height);
		
		g.setColor(Color.white);
		g.fillRect(platingArea_x, platingArea_y, platingArea_width, platingArea_height);
		
		if (!platedFoods.isEmpty()) {	
			int offset = platingArea_width / platedFoods.size();
			synchronized ( platedFoods ) {
			for (MyFood f : platedFoods) {
				int xpos = platingArea_x + (offset * platedFoods.indexOf(f));
				int ypos = platingArea_y + 10;
				g.setColor(Color.black);
				g.drawString(f.type, xpos, ypos);
			}//platedFoods
			}//sync
		}
		if (!cookingFoods.isEmpty()) {
			int offset = cookingArea_width / cookingFoods.size();
			synchronized ( cookingFoods ) {
			for ( CookGui.Food f : cookingFoods) {
				int xpos = cookingArea_x + (offset * cookingFoods.indexOf(f));
				int ypos = cookingArea_y + 10;
				g.setColor(Color.cyan);
				g.drawString(f.type, xpos, ypos);
			}//cookingFoods
			}//sync
		}
		
	}

	@Override
	public boolean isPresent() {
		return true;
	}
	
	public int getRefX() {
		int x = refrigerator_x + ref_size_x;
		return x;
	}
	
	public int getRefY() {
		int y = refrigerator_y + (ref_size_y/2);
		return y;
	}

	public int getCookingX() {
		int x = cookingArea_x + (cookingArea_width/2);
		return x;
	}
	
	public int getCookingY() {
		int y = cookingArea_y - 20;
		return y;
	}

	public int getHomeX() {
		int x = (x_rightbound - x_leftbound) /4;
		return x;
	}

	public int getHomeY() {
		int y = (y_upperbound + platingArea_height);
		return y;
	}

	public int getPlatingX() {
		int x = platingArea_x + (platingArea_width/2);
		return x;
	}

	public int getPlatingY() {
		int y = platingArea_y + platingArea_height;
		return y;
	}
	
	public int getPlatingYWaiter() {
		int y = platingArea_y - 20;
		return y;
	}

	public void putFoodOnPlatingArea(String type) {
		platedFoods.add(new MyFood(type));
	}
	
	public void takeFoodFromPlatingArea(String type) {
		synchronized ( platedFoods ) {
		for (MyFood f : platedFoods) {
			if (f.type.equalsIgnoreCase(type)) {
				platedFoods.remove(f);
				break;
			}
		}//platedFoods
		}//sync
	}
	public void putFoodInCookingArea(CookGui.Food food) {
		cookingFoods.add(food);
	}
	public void takeFoodFromCookingArea(CookGui.Food food) {
		synchronized ( cookingFoods ) {
		for ( CookGui.Food f : cookingFoods) {
			if (f.equals(food)) {
				cookingFoods.remove(f);
				break;
			}
		}//cookingFoods
		}//sync
	}
	
	//TODO: A "Cooking" Area where the cooking takes place
	//TODO: A "Plating" Area from which the waiter picks up the completed food
	
}

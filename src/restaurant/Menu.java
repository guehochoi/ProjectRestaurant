package restaurant;

import java.util.*;

public class Menu {

	public final static int numberOfMenu = 5;
	
	public class Item {
		double price;
		Type type;
		Item(Type type, double price) {
			this.price = price;
			this.type = type;
		}
	}
	
	public enum Type {
		Chicken, Beef, Turkey, Pork, Duck
	}
	
	List<Item> availableItems = new ArrayList<Item>();
	
	public Menu() {
		availableItems.add(new Item ( Type.Beef, 12.99 ));
		availableItems.add(new Item ( Type.Chicken, 5.49 ));
		availableItems.add(new Item ( Type.Turkey, 11.99 ));
		availableItems.add(new Item ( Type.Pork, 10.99 ));
		availableItems.add(new Item ( Type.Duck, 16.99 ));
	}
	
	public boolean removeItemFromMenu(String str) {
		// check if that item is in the available items before remove, and if not, return false.
		Item del = null;
		Type t = Type.valueOf(str);
		switch(t) {
		case Chicken:
			for (Item i : availableItems) {
				if (i.type.toString().equalsIgnoreCase("Chicken"))
					del = i;
			}
			availableItems.remove(del);
			return true;
		case Beef:	
			for (Item i : availableItems) {
			if (i.type.toString().equalsIgnoreCase("Beef"))
				del = i;
			}
			availableItems.remove(del);
		return true;
		case Turkey:	
			for (Item i : availableItems) {
				if (i.type.toString().equalsIgnoreCase("Turkey"))
					del = i;
			}
			availableItems.remove(del);
			return true;
		case Pork:	
			for (Item i : availableItems) {
				if (i.type.toString().equalsIgnoreCase("Pork"))
					del = i;
			}
			availableItems.remove(del);
			return true;
		case Duck:	
			for (Item i : availableItems) {
				if (i.type.toString().equalsIgnoreCase("Duck"))
					del = i;
			}
			availableItems.remove(del);
			return true;
		default: System.out.println("Invalid removal of item");	return false;
		}
	}
	
	public static double getPrice(String choice) {
		
		Type t = Type.valueOf(choice);
		switch(t) {
		case Chicken:	return 5.49;
		case Beef:	return 12.99;
		case Turkey:	return 11.99;
		case Pork:	return 10.99;
		case Duck:	return 16.99;
		default: System.out.println("Invalid removal of item");	return -1;
		}
	}
	
	public String getRandom() {
		/* I encountered the Index out of bounds exception.
		 * My suspection is that index bounds happens when all the foods are out
		 * thus, there is no more food to be served, and there is nothing on the menu */
		
		int magic = (int)(Math.random() * availableItems.size());
		
		return availableItems.get(magic).type.toString(); 
		
	}
	public String getRandomAffordable(double cash) {
		ArrayList<Item> affordables = new ArrayList<Item>();
		double tax = 0.085;
		for (Item i : availableItems) {
			if (i.price + (double)((int)(tax*i.price*100)/100) <= cash) {
				//System.out.println(i.price + (double)(int)(tax*i.price*100)/100);
				affordables.add(i);
			}
		}
		if (affordables.isEmpty()) {
			// if null, the customer cannot afford anything
			return null;
		}
		int magic = (int)(Math.random() * affordables.size());
		
		return affordables.get(magic).type.toString(); 
	}
	
	
}

package restaurant;

import java.util.ArrayList;
import java.util.List;

public class Check {
	final static double tax = 0.085;
	private double subTotal = 0;
	List<String> items = new ArrayList<String>();
	
	public Check() {
	}
	public void addItem(String choice) {
		items.add(choice);
		subTotal += (double)((int)(Menu.getPrice(choice)));
	}
	public void addItem(String choice, int quantity) {
		items.add(choice);
		subTotal += (double)((int)((Menu.getPrice(choice)) * quantity));
	}
	public void appendCheckWithInterest(Check check, double interest) {
		for ( String str : check.items ) {
			items.add(str);
		}
		this.subTotal += check.getTotal() + (check.getTotal() * interest);
	}
	public double getTotal() {
		/*
		for (String i : items) {
			System.out.println(i + " price: " +Menu.getPrice(i));
			subTotal += Menu.getPrice(i);
		}*/
		double totalTax = (double)((int)((subTotal*tax)*100))/100;
		
		return totalTax + subTotal;
	}
	
}

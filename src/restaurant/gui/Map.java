package restaurant.gui;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import restaurant.CustomerAgent;
import restaurant.interfaces.Customer;

public class Map implements Gui {

	List<MyHomePos> waiterHomePositions
		= Collections.synchronizedList(new ArrayList<MyHomePos>());
	
	class MyHomePos {
		Point point;
		PointState s;
		WaiterGui g;
		MyHomePos(Point point, PointState s) {
			this.point = point;
			this.s = s;
		}
	}
	enum PointState {
		taken, available
	}
	
	List<WaitingPos> waitingAreaPositions
		= Collections.synchronizedList(new ArrayList<WaitingPos>());
	
	class WaitingPos {
		Point point;
		PointState s;
		CustomerGui g;
		WaitingPos (Point point, PointState s) {
			this.point = point;
			this.s = s;
		}
	}
	
	public Map() {
		int winx = AnimationPanel.WINDOWX;
		int rows = 0;
		int rowOffset = 22;
		int column = 22;
		int waitingAreaColumn = AnimationPanel.WINDOWY/2 + 40;
		for (int i=0; i<30; i++) {
			if ( i%3 == 0) {
				rows++;
			}
			waiterHomePositions.add(
					new MyHomePos(new Point(winx-(column*((i%3)+1)), rows*rowOffset), PointState.available));
			waitingAreaPositions.add(
					new WaitingPos(
							new Point(winx-(column*((i%3)+1)), 
									waitingAreaColumn + (rows*rowOffset)), 
									PointState.available));
		}
	}
	
	@Override
	public void updatePosition() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void draw(Graphics2D g) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isPresent() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public Point getWaiterHomePos(WaiterGui g) {
		synchronized( waiterHomePositions) {
		for (MyHomePos p: waiterHomePositions) {
			if (p.s == PointState.available) {
				p.s = PointState.taken;
				p.g = g;
				return p.point;
			}
		}//waiterHomePositions
		}//sync
		
		//there is no more available position
		return new Point(AnimationPanel.WINDOWX-20, 0);
	}
	
	public void positionAvailable(WaiterGui g) {
		synchronized( waiterHomePositions) {
		for (MyHomePos p: waiterHomePositions) {
			if( p.g != null) {
			if (p.g.equals(g)) {
				if (p.s == PointState.taken) {
					p.s = PointState.available;
					p.g = null;
					return;
				}
			}
			}
		}//waiterHomePositions
		}//sync
	}
	
	public Point getWaitingAreaPos(CustomerGui g) {
		synchronized( waitingAreaPositions) {
		for (WaitingPos p: waitingAreaPositions) {
			if (p.s == PointState.available) {
				p.s = PointState.taken;
				p.g = g;
				return p.point;
			}
		}//waiterHomePositions
		}//sync
		
		//there is no more available position
		return new Point(AnimationPanel.WINDOWX-20, AnimationPanel.WINDOWY/2);
	}
	public void positionAvailable(CustomerGui g) {
		synchronized( waitingAreaPositions) {
		for (WaitingPos p: waitingAreaPositions) {
			if( p.g != null) {
			if (p.g.equals(g)) {
				if (p.s == PointState.taken) {
					p.s = PointState.available;
					p.g = null;
					return;
				}
			}
			}
		}//waiterHomePositions
		}//sync
	}
	public Point getCustomerPosition(Customer c) {
		synchronized( waitingAreaPositions) {
		for (WaitingPos p: waitingAreaPositions) {
			if (p.g != null) {
			if (p.s == PointState.taken && p.g.getCustomerAgent().equals(c)) {
				return p.point;
			}
			}
		}//waiterHomePositions
		}//sync
		
		//there is no such customer
		return new Point(AnimationPanel.WINDOWX - 40, AnimationPanel.WINDOWY -80);
	}
	
	/*
	public static void main(String[] args) {
		Map map = new Map();
		for ( MyHomePos p : map.waiterHomePositions ) {
			System.out.println(p.point);
		}
	}*/

}

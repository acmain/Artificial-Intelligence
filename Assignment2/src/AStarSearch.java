import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

class AStarSearch {
	
	PriorityQueue<MyState> frontier;
	double lowestCost = -1;
	
	AStarSearch() {   }
	
	static void printLineage(MyState s) {
		if (s.parent != null) {
			printLineage(s.parent);
			s.print();
		} else {
			s.print();
		}
	}

	static double actionCost(int xPos, int yPos, int xMove, int yMove) {
		double speed = Model.getTravelSpeed(xPos, yPos);
		double dist = Math.sqrt(xMove * xMove + yMove * yMove);
		return dist / speed;
	}
	
	static final double lowestCost() {
		double speed = Double.MAX_VALUE;
		double temp;
		for(int xPos = 0; xPos < Model.XMAX; xPos+=10) {
			for(int yPos = 0; yPos < Model.YMAX; yPos+=10) {
				temp = Model.getTravelSpeed(xPos, yPos);
				if(temp < speed)
					speed = temp;
			}
		}
		return speed;
	}

	MyState a_star_search(MyState startState, MyState goalState) {
		QueueComparator qComp = new QueueComparator();
		TreeComparator tComp = new TreeComparator();
		frontier = new PriorityQueue<MyState>(qComp);
		TreeSet<MyState> beenthere = new TreeSet<MyState>(tComp);
		
		if(lowestCost == -1)
			lowestCost = lowestCost();
		
		startState.cost = 0.0;
		startState.parent = null;
		beenthere.add(startState);
		frontier.add(startState);
		while (frontier.size() > 0) {
			MyState s = frontier.poll();
			if (s.isEqual(goalState)) {
				return s;
			}

			int xMove, yMove;
			for (int i = -1; i < 2; i++) {
				for (int j = -1; j < 2; j++) {
					if (0 == j && 0 == i)
						continue;
					xMove = i * 10;
					yMove = j * 10;

					MyState child = new MyState(0.0, s);
					child.state = s.transition(xMove, yMove);
					double dist = Math.sqrt(Math.pow(child.state[0] - goalState.state[0],2) + Math.pow(child.state[1] - goalState.state[1],2));
					child.huer = dist / lowestCost / 10;
					

					double acost = actionCost(s.state[0], s.state[1], xMove, yMove);
					if (beenthere.contains(child)) {
						MyState oldChild = beenthere.floor(child);
						if (s.cost + acost < oldChild.cost) {
							oldChild.cost = s.cost + acost;
							oldChild.parent = s;
						}
					} else {
						child.cost = s.cost + acost;
						if (child.state[0] >= 0 && child.state[0] < Model.XMAX && child.state[1] >= 0
								&& child.state[1] < Model.YMAX)
							frontier.add(child);
						beenthere.add(child);
					}
				}
			}
		}
		throw new RuntimeException("There is no path to the goal");
	}
}
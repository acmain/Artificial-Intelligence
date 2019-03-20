import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Stack;
import java.awt.event.MouseEvent;
import java.awt.Graphics;
import java.awt.Color;

class Agent {

	MyState start = new MyState(0,null);
	MyState finish = new MyState(0,null);
	Stack<MyState> stack = new Stack<MyState>();
	MyState[] frontier;
	UniformCostSearch ucs = new UniformCostSearch();
	AStarSearch ass = new AStarSearch();
	
	int lastButton = 0;
	
	void drawPlan(Graphics g, Model m) {
		g.setColor(Color.red);
		
		for(int i = 0; i < stack.size(); i++) {
			if(i == 0)
				g.drawLine((int)m.getX(), (int)m.getY(), stack.peek().state[0], stack.peek().state[1]);
			else {
				int j = stack.size() - i;
				g.drawLine(stack.get(j).state[0], stack.get(j).state[1], stack.get(j-1).state[0], stack.get(j-1).state[1]);
			}
		}
		
		if(frontier != null) {
			g.setColor(Color.yellow);
			for(int i = 0; i < frontier.length; i++) {
				g.fillOval(frontier[i].state[0],frontier[i].state[1],10,10);
			}
		}
	}

	void updateStack(MyState s) {
		stack = new Stack<MyState>();
		lineageStack(s);
	}
	
	void lineageStack(MyState s) {
		if(s.parent != null) {
			stack.push(s);
			lineageStack(s.parent);
		}
	}
	
	void update(Model m)
	{
		Controller c = m.getController();
		
		while(true)
		{
			MouseEvent e = c.nextMouseEvent();
			if(e == null)
				break;
			lastButton = e.getButton();
			m.setDestination(e.getX(), e.getY());
		}
		
		if(((int)start.state[0]/10 != (int)m.getX()/10 || (int)start.state[1]/10 != (int)m.getY()/10)
				|| m.getDestinationX() != finish.state[0] || m.getDestinationY() != finish.state[1]) {			
			start.state[0] = (int) m.getX();
			start.state[1] = (int) m.getY();
			finish.state[0] = (int) m.getDestinationX();
			finish.state[1] = (int) m.getDestinationY();
			
			if(lastButton == 1){
				finish.parent = ucs.uniform_cost_search(start, finish);
				frontier = ucs.frontier.toArray(new MyState[0]);
			}
			else if(lastButton == 3){
				finish.parent = ass.a_star_search(start, finish);
				frontier = ass.frontier.toArray(new MyState[0]);
			}
			
			if(finish.parent != null && (stack.isEmpty() || !stack.get(0).isEqual(finish))) {
				updateStack(finish.parent);
			}
		}
	}

	public static void main(String[] args) throws Exception
	{		
		Controller.playGame();
	}
}

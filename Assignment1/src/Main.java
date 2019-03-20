import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

public class Main {
	
	public static boolean[][] draw;
	
	//set the initial state of the blocks
	public static void init(Shape[] b) {
		b[0] = new Shape(4, new Point(1,3));
		b[0].addPoint(1,0);
		b[0].addPoint(0,1);
		b[0].addPoint(1,1);
		
		b[1] = new Shape(3, new Point(1,5));
		b[1].addPoint(0,1);
		b[1].addPoint(1,1);
		
		b[2] = new Shape(3, new Point(2,5));
		b[2].addPoint(1,0);
		b[2].addPoint(1,1);
		
		b[3] = new Shape(3, new Point(3,7));
		b[3].addPoint(0,1);
		b[3].addPoint(1,1);
		
		b[4] = new Shape(3, new Point(4,7));
		b[4].addPoint(1,0);
		b[4].addPoint(1,1);
		
		b[5] = new Shape(3, new Point(6,7));
		b[5].addPoint(1,0);
		b[5].addPoint(0,1);
		
		b[6] = new Shape(4, new Point(5,4));
		b[6].addPoint(-1,1);
		b[6].addPoint(0,1);
		b[6].addPoint(0,2);
		
		b[7] = new Shape(4, new Point(6,4));
		b[7].addPoint(0,1);
		b[7].addPoint(1,1);
		b[7].addPoint(0,2);
		
		b[8] = new Shape(3, new Point(8,5));
		b[8].addPoint(0,1);
		b[8].addPoint(-1,1);
		
		b[9] = new Shape(3, new Point(6,2));
		b[9].addPoint(0,1);
		b[9].addPoint(-1,1);
		
		b[10] = new Shape(3, new Point(5,1));
		b[10].addPoint(1,0);
		b[10].addPoint(0,1);
		
		b[11] = new Shape(51, new Point(0,0));
		for(int i = 1; i < 10; i++)
			b[11].addPoint(i,0);
		for(int i = 0; i < 10; i++)
			b[11].addPoint(i,9);
		for(int i = 1; i < 9; i++)
			b[11].addPoint(0,i);
		for(int i = 1; i < 9; i++)
			b[11].addPoint(9,i);
		b[11].addPoint(1,1);
		b[11].addPoint(2,1);
		b[11].addPoint(1,2);
		b[11].addPoint(7,1);
		b[11].addPoint(8,1);
		b[11].addPoint(8,2);
		b[11].addPoint(4,3);
		b[11].addPoint(3,4);
		b[11].addPoint(4,4);
		b[11].addPoint(1,7);
		b[11].addPoint(1,8);
		b[11].addPoint(2,8);
		b[11].addPoint(8,7);
		b[11].addPoint(8,8);
		b[11].addPoint(7,8);
	}
	
	//check if a state is valid
	public static boolean validState(GameState state, Shape[] board) throws Exception {
		draw = new boolean[10][10];
		
		for(int i = 0; i < 11; i++) {
			board[i].shift(state.state[i*2], state.state[(i*2) + 1]);
		}
		
		for(int i = 0; i < 12; i++) {
			for(int j = 0; j < board[i].getCurrentSize(); j++) {
				Point p = board[i].getPos(j);
				
				if(!draw[p.getX()][p.getY()])
					draw[p.getX()][p.getY()] = true;
				else {
					for(int k = 0; k < 11; k++) {
						board[k].shift(-state.state[k*2], -state.state[(k*2) + 1]);
					}
					
					return false;
				}
			}
		}
		
		//move the pieces back
		for(int i = 0; i < 11; i++) {
			board[i].shift(-state.state[i*2], -state.state[(i*2) + 1]);
		}
		
		return true;
	}
	
	static String stateToString(GameState b)
	{
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < 22; i++) {
			if(i % 2 == 0) {
				sb.append("(");
				sb.append(Byte.toString(b.state[i]));
				sb.append(",");
			}
			else {
				sb.append(Byte.toString(b.state[i]));
				if(i != 21)
					sb.append(") ");
				else
					sb.append(")");
			}	
		}
		return sb.toString();
	}
	
	public static void breadthFirstSearch(Shape[] board, BufferedWriter w) throws Exception {
		StateComparator comp = new StateComparator();
		Queue<GameState> open = new LinkedList<GameState>();
		Set<GameState> closed = new TreeSet<GameState>(comp);
		
		open.add(new GameState(null));
		
		while(!open.isEmpty()) {
			GameState subRoot = open.poll();

			if(subRoot.state[0] == 4 && subRoot.state[1] == -2) {
				System.out.println("Solution Found");
				Thread.sleep(20000);
				Stack<GameState> s = new Stack<GameState>();
				while(subRoot.prev != null) {
					s.push(subRoot);
					subRoot = subRoot.prev;
				}
				s.push(new GameState(null));
		
				//Viz viz = new Viz(new GameState(null));
				for(int i = s.size()-1; i >= 0; i--) {
					if(i != 0)
						w.append(stateToString(s.elementAt(i)) + "\n");
					else
						w.append(stateToString(s.elementAt(i)));
					//viz.updateGameState(s.elementAt(i));
					//Thread.sleep(500);
				}
				return;
			}
			
			for(int i = 0; i < 22; i++) {
				for(int j = 0; j < 2; j++) {
					GameState state = new GameState(subRoot);
					for(int k = 0; k < 22; k++) {
						state.state[k] = subRoot.state[k];
					}

					if(j == 0) {
						state.state[i] -= 1;
					} else {
						state.state[i] += 1;
					}
					
					if(closed.contains(state)) {
						continue;
					} else {
						if(validState(state, board)) {
							closed.add(state);
							open.add(state);
						}
					}
				}
			}
			closed.add(subRoot);
		}
		
	}
	
	
	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();
		Shape[] board = new Shape[12];
		init(board);
		BufferedWriter write = new BufferedWriter(new FileWriter("results.txt"));

		breadthFirstSearch(board,write);
		
		write.close();
		
		long runTime = (new Date()).getTime() - startTime;
		double minutes = runTime/60000.0;
		System.out.printf("Runtime: %.3f minutes\n", minutes);
		
	}

}

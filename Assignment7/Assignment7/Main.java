import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;

/* ACTION GUIDE
 * 0 = left
 * 1 = right
 * 2 = up
 * 3 = down
 */

class State {
	int row, col;
	State parent;
	
	State(int row, int col){
		this.row = row;
		this.col = col;
	}
	
	void doAction(int action) {
		int dRow = 0, dCol = 0;
		if(action == 0) {
			dCol = -1;
		} else if(action == 1) {
			dCol = 1;
		} else if(action == 2) {
			dRow = -1;
		} else if(action == 3) {
			dRow = 1;
		}
		
		parent = new State(row,col);
		row += dRow;
		col += dCol;
	}
}

public class Main {

	static final int cols = 20;
	static final int rows = 10;
	static final int actions = 4;
	static double[][][] qTable;
	static double[][][] rTable;
	static char[][] grid;
	static int sRow,sCol,gRow,gCol;
	
	public static void main(String[] args) {	
		qTable = new double[rows][cols][actions];
		rTable = new double[rows][cols][actions];
		grid = new char[rows][cols];
		
		Random rand = new Random(0);
		//values chosen to make policy look better
		final double epsilon = 0.6;
		final double ak = 0.1;
		final double gamma = 0.95;
		final int iterations = 10000000;
		int count = 0;
		
		initGrid();
		//printGrid();
		initRTable();
		//printTable(rTable);
		
		State s = new State(sRow,sCol);
		for(int z = 0; z < iterations; z++) {
			int action = 0;
			if(rand.nextDouble() < epsilon) {
				//pick random action
				do {
					action = rand.nextInt(4);
				} while(rTable[s.row][s.col][action] == -1);
			} else {
				//pick best action
				for(int cand = 0; cand < 4; cand++) {
					if(qTable[s.row][s.col][cand] > qTable[s.row][s.col][action])
						action = cand;
				}
				if(qTable[s.row][s.col][action] == 0.0) {
					do {
						action = rand.nextInt(4);
					} while(rTable[s.row][s.col][action] == -1);
				}
			}
			
			//System.out.println(s.row + " " + s.col + " " + action);
			s.doAction(action);
			State i = s.parent;
			
			//apply equation
			int maxAct = 0;
			for(int cand = 0; cand < 4; cand++) {
				if(qTable[s.row][s.col][cand] > qTable[s.row][s.col][maxAct])
					maxAct = cand;
			}
			
			qTable[i.row][i.col][action] = (1.0-ak)*qTable[i.row][i.col][action] + ak*(rTable[i.row][i.col][action] + gamma*qTable[s.row][s.col][maxAct]);
			
			//reset
			if(s.row == gRow && s.col == gCol) {
				s = new State(sRow,sCol);
				normalize();
			}
			
			count++;
			if(count % 100000 == 0) {
				printPolicy();
				System.out.println();
			}
		}

	}
	
	static void printPolicy() {
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < cols; j++) {
				int maxAct = 0;
				for(int cand = 0; cand < 4; cand++) {
					if(qTable[i][j][cand] > qTable[i][j][maxAct])
						maxAct = cand;
				}
				char print = grid[i][j];
				if(print == ' ') {
					if(maxAct == 0)
						print = '<';
					else if (maxAct == 1)
						print = '>';
					else if (maxAct == 2)
						print = '^';
					else if (maxAct == 3)
						print = 'v';
				}
				
				System.out.print(print + " ");
			}
			System.out.println();
		}
	}
	
	static void initRTable() {
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < cols; j++) {
				for(int k = 0; k < actions; k++) {
					calcReward(i,j,k);
				}
			}
		}
	}
	
	static void calcReward(int row, int col, int action) {
		if(grid[row][col] == '#') {
			rTable[row][col][action] = -1;
			return; //will return if the current position isn't valid to save computation time
		}
		
		int dRow = 0, dCol = 0;
		if(action == 0) {
			dCol = -1;
		} else if(action == 1) {
			dCol = 1;
		} else if(action == 2) {
			dRow = -1;
		} else if(action == 3) {
			dRow = 1;
		}
		
		if(row+dRow < 0 || row+dRow >= rows || col+dCol < 0 || col+dCol >= cols) {
			rTable[row][col][action] = -1;
			return;
		}
		
		if(grid[row+dRow][col+dCol] == '#') {
			rTable[row][col][action] = -1;
			return;
		}
		
		if(grid[row+dRow][col+dCol] == 'G')	{
			gRow = row+dRow;
			gCol = col+dCol;
			rTable[row][col][action] = 100;
			return;
		}
		
		if(grid[row+dRow][col+dCol] == 'S')	{
			sRow = row+dRow;
			sCol = col+dCol;
		}
		
		//compute distance based reward
		double dist = 0;//Math.sqrt(Math.pow(row-gRow,2)+Math.pow(col-gCol, 2));
		rTable[row][col][action] = dist;
	}
	
	static void initGrid() {
		Scanner in = null;
		try {
			in = new Scanner(new File("grid.txt"));
		} catch (FileNotFoundException e) {
			System.out.println("Could not find input file: table.txt\nexiting...");
			System.exit(0);
		}
		
		for(int i = 0; i < rows; i++) {
			String line = in.nextLine();
			for(int j = 0; j < cols; j++) {
				grid[i][j] = line.charAt(j);
			}
		}
	}

	static void printGrid() {
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < cols; j++) {
				System.out.print(grid[i][j]);
			}
			System.out.println();
		}
	}
	
	static void printTable(double[][][] table) {
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < cols; j++) {
				System.out.print(i + "," + j + ": ");
				for(int k = 0; k < actions; k++) {
					System.out.print(table[i][j][k] + " ");
				}
				System.out.println();
			}
		}
	}
	
	static void normalize() {
		double maxVal = 0;
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < cols; j++) {
				for(int k = 0; k < actions; k++) {
					if(qTable[i][j][k] > maxVal)
						maxVal = qTable[i][j][k];
				}
			}
		}
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < cols; j++) {
				for(int k = 0; k < actions; k++) {
					qTable[i][j][k] = qTable[i][j][k] / maxVal * 100;
				}
			}
		}
	}
}

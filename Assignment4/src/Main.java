import java.util.Random;
import java.util.Scanner;

public class Main {

	public static void printBoard(State board) {
		char[] temp = new char[9];
		byte[] b = board.board;
		for (int i = 0; i < 9; i++) {
			if (b[i] == 0)
				temp[i] = (char) (49 + i);
			else if (b[i] == -1) {
				temp[i] = 'O';
			} else if (b[i] == 1) {
				temp[i] = 'X';
			}
		}

		System.out.printf(" %c | %c | %c \n", temp[0], temp[1], temp[2]);
		System.out.println("---+---+---");
		System.out.printf(" %c | %c | %c \n", temp[3], temp[4], temp[5]);
		System.out.println("---+---+---");
		System.out.printf(" %c | %c | %c \n", temp[6], temp[7], temp[8]);

	}

	public static boolean gameOver(State board) {
		byte[] b = board.board;

		// detect horizontal win
		int temp = 0;
		for (int i = 0; i < 9 && temp == 0; i += 3)
			if (b[i] != 0 && b[i] == b[i + 1] && b[i] == b[i + 2])
				temp = b[i];

		// detect vertical win
		for (int i = 0; i < 3 && temp == 0; i++)
			if (b[i] != 0 && b[i] == b[i + 3] && b[i] == b[i + 6])
				temp = b[i];

		// detect diagonal win
		if (b[0] != 0 && b[0] == b[4] && b[0] == b[8] && temp == 0)
			temp = b[0];
		if (b[2] != 0 && b[2] == b[4] && b[2] == b[6] && temp == 0)
			temp = b[2];

		// detect draw
		for (int i = 0; i < 9 && temp == 0; i++) {
			if (b[i] == 0)
				break;
			if (i == 8 && b[i] != 0)
				temp = 2; // declare draw if no spaces left
		}

		if (temp != 0) {
			if (temp == 2)
				temp = 0; // official draw state
			board.board[9] = (byte) temp; // byte 9 stores winner
			return true;
		}

		return false;
	}

	public static int getInt(Scanner in, int min, int max) {
		boolean valid = false;
		int input = 0;

		while (!valid) {
			try {
				input = Integer.parseInt(in.nextLine());
			} catch (Exception e) {
				System.out.print("Not a number, try again: ");
				continue;
			}

			if (input > min && input < max)
				return input - 1; // return as byte array index
			else {
				System.out.printf("Number not between %d and %d, try again: ", min + 1, max - 1);
			}
		}

		return -1;
	}

	private static boolean setBoard(State b, int i, int val, boolean verbose) {
		if (b.board[i] == 0) {
			b.board[i] = (byte) val;
			return true;
		}

		if (verbose)
			System.out.println("Space Occupied '" + (i + 1) + "', try again.");

		return false;
	}

	public static void main(String[] args) {
		boolean again = true;
		Scanner in = new Scanner(System.in);

		while (again) {
			
			State board = new State(null);
			printBoard(board);
			
			while (!gameOver(board)) {
				int space;
				do {
					System.out.print("Choose a square #: ");
					space = getInt(in,0,10);
				} while (!setBoard(board, space, 1, true));

				if (gameOver(board)) {
					printBoard(board);
					break;
				}

				MiniMax m = new MiniMax(new State(board));

				board = new State(m.makeMove(board));
				/*
				 * int cpu; do { cpu = r.nextInt(9); } while(!setBoard(board,cpu, -1, false));
				 * System.out.printf("Opponents Move: %d\n",cpu+1);
				 */
				printBoard(board);
			}

			if (board.board[9] == 1) {
				System.out.println("Player Wins");
			} else if (board.board[9] == -1) {
				System.out.println("Computer Wins");
			} else {
				System.out.println("Draw");
			}
			
			System.out.println("Would you like to play again? 1)Yes 2)No");
			int choice = getInt(in,0,3);
			if(choice == 1) //getInt subtracts 1 from input number;
				again = false;
		}
		in.close();
	}
}

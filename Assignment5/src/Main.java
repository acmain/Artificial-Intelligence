import java.util.Random;
import java.util.Scanner;

public class Main {
	static Scanner in = new Scanner(System.in);

	public static void main(String[] args) throws InterruptedException {
		int white = -1, black = -1;
		try {
			white = Integer.parseInt(args[0]);
			black = Integer.parseInt(args[1]);

		} catch (Exception e) {
			System.out.println("Command line arguments not 2 integers");
			System.exit(0);
		}
		;

		if (white < 0 || black < 0) {
			System.out.println("Command line arguments not non-negative integers");
			System.exit(0);
		}

		AlphaBeta ab = new AlphaBeta(new Random());
		ChessState s = new ChessState();
		s.resetBoard();
		s.printBoard(System.out);

		if (white > 0 && black > 0) { // two CPU players
			while (!s.isOver()) {
				s = new ChessState(ab.makeMove(s, white, true), true);
				//Thread.sleep(1000);
				s.printBoard(System.out);

				if (s.isOver())
					break;

				s = new ChessState(ab.makeMove(s, black, false), true);
				//Thread.sleep(1000);
				s.printBoard(System.out);
			}
		} else if (white == 0 && black == 0) { // two humans
			System.out.println("Enter Moves as Such: b1c3");
			while (!s.isOver()) {
				s = new ChessState(playerMove(s, true), true);
				s.printBoard(System.out);

				if (s.isOver())
					break;

				s = new ChessState(playerMove(s, false), true);
				s.printBoard(System.out);
			}
		} else if (white == 0) { // one human
			System.out.println("Enter Moves as Such: b1c3");
			while (!s.isOver()) {
				s = new ChessState(playerMove(s, true), true);
				s.printBoard(System.out);

				if (s.isOver())
					break;

				s = new ChessState(ab.makeMove(s, black, false), true);
				// Thread.sleep(1000);
				s.printBoard(System.out);
			}
		} else if (black == 0) { // one human
			System.out.println("Enter Moves as Such: b1c3");
			while (!s.isOver()) {
				s = new ChessState(ab.makeMove(s, white, true), true);
				// Thread.sleep(1000);
				s.printBoard(System.out);

				if (s.isOver())
					break;

				s = new ChessState(playerMove(s, false), true);
				s.printBoard(System.out);
			}
		}

		if (s.whiteWins())
			System.out.println("White Wins!");
		else
			System.out.println("Black Wins!");
	}

	private static ChessState playerMove(ChessState state, boolean isWhite) {
		String str = "";
		if (isWhite)
			str = "White Move: ";
		else
			str = "Black Move: ";

		char[] input;
		ChessState temp = new ChessState(state, true);
		do {
			System.out.println(str);
			input = transform(in.next().toUpperCase().toCharArray());

		} while (!temp.isValidMove(input[0] - 48, input[1] - 48, input[2] - 48, input[3] - 48)
				|| !temp.move(input[0] - 48, input[1] - 48, input[2] - 48, input[3] - 48));

		return temp;
	}

	private static char[] transform(char[] trans) {
		if (trans.length == 4) {
			trans[1] = (char) (trans[1] - 1);
			trans[3] = (char) (trans[3] - 1);
			trans[0] = (char) (trans[0] - ('A' - '0'));
			trans[2] = (char) (trans[2] - ('A' - '0'));
		} else {
			System.out.println("Input must be 4 chars long, try again");
		}
		return trans;
	}

}


public class MiniMax {

	MiniMax(State root) {
		// createTree(root);
	}

	static void debug(String s) {
		System.out.println(s);
	}

	boolean contains(byte[] array, int value, int maxIndex) {
		for (int i = 0; i <= maxIndex; i++) {
			if (value == array[i])
				return true;
		}
		return false;
	}

	boolean isTerminal(State node) {
		if (Main.gameOver(node)) {
			return true;
		}

		return false;
	}

	boolean isTrap(State node, boolean me) {
		int waysToLose = 0;
		for (int i = 0; i < node.children.size(); i++) {
			if (me && node.children.get(i).board[9] == 1)
				waysToLose++;
			if (!me && node.children.get(i).board[9] == -1)
				waysToLose++;
		}

		if (waysToLose > 1)
			return true;
		return false;
	}

	int minimax(State root, boolean isMaximizing) {
		if (isTerminal(root)) {
			if (root.board[9] == -1)
				root.cost = 10;
			else if (root.board[9] == 1)
				root.cost = -10;
			return root.cost;
		} else if (isTrap(root, true)) {
			root.cost = -100000;
			return root.cost;
		} else if (isTrap(root, false)) {
			root.cost = 100000;
			return root.cost;
		}

		if (isMaximizing) {
			root.cost = Integer.MIN_VALUE;
			for (int i = 0; i < root.children.size(); i++) {
				root.cost = Math.max(root.cost, minimax(root.children.get(i), false));
			}
			// debug(root.cost + "");
			return root.cost;
		} else {
			root.cost = Integer.MAX_VALUE;
			for (int i = 0; i < root.children.size(); i++) {
				root.cost = Math.min(root.cost, minimax(root.children.get(i), true));
			}
			// debug(root.cost + "");
			return root.cost;
		}
	}

	boolean playerTurn(State node) {
		int sum = 0;
		for (int i = 0; i < 9; i++) {
			sum += node.board[i];
		}
		if (sum == 1)
			return false;
		else if (sum == 0)
			return true;
		else
			debug("Why must you break things " + sum);

		System.exit(0);
		return false;
	}

	void createTree(State root) {
		// look for valid moves
		byte move;
		if (playerTurn(root))
			move = 1;
		else
			move = -1;

		for (int i = 0; i < 9; i++) {
			if (root.board[i] == 0) {
				State node = new State(root);
				node.board[i] = move;
				if (!isTerminal(node))
					createTree(node);
			}
		}
	}

	State makeMove(State board) {
		createTree(board);
		int maxCost = Integer.MIN_VALUE;
		int maxIndex = 0;

		for (int i = 1; i < board.children.size(); i++) {
			int temp = minimax(board.children.get(i), false);
			if (temp > maxCost) {
				maxCost = temp;
				maxIndex = i;
			}
		}
		
		printMove(board, board.children.get(maxIndex));

		return board.children.get(maxIndex);
	}

	private void printMove(State before, State after) {
		for(int i = 0; i < 9; i++) {
			if(before.board[i] != after.board[i]) {
				System.out.println("Opponents Move: " + (i+1));
				return;
			}
		}
		
	}

	void printArray(State board) {
		for (int i = 0; i < 10; i++)
			System.out.print(board.board[i]);
		System.out.println("");
	}
}

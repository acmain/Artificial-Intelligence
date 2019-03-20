import java.util.ArrayList;

public class State {

	State parent;
	ArrayList<State> children = new ArrayList<State>();
	byte[] board;
	int cost;

	State(State parent) {
		this.parent = parent;
		cost = 0;
		board = new byte[10];
		if (parent != null) {
			cost = parent.cost;
			setBoard(parent.board);
			parent.children.add(this);
		}

	}

	void setBoard(byte[] b) {
		for (int i = 0; i < b.length; i++) {
			board[i] = b[i];
		}
	}
}

import java.util.ArrayList;
import java.util.Random;

public class AlphaBeta {
	Random rand;
	
	AlphaBeta(Random rand) {
		this.rand = rand;
	}

	public ChessState makeMove(ChessState state, int depth, boolean isWhite) {
		int value = alphabeta(state, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, isWhite);
		for(int i = 0; i < state.children.size(); i++) {
			//System.out.print(state.children.get(i).heur + " ");
			if(value == state.children.get(i).heur) {
				return state.children.get(i);
			}
		}
		System.out.println("Could not find the optimal move");
		return null;
	}
	
	private int alphabeta(ChessState node, int depth, int alpha, int beta, boolean maximizingPlayer) {
		if (depth == 0 || node.isOver()) {
			//increased randomness to heuristic to ensure no inf. loops early in the game
			node.heur = node.heuristic(rand.nextInt(7) - 3);
			return node.heur;
		}

		if (maximizingPlayer) {
			int value = Integer.MIN_VALUE;
			ChessState.ChessMoveIterator it = node.iterator(maximizingPlayer);
			while (it.hasNext()) {
				ChessState.ChessMove move = it.next();
				ChessState child = new ChessState(node,false);
				child.move(move.xSource, move.ySource, move.xDest, move.yDest);
				
				value = Math.max(value, alphabeta(child, depth - 1, alpha, beta, false));
				alpha = Math.max(alpha, value);
				if (alpha >= beta)
					break;
			}
			node.heur = value;
			return node.heur;
		} else {
			int value = Integer.MAX_VALUE;
			ChessState.ChessMoveIterator it = node.iterator(maximizingPlayer);
			while (it.hasNext()) {
				ChessState.ChessMove move = it.next();
				ChessState child = new ChessState(node,false);
				child.move(move.xSource, move.ySource, move.xDest, move.yDest);

				value = Math.min(value, alphabeta(child, depth - 1, alpha, beta, true));
				beta = Math.min(beta, value);
				if (alpha >= beta)
					break;
			}
			
			node.heur = value;
			return node.heur;
		}
	}
}

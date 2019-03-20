import java.util.Comparator;

class GameState
{
	GameState prev;
	byte[] state;
	
	GameState(GameState _prev)
	{
		prev = _prev;
		state = new byte[22];
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof GameState) {
			GameState toCompare = (GameState) o;
			for(int i = 0; i < 22; i++) {
				if(toCompare.state[i] != state[i])
					return false;
			}
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return state.hashCode();
	}
	
	String stateToString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(Byte.toString(state[0]));
		for(int i = 1; i < state.length; i++) {
			sb.append(",");
			sb.append(Byte.toString(state[i]));
		}
		return sb.toString();
	}
}

class StateComparator implements Comparator<GameState>
{
	public int compare(GameState a, GameState b)
	{
		for(int i = 0; i < 22; i++)
		{
			if(a.state[i] < b.state[i])
				return -1;
			else if(a.state[i] > b.state[i])
				return 1;
		}
		return 0;
	}
}  
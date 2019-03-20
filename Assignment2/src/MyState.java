import java.util.Comparator;

class MyState {
	public double cost;
	MyState parent;
	int[] state;
	double huer;

	MyState(double cost, MyState par) {
		this.cost = cost;
		parent = par;
		state = new int[2];
		huer = 0;
		if(par != null) {
			state[0] = par.state[0];
			state[1] = par.state[1];
		}
	}

	String print() {
		return "(" + state[0] + "," + state[1] + ")";
	}
	
	boolean isEqual(MyState s) {
		for (int i = 0; i < 2; i++) {
			if ((int)(state[i]/10) != (int)(s.state[i]/10))
				return false;
		}
		return true;
	}

	int[] transition(int xMove, int yMove) {
		int[] trans = new int[2];
		trans[0] += (state[0] + xMove);
		trans[1] += (state[1] + yMove);
		return trans;
	}

}

class QueueComparator implements Comparator<MyState> {
	public int compare(MyState a, MyState b) {
		if (a.cost + a.huer < b.cost + b.huer) {
			return -1;
		} else if (a.cost + a.huer > b.cost + b.huer) {
			return 1;
		}
		else return 0;
	}
}

class TreeComparator implements Comparator<MyState> {
	public int compare(MyState a, MyState b) {
		for(int i = 0; i < 2; i++)
		{
			if(a.state[i] < b.state[i])
				return -1;
			else if(a.state[i] > b.state[i])
				return 1;
		}
		return 0;
	}
}

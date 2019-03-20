import java.util.PriorityQueue;

// This agent was written by Michael Gammon
class Winner2015b implements IAgent
{
	static int iter;
	float xPreset, yPreset;
	Sprite[] sprites = new Sprite[3];
	static State[][] terrainMap;
	
	Winner2015b() {
		reset();
		//The endgame coordinates for the sprites, close to the enemy flag
		xPreset = Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS / (float) Math.sqrt(2) * 0.9f;
		yPreset = Model.YFLAG_OPPONENT + Model.MAX_THROW_RADIUS / (float) Math.sqrt(2)  * 0.9f;
		for (int i = 0; i < sprites.length; i++) {
			sprites[i] = new Sprite(i, xPreset, yPreset);
		}
	}

	public void reset() {
		iter = 0;
	}

	public static float sq_dist(float x1, float y1, float x2, float y2) {
		return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
	}

	public void update(Model m) {
		getTerrainStateMap(m);
		Sprite.model = m;
		for (Sprite sprite : sprites) {
			sprite.update();
		}
		iter++;
	}
	
	public State[][] getTerrainStateMap(Model m){
		if (terrainMap == null) {
			terrainMap = TreeUtility.createTerrainStateMap(m);
		}
		return terrainMap;
	}
	
	public static class Bomb{
		int index;
		float distance;
		
		public Bomb(int index) {
			this.index = index;
			
		}
		
		public Bomb(int index, float distance) {
			this.index = index;
			this.distance = distance;
		}
	}
	
	public static class Enemy{
		int index;
		float distance;
		
		public Enemy(int index, float distance) {
			this.index = index;
			this.distance = distance;
		}
	}
	
	public static class Sprite{
		public static Model model;
		public int index;
		
		public float xPosition;
		public float yPosition;
		
		public Sprite(int i, float xPosition, float yPosition) {
			index = i;
			this.xPosition = xPosition;
			this.yPosition = yPosition;
		}
		
		public void update() {
			//Move to the preset position
			if (iter % 2 == 0) { moveToPosition(xPosition, yPosition); }
			//Keep your energy up
			if (energy() < 0.7f) { stay(); }
			Enemy nearestEnemy = getEnemyNearestMe();
			if (nearestEnemy != null) {
				shootEnemy(nearestEnemy);
			}
			//Shoot the flag if you can
			shootTheFlag();
			//Don't let enemies get too close (can come within 90% of throwing distance)
			avoidEnemies();
			//And always avoid bombs
			avoidBombs();
		}
		
		private boolean avoidEnemies() {
			Enemy nearestEnemy = getEnemyNearestMe();
			if(nearestEnemy != null && nearestEnemy.distance * 0.9f <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
				float dx = x() - model.getXOpponent(nearestEnemy.index);
				float dy = y() - model.getYOpponent(nearestEnemy.index);
				if(dx == 0 && dy == 0)
					dx = 1.0f;
				move(x() + dx * 10.0f, y() + dy * 10.0f);
				return true;
			}
			return false;
		}
		
		private Enemy getEnemyNearestMe() {
			int enemyIndex = -1;
			float dd = Float.MAX_VALUE;
			for(int i = 0; i < model.getSpriteCountOpponent(); i++) {
				if(model.getEnergyOpponent(i) < 0)
					continue; // don't care about dead opponents
				float d = sq_dist(x(), y(), model.getXOpponent(i), model.getYOpponent(i));
				if(d < dd) {
					dd = d;
					enemyIndex = i;
				}
			}
			if (enemyIndex == -1) {
				return null;
			}
			return new Enemy(enemyIndex, dd);
		}
		
		private boolean avoidBombs() {
			Bomb nearestBomb = getBombNearestMe();
			if(nearestBomb != null && nearestBomb.distance <= 2.0f * Model.BLAST_RADIUS * Model.BLAST_RADIUS) {
				float dx = x() - model.getBombTargetX(nearestBomb.index);
				float dy = y() - model.getBombTargetY(nearestBomb.index);
				if(dx == 0 && dy == 0)
					dx = 1.0f;
				move(x() + dx * 10.0f, y() + dy * 10.0f);
				return true;
			}
			return false;
		}
		
		private Bomb getBombNearestMe() {
			Bomb nearestBomb = new Bomb(-1, Float.MAX_VALUE);
			float dd = Float.MAX_VALUE;
			for(int i = 0; i < model.getBombCount(); i++) {
				float d = sq_dist(x(), y(), model.getBombTargetX(i), model.getBombTargetY(i));
				if(d < dd) {
					nearestBomb = new Bomb(i, d);
				}
			}
			if (nearestBomb.index == -1) {
				return null;
			}
			return nearestBomb;
		}
		
		//Uses UCS to find an optimal path
		private void moveToPosition(float x, float y) {
			State[][] costMap = TreeUtility.uniformCostSearch(
					terrainMap, 
					terrainMap[(int) x() / 10][(int) y() / 10], 
					terrainMap[(int) x / 10][(int) y / 10]);
			
			//Backtrack to the first step we need to take
			if (costMap != null) {
				State currentState = costMap[(int) x / 10][(int) y / 10];
				State prevState = null;
				while (currentState.parent != null) {
					prevState = currentState;
					currentState = prevState.parent;
				}
				if (prevState != null) {
					move(prevState.x * 10, prevState.y * 10);
				}
			}
		}
		
		private void shootTheFlag() {
			if (energy() > 0.5f & sq_dist(x(), y(), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
				shoot(Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
			}
		}
		
		private void shootEnemy(Enemy enemy) {
			//Center the shot on the enemy if I can
			if (enemy.distance <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
				shoot(model.getXOpponent(enemy.index), model.getYOpponent(enemy.index));

			//Otherwise try to get them in the blast radius anyhow
			}else if (Math.sqrt(enemy.distance) < Model.MAX_THROW_RADIUS + (Model.BLAST_RADIUS * 0.25 ) ) {
				float dx = model.getXOpponent(enemy.index) - x();
				float dy = model.getYOpponent(enemy.index) - y();
				float scale = (float) (Model.MAX_THROW_RADIUS / Math.sqrt(enemy.distance));
				float throwX = dx * scale + x();
				float throwY = dy * scale + y();
				shoot(throwX, throwY);
			}
		}
		
		public float x() {
			return model.getX(index);
		}
		
		public float y() {
			return model.getY(index);
		}
		
		public float energy() {
			return model.getEnergySelf(index);
		}
		
		public void move(float x, float y) {
			model.setDestination(index, x, y);
		}
		
		public void stay() {
			model.setDestination(index, x(), y());
		}
		
		public void shoot(float x, float y) {
			model.throwBomb(index, x, y);
		}
	}
	
	public static class State implements Comparable<State>{
		public double cost;
		public double actionCost;
		public State parent;
		public int x;
		public int y;

		public State(double actionCost, int x, int y) {
			this.cost = Double.MAX_VALUE;
			this.actionCost = actionCost;
			this.x = x;
			this.y = y;
		}
		  
		public State[] getAdjacent(State[][] states){
			int maxX = states.length - 1;
			int maxY = states[0].length - 1;
			
			State[] adjacentList = new State[4];
			adjacentList[0] = (x + 1 <= maxX) ? states[x + 1][y] : null;
			adjacentList[1] = (x - 1 >= 0) ? states[x - 1][y] : null;
			adjacentList[2] = (y + 1 <= maxY) ? states[x][y + 1] : null;
			adjacentList[3] = (y - 1 >= 0) ? states[x][y - 1] : null;

			return adjacentList;
		}

		@Override
		public int compareTo(State state) {
			if (this.cost > state.cost){
				  return 1;
			  }
			  if (this.cost < state.cost){
				  return -1;
			  }
			  return 0;
		}
	}
	
	public static class TreeUtility{
		//I'm representing the map as 1/10th of XMAX and YMAX to cut down on computation time.
		public static State[][] createTerrainStateMap(Model model){
			State[][] map = new State[((int)Model.XMAX / 10) + 1][((int) Model.YMAX / 10) + 1];
			for(int x = 0; x < map.length; x++) {
				for (int y = 0; y < map[0].length; y++) {
					map[x][y] = new State(1 / model.getTravelSpeed(x * 10, y * 10), x, y);
				}
			}
			return map;
		}
		
		public static State[][] uniformCostSearch(State[][] map, State start, State end){
			PriorityQueue<State> queue = new PriorityQueue<State>();
			boolean[][] visited = new boolean[map.length][map[0].length];
			start.cost = 0;
			start.parent = null;
			visited[start.x][start.y] = true;
			queue.add(start);
			
			while (queue.size() > 0){
				State state = queue.remove();
				if (state.equals(end)){
					return map;
				}
				for(State adjacentState : state.getAdjacent(map)){
					if (adjacentState == null) break;
					if (visited[adjacentState.x][adjacentState.y]){
						if (state.cost + adjacentState.actionCost < adjacentState.cost){
							adjacentState.cost = state.cost + adjacentState.actionCost;
							adjacentState.parent = state;
						}
					}else{
						adjacentState.cost = state.cost + adjacentState.actionCost;
						adjacentState.parent = state;
						queue.add(adjacentState);
						visited[adjacentState.x][adjacentState.y] = true;
					}
				}
			}
			return null;
		}
	}
}
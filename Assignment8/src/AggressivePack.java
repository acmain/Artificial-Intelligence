// The contents of this file are dedicated to the public domain.
// (See http://creativecommons.org/publicdomain/zero/1.0/)

class AggressivePack implements IAgent
{
	int iter;
	int index; // a temporary value used to pass values around

	AggressivePack() {
		reset();
	}

	public void reset() {
		iter = 0;
	}

	public static float sq_dist(float x1, float y1, float x2, float y2) {
		return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
	}

	float nearestBombTarget(Model m, float x, float y) {
		index = -1;
		float dd = Float.MAX_VALUE;
		for(int i = 0; i < m.getBombCount(); i++) {
			float d = sq_dist(x, y, m.getBombTargetX(i), m.getBombTargetY(i));
			if(d < dd) {
				dd = d;
				index = i;
			}
		}
		return dd;
	}

	float nearestOpponent(Model m, float x, float y) {
		index = -1;
		float dd = Float.MAX_VALUE;
		for(int i = 0; i < m.getSpriteCountOpponent(); i++) {
			if(m.getEnergyOpponent(i) < 0)
				continue; // don't care about dead opponents
			float d = sq_dist(x, y, m.getXOpponent(i), m.getYOpponent(i));
			if(d < dd) {
				dd = d;
				index = i;
			}
		}
		return dd;
	}

	void avoidBombs(Model m, int i) {
		if(nearestBombTarget(m, m.getX(i), m.getY(i)) <= 2.0f * Model.BLAST_RADIUS * Model.BLAST_RADIUS) {
			float dx = m.getX(i) - m.getBombTargetX(index);
			float dy = m.getY(i) - m.getBombTargetY(index);
			if(dx == 0 && dy == 0)
				dx = 1.0f;
			m.setDestination(i, m.getX(i) + dx * 10.0f, m.getY(i) + dy * 10.0f);
		}
	}

	public void update(Model m) {

		// Come together at the start of the game
		if(iter < 170) {
			m.setDestination(0, 300, 300);
			m.setDestination(1, 300, 300);
			m.setDestination(2, 300, 300);
			iter++;
			return;
		}

		// Find my player with the most energy.
		int leader = 0;
		if(m.getEnergySelf(1) > m.getEnergySelf(leader))
			leader = 1;
		if(m.getEnergySelf(2) > m.getEnergySelf(leader))
			leader = 2;

		// Find the enemy closest to the leader
		nearestOpponent(m, m.getX(leader), m.getY(leader));
		if(index >= 0) {
			float enemyX = m.getXOpponent(index);
			float enemyY = m.getYOpponent(index);
			for(int i = 0; i < 3; i++) {

				// Get close enough to throw a bomb at the enemy
				float myX = m.getX(i);
				float myY = m.getY(i);
				float dx = myX - enemyX;
				float dy = myY - enemyY;
				float t = 1.0f / Math.max(Model.EPSILON, (float)Math.sqrt(dx * dx + dy * dy));
				dx *= t;
				dy *= t;
				m.setDestination(i, enemyX + dx * (Model.MAX_THROW_RADIUS - Model.EPSILON), enemyY + dy * (Model.MAX_THROW_RADIUS - Model.EPSILON));

				// Throw bombs if I can hit the enemy
				if(sq_dist(enemyX, enemyY, myX, myY) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS)
					m.throwBomb(i, enemyX, enemyY);
			}
		}
		else {

			for(int i = 0; i < 3; i++) {

				// Head for the opponent's flag
				m.setDestination(i, Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT);

				// Shoot at the flag if I can hit it
				if(sq_dist(m.getX(i), m.getY(i), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
					if(iter % 5 == 0)
						m.throwBomb(i, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
				}
			}
		}

		// Try not to die
		for(int i = 0; i < 3; i++)
				avoidBombs(m, i);
		iter++;
	}
}

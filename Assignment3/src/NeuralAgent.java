// The contents of this file are dedicated to the public domain.
// (See http://creativecommons.org/publicdomain/zero/1.0/)

class NeuralAgent implements IAgent
{
	int index; // a temporary value used to pass values around
	NeuralNet nn;
	double[] in;
	double[] weights;

	NeuralAgent(double[] weights) {
		in = new double[20];
		nn = new NeuralNet();
		nn.layers.add(new LayerLinear(in.length, 8));
		nn.layers.add(new LayerTanh(8));
		nn.layers.add(new LayerLinear(8, 10));
		nn.layers.add(new LayerTanh(10));
		nn.layers.add(new LayerLinear(10, 3));
		nn.layers.add(new LayerTanh(3));
		
		double[] w = new double[291];
		for(int i = 0; i < 291; i++) {
			w[i] = weights[i];
		}
		
		this.weights = weights;
		setWeights(w);
	}

	public void reset() {
	}

	/// Returns the number of weights necessary to fully-parameterize this agent
	int countWeights() {
		int n = 0;
		for(int i = 0; i < nn.layers.size(); i++)
			n += nn.layers.get(i).countWeights();
		return n;
	}


	/// Sets the parameters of this agent with the specified weights
	void setWeights(double[] weights) {
		if(weights.length != countWeights())
			throw new IllegalArgumentException("Wrong number of weights. Got " + Integer.toString(weights.length) + ", expected " + Integer.toString(countWeights()));
		int start = 0;
		for(int i = 0; i < nn.layers.size(); i++)
			start += nn.layers.get(i).setWeights(weights, start);
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

	void beDefender(Model m, int i) {
		// Find the opponent nearest to my flag
		nearestOpponent(m, Model.XFLAG, Model.YFLAG);
		if(index >= 0) {
			float enemyX = m.getXOpponent(index);
			float enemyY = m.getYOpponent(index);

			// Stay between the enemy and my flag
			m.setDestination(i, 0.5f * (Model.XFLAG + enemyX), 0.5f * (Model.YFLAG + enemyY));

			// Throw boms if the enemy gets close enough
			if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS)
				m.throwBomb(i, enemyX, enemyY);
		}
		else {
			// Guard the flag
			m.setDestination(i, Model.XFLAG + Model.MAX_THROW_RADIUS, Model.YFLAG);
		}

		// If I don't have enough energy to throw a bomb, rest
		if(m.getEnergySelf(i) < Model.BOMB_COST)
			m.setDestination(i, m.getX(i), m.getY(i));

		// Try not to die
		avoidBombs(m, i);
	}

	void beFlagAttacker(Model m, int i) {
		// Head for the opponent's flag
		m.setDestination(i, Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT);

		// Shoot at the flag if I can hit it
		if(sq_dist(m.getX(i), m.getY(i), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
			m.throwBomb(i, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
		}

		// Try not to die
		avoidBombs(m, i);
	}

	void beAggressor(Model m, int i) {
		float myX = m.getX(i);
		float myY = m.getY(i);

		// Find the opponent nearest to me
		nearestOpponent(m, myX, myY);
		if(index >= 0) {
			float enemyX = m.getXOpponent(index);
			float enemyY = m.getYOpponent(index);

			if(m.getEnergySelf(i) >= m.getEnergyOpponent(index)) {

				// Get close enough to throw a bomb at the enemy
				float dx = myX - enemyX;
				float dy = myY - enemyY;
				float t = 1.0f / Math.max(Model.EPSILON, (float)Math.sqrt(dx * dx + dy * dy));
				dx *= t;
				dy *= t;
				m.setDestination(i, enemyX + dx * (Model.MAX_THROW_RADIUS - Model.EPSILON), enemyY + dy * (Model.MAX_THROW_RADIUS - Model.EPSILON));

				// Throw bombs
				if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS)
					m.throwBomb(i, enemyX, enemyY);
			}
			else {

				// If the opponent is close enough to shoot at me...
				if(sq_dist(enemyX, enemyY, myX, myY) <= (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS) * (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS)) {
					m.setDestination(i, myX + 10.0f * (myX - enemyX), myY + 10.0f * (myY - enemyY)); // Flee
				}
				else {
					m.setDestination(i, myX, myY); // Rest
				}
			}
		}

		// Try not to die
		avoidBombs(m, i);
	}

	public void update(Model m) {

		// Compute some features
		in[0] = m.getX(0) / 600.0 - 0.5;
		in[1] = m.getY(0) / 600.0 - 0.5;
		in[2] = m.getX(1) / 600.0 - 0.5;
		in[3] = m.getY(1) / 600.0 - 0.5;
		in[4] = m.getX(2) / 600.0 - 0.5;
		in[5] = m.getY(2) / 600.0 - 0.5;
		in[6] = nearestOpponent(m, m.getX(0), m.getY(0)) / 600.0 - 0.5;
		in[7] = nearestOpponent(m, m.getX(0), m.getY(0)) / 600.0 - 0.5;
		in[8] = nearestOpponent(m, m.getX(0), m.getY(0)) / 600.0 - 0.5;
		in[9] = nearestBombTarget(m, m.getX(0), m.getY(0)) / 600.0 - 0.5;
		in[10] = nearestBombTarget(m, m.getX(0), m.getY(0)) / 600.0 - 0.5;
		in[11] = nearestBombTarget(m, m.getX(0), m.getY(0)) / 600.0 - 0.5;
		in[12] = m.getEnergySelf(0);
		in[13] = m.getEnergySelf(1);
		in[14] = m.getEnergySelf(2);
		in[15] = m.getEnergyOpponent(0);
		in[16] = m.getEnergyOpponent(1);
		in[17] = m.getEnergyOpponent(2);
		in[18] = m.getFlagEnergySelf();
		in[19] = m.getFlagEnergyOpponent();

		// Determine what each agent should do
		double[] out = nn.forwardProp(in);

		// Do it
		for(int i = 0; i < 3; i++)
		{
			if(out[i] < -0.333)
				beDefender(m, i);
			else if(out[i] > 0.333)
				beAggressor(m, i);
			else
				beFlagAttacker(m, i);
		}
	}
}

// The contents of this file are dedicated to the public domain.
// (See http://creativecommons.org/publicdomain/zero/1.0/)

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Random;

class PrescientMoron implements IAgent
{
	int iter;
	Random r;

	PrescientMoron() {
		r = new Random();
	}

	public void reset() {
		iter = 0;
	}

	public static float sq_dist(float x1, float y1, float x2, float y2) {
		return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
	}

	void pickDestination(Model m, int sprite) {

		float bestX = m.getX(sprite);
		float bestY = m.getY(sprite);
		float best_sqdist = sq_dist(bestX, bestY, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
		for(int sim = 0; sim < 4; sim++) { // try 4 candidate destinations

			// Pick a random destination to simulate
			float x = (float)r.nextDouble() * Model.XMAX;
			float y = (float)r.nextDouble() * Model.YMAX;

			// Fork the universe and simulate it for 10 time-steps
			Controller cFork = m.getController().fork(new PrescientMoronShadow(x, y), new OpponentShadow());
			Model mFork = cFork.getModel();
			for(int j = 0; j < 10; j++)
				cFork.update();

			// See how close the current sprite got to the opponent's flag in the forked universe
			float sqd = sq_dist(mFork.getX(sprite), mFork.getY(sprite), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
			if(sqd < best_sqdist) {
				best_sqdist = sqd;
				bestX = x;
				bestY = y;
			}
		}

		// Head for the point that worked out best in simulation
		m.setDestination(sprite, bestX, bestY);

		// Shoot at the flag if I can hit it
		if(sq_dist(m.getX(sprite), m.getY(sprite), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS)
			m.throwBomb(sprite, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
	}

	public void update(Model m) {
		if(iter % 20 == 0) { // keep computation low by only doing this every 20 iterations
			for(int i = 0; i < 3; i++) {
				pickDestination(m, i);
			}
		}

		iter++;
	}

	static class PrescientMoronShadow implements IAgent
	{
		float dx;
		float dy;

		PrescientMoronShadow(float destX, float destY) {
			dx = destX;
			dy = destY;
		}

		public void reset() {
		}

		public void update(Model m) {
			for(int i = 0; i < 3; i++) {
				m.setDestination(i, dx, dy);
			}
		}
	}

	static class OpponentShadow implements IAgent
	{
		OpponentShadow() {
		}

		public void reset() {
		}

		public void update(Model m) {
			// The imagined opponent does nothing
		}
	}
}

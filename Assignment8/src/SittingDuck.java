// The contents of this file are dedicated to the public domain.
// (See http://creativecommons.org/publicdomain/zero/1.0/)

import java.awt.event.MouseEvent;
import java.util.ArrayList;

class SittingDuck implements IAgent
{
	int iter;

	SittingDuck() {
	}

	public void reset() {
		iter = 0;
	}

	public static float sq_dist(float x1, float y1, float x2, float y2) {
		return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
	}

	public void update(Model m) {
		if(iter > 500) {
			for(int i = 0; i < m.getSpriteCountSelf(); i++) {

				// Head for the opponent's flag
				m.setDestination(i, Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT);

				// Shoot at the flag if I can hit it
				if(sq_dist(m.getX(i), m.getY(i), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
					m.throwBomb(i, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
				}
			}
		}

/*
		// Debug spew
		System.out.print(Integer.toString(iter) + "=");
		for(int i = 0; i < m.getSpriteCountSelf(); i++) {
			System.out.print("(" + Float.toString(m.getX(i)) + "," + Float.toString(m.getY(i)) + ":" + Float.toString(m.getEnergySelf(i)) + ")");
		}
		System.out.println();
*/
		iter++;
	}
}

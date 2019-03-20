// The contents of this file are dedicated to the public domain.
// (See http://creativecommons.org/publicdomain/zero/1.0/)

import java.awt.event.MouseEvent;
import java.util.ArrayList;

// This class only works for player 1.
class Human implements IAgent
{
	int index;

	Human() {
	}

	public void reset() {}

	public static float sq_dist(float x1, float y1, float x2, float y2) {
		return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
	}

	float nearestAlly(Model m, float x, float y) {
		index = -1;
		float dd = Float.MAX_VALUE;
		for(int i = 0; i < m.getSpriteCountSelf(); i++) {
			if(m.getEnergySelf(i) < 0)
				continue; // don't care about dead robots
			float d = sq_dist(x, y, m.getX(i), m.getY(i));
			if(d < dd) {
				dd = d;
				index = i;
			}
		}
		return dd;
	}

	public void update(Model m) {
		Controller c = m.getController();
		while(true)
		{
			MouseEvent e = c.nextMouseEvent();
			if(e == null)
				break;
			int sel = c.getSelectedSprite();
			if(e.getButton() == MouseEvent.BUTTON1)
			{
				float dd = nearestAlly(m, e.getX(), e.getY());
				if(dd < 1400 || sel < 0)
					c.setSelectedSprite(index);
				else
					m.throwBomb(sel, e.getX(), e.getY());
			}
			else {
				if(sel >= 0)
					m.setDestination(sel, e.getX(), e.getY());
			}
		}
	}
}

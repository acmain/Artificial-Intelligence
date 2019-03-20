
// The contents of this file are dedicated to the public domain.
// (See http://creativecommons.org/publicdomain/zero/1.0/)

import java.awt.Graphics;
import java.io.File;
import java.util.Random;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import javax.imageio.ImageIO;

class Model {
	public static final float EPSILON = 0.0001f; // A small number
	public static final float XMAX = 1200.0f - EPSILON; // The maximum horizontal screen position. (The minimum is 0.)
	public static final float YMAX = 600.0f - EPSILON; // The maximum vertical screen position. (The minimum is 0.)

	private Controller controller;
	private static byte[] terrain;
	private static ArrayList<Sprite> sprites;
	private MyState m;
	private float dx=0,dy=0;

	Model(Controller c) {
		this.controller = c;
	}

	void initGame() throws Exception {
		BufferedImage bufferedImage = ImageIO.read(new File("terrain.png"));
		if (bufferedImage.getWidth() != 60 || bufferedImage.getHeight() != 60)
			throw new Exception("Expected the terrain image to have dimensions of 60-by-60");
		terrain = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
		sprites = new ArrayList<Sprite>();
		sprites.add(new Sprite(100, 100));
	}

	// These methods are used internally. They are not useful to the agents.
	byte[] getTerrain() {
		return this.terrain;
	}

	static ArrayList<Sprite> getSprites() {
		return sprites;
	}

	void update() {
		// Update the agents
		for (int i = 0; i < sprites.size(); i++)
			sprites.get(i).update();
	}

	// 0 <= x < MAP_WIDTH.
	// 0 <= y < MAP_HEIGHT.
	static float getTravelSpeed(float x, float y) {
		if(x < 0 || x >= XMAX || y < 0 || y >= YMAX) {
			return EPSILON;
		}
		
		int xx = (int) (x * 0.1f);
		int yy = (int) (y * 0.1f);
		if (xx >= 60) {
			xx = 119 - xx;
			yy = 59 - yy;
		}
		int pos = 4 * (60 * yy + xx);
		return Math.max(0.2f, Math.min(3.5f, -0.01f * (terrain[pos + 1] & 0xff) + 0.02f * (terrain[pos + 3] & 0xff)));
	}

	Controller getController() {
		return controller;
	}

	float getX() {
		return sprites.get(0).x;
	}

	float getY() {
		return sprites.get(0).y;
	}

	float getDestinationX() {
		return sprites.get(0).xDestination;
	}

	float getDestinationY() {
		return sprites.get(0).yDestination;
	}

	void setDestination(float x, float y) {
		Sprite s = sprites.get(0);
		s.xDestination = x;
		s.yDestination = y;
	}

	double getDistanceToDestination(int sprite) {
		Sprite s = sprites.get(sprite);
		return Math.sqrt(
				(s.x - s.xDestination) * (s.x - s.xDestination) + (s.y - s.yDestination) * (s.y - s.yDestination));
	}

	class Sprite {
		float x;
		float y;
		float xDestination;
		float yDestination;

		Sprite(float x, float y) {
			this.x = x;
			this.y = y;
			this.xDestination = x;
			this.yDestination = y;
		}

		void update() {
			
			if(dx == 0 && dy == 0 &&!controller.agent.stack.isEmpty())
				m = controller.agent.stack.pop();
			if(m != null) {
				float speed = Model.this.getTravelSpeed(this.x, this.y);
				dx = m.state[0] - this.x;
				dy = m.state[1] - this.y;
				float dist = (float) Math.sqrt(dx * dx + dy * dy);
				float t = speed / Math.max(speed, dist);
				dx *= t;
				dy *= t;
				this.x += dx;
				this.y += dy;
				this.x = Math.max(0.0f, Math.min(XMAX, this.x));
				this.y = Math.max(0.0f, Math.min(YMAX, this.y));
			}
		}
	}
}

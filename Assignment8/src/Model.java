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
	public static final float XFLAG = 100.0f; // The horizontal location of your flag
	public static final float YFLAG = 450.0f; // The vertical location of your flag
	public static final float XFLAG_OPPONENT = XMAX - XFLAG; // The horizontal location of the opponent's flag
	public static final float YFLAG_OPPONENT = YMAX - YFLAG; // The vertical location of the opponent's flag
	public static final float ENERGY_RECHARGE_RATE = 0.0005f; // The amount of energy given to each sprite each frame
	public static final float REST_RECHARGE_BONUS = 0.002f; // The amount of extra recharge if you are not moving or throwing
	public static final float BROKEN_CRAWL_RATE = 0.3f; // The relative rate at which a broken robot can crawl back to its flag, then reassenble itself
	public static final float BLAST_RADIUS = 48.0f; // The radius of the bomb's explosion
	public static final float MAX_THROW_RADIUS = 200.0f; // The max distance a sprite can throw a bomb
	public static final float BOMB_COST = 0.1f; // The energy cost to throw a bomb
	public static final float BOMB_DAMAGE_TO_FLAG = 0.05f; // The amount of energy a detonating bomb takes from a flag within its blast radius
	public static final float BOMB_DAMAGE_TO_SPRITE = 0.3f; // The amount of energy a detonating bomb takes from all sprites within the blast radius
	public static final int BOMB_COOL_DOWN = 40; // The number of frames a sprite must wait before throwing another bomb

	private Controller controller;
	private Object secret_symbol; // used to limit access to methods that agents could potentially use to cheat
	private byte[] terrain;
	private ArrayList<Sprite> sprites_blue;
	private ArrayList<Sprite> sprites_red;
	private ArrayList<Sprite> sprites_self;
	private ArrayList<Sprite> sprites_opponent;
	private ArrayList<Bomb> bombs_throwing;
	private ArrayList<Bomb> bombs_flying;
	private ArrayList<Bomb> bombs_exploding;
	private float energy_blue;
	private float energy_red;

	Model(Controller c, Object symbol) {
		this.controller = c;
		this.secret_symbol = symbol;
	}

	void initGame() throws Exception {
		BufferedImage bufferedImage = ImageIO.read(new File("terrain.png"));
		if(bufferedImage.getWidth() != 60 || bufferedImage.getHeight() != 60)
			throw new Exception("Expected the terrain image to have dimensions of 60-by-60");
		terrain = ((DataBufferByte)bufferedImage.getRaster().getDataBuffer()).getData();
		sprites_blue = new ArrayList<Sprite>();
		sprites_red = new ArrayList<Sprite>();
		setPerspectiveBlue(secret_symbol);
		bombs_throwing = new ArrayList<Bomb>();
		bombs_flying = new ArrayList<Bomb>();
		bombs_exploding = new ArrayList<Bomb>();
		energy_blue = 1.0f;
		energy_red = 1.0f;
		sprites_blue.add(new Sprite(100, 100));
		sprites_blue.add(new Sprite(100, 300));
		sprites_blue.add(new Sprite(100, 500));
		sprites_red.add(new Sprite(100, 100));
		sprites_red.add(new Sprite(100, 300));
		sprites_red.add(new Sprite(100, 500));
	}

	Model clone(Controller c, Object symbol) {
		Model m = new Model(c, symbol);
		m.terrain = terrain; // shallow copy
		m.sprites_blue = new ArrayList<Sprite>();
		for(int i = 0; i < sprites_blue.size(); i++)
			m.sprites_blue.add(sprites_blue.get(i).clone(m));
		m.sprites_red = new ArrayList<Sprite>();
		for(int i = 0; i < sprites_red.size(); i++)
			m.sprites_red.add(sprites_red.get(i).clone(m));
		m.sprites_self = (sprites_self == sprites_blue) ? m.sprites_blue : m.sprites_red;
		m.sprites_opponent = (sprites_opponent == sprites_blue) ? m.sprites_blue : m.sprites_red;
		m.bombs_throwing = new ArrayList<Bomb>();
		for(int i = 0; i < bombs_throwing.size(); i++)
			m.bombs_throwing.add(bombs_throwing.get(i).clone());
		m.bombs_flying = new ArrayList<Bomb>();
		for(int i = 0; i < bombs_flying.size(); i++)
			m.bombs_flying.add(bombs_flying.get(i).clone());
		m.bombs_exploding = new ArrayList<Bomb>();
		for(int i = 0; i < bombs_exploding.size(); i++)
			m.bombs_exploding.add(bombs_exploding.get(i).clone());
		m.energy_blue = energy_blue;
		m.energy_red = energy_red;
		return m;
	}

	// These methods are used internally. They are not useful to the agents.
	private void checkSymbol(Object symbol) { if(symbol != this.secret_symbol) throw new NullPointerException("Counterfeit symbol!"); }
	boolean amIblue(Object symbol) { checkSymbol(symbol); return sprites_self == sprites_blue; }
	void setPerspectiveBlue(Object symbol) { checkSymbol(symbol); sprites_self = sprites_blue; sprites_opponent = sprites_red; }
	void setPerspectiveRed(Object symbol) { checkSymbol(symbol); sprites_self = sprites_red; sprites_opponent = sprites_blue; }
	void setFlagEnergyBlue(Object symbol, float val) { checkSymbol(symbol); energy_blue = val; }
	void setFlagEnergyRed(Object symbol, float val) { checkSymbol(symbol); energy_red = val; }
	byte[] getTerrain(Object symbol) { checkSymbol(symbol); return this.terrain; }
	ArrayList<Sprite> getSpritesBlue(Object symbol) { checkSymbol(symbol); return this.sprites_blue; }
	ArrayList<Sprite> getSpritesRed(Object symbol) { checkSymbol(symbol); return this.sprites_red; }
	ArrayList<Bomb> getBombsFlying(Object symbol) { checkSymbol(symbol); return this.bombs_flying; }
	ArrayList<Bomb> getBombsExploding(Object symbol) { checkSymbol(symbol); return this.bombs_exploding; }

	void update() {
		// Update the blue agents
		for(int i = 0; i < sprites_blue.size(); i++)
			sprites_blue.get(i).update();

		// Update the red agents
		for(int i = 0; i < sprites_red.size(); i++)
			sprites_red.get(i).update();

		// Update the exploding bombs
		for(int i = 0; i < bombs_exploding.size(); i++) {
			if(!bombs_exploding.get(i).update()) {
				// Destroy this bomb
				bombs_exploding.set(i, bombs_exploding.get(bombs_exploding.size() - 1));
				bombs_exploding.remove(bombs_exploding.size() - 1);
			}
		}

		// Update the flying bombs
		for(int i = 0; i < bombs_throwing.size(); i++)
			bombs_flying.add(bombs_throwing.get(i));
		bombs_throwing.clear();
		for(int i = 0; i < bombs_flying.size(); i++) {
			Bomb b = bombs_flying.get(i);
			b.update();
			if(b.position > b.distance)
			{
				// Move this bomb to the exploding list
				bombs_exploding.add(b);
				bombs_flying.set(i, bombs_flying.get(bombs_flying.size() - 1));
				bombs_flying.remove(bombs_flying.size() - 1);

				// Damage nearby flags
				if(b.doesHit(XFLAG, YFLAG))
					energy_blue -= BOMB_DAMAGE_TO_FLAG;
				if(b.doesHit(XFLAG_OPPONENT, YFLAG_OPPONENT))
					energy_red -= BOMB_DAMAGE_TO_FLAG;

				// Damage nearby sprites
				for(int j = 0; j < sprites_blue.size(); j++)
					sprites_blue.get(j).onDetonation(b, false);
				for(int j = 0; j < sprites_red.size(); j++)
					sprites_red.get(j).onDetonation(b, true);
			}
		}
	}

	// 0 <= x < MAP_WIDTH.
	// 0 <= y < MAP_HEIGHT.
	float getTravelSpeed(float x, float y) {
			int xx = (int)(x * 0.1f);
			int yy = (int)(y * 0.1f);
			if(xx >= 60)
			{
				xx = 119 - xx;
				yy = 59 - yy;
			}
			int pos = 4 * (60 * yy + xx);
			return Math.max(0.2f, Math.min(3.5f, -0.01f * (terrain[pos + 1] & 0xff) + 0.02f * (terrain[pos + 3] & 0xff)));
	}

	Controller getController() { return controller; }
	long getTimeBalance() { return controller.getTimeBalance(secret_symbol, sprites_self == sprites_blue); }
	float getFlagEnergySelf() { return (sprites_self == sprites_blue ? energy_blue : energy_red); }
	float getFlagEnergyOpponent() { return (sprites_self == sprites_blue ? energy_red : energy_blue); }
	int getSpriteCountSelf() { return sprites_self.size(); }
	float getX(int sprite) { return sprites_self.get(sprite).x; }
	float getY(int sprite) { return sprites_self.get(sprite).y; }
	float getEnergySelf(int sprite) { return sprites_self.get(sprite).energy; }
	int getSpriteCountOpponent() { return sprites_opponent.size(); }
	float getXOpponent(int opponent) { return XMAX - sprites_opponent.get(opponent).x; }
	float getYOpponent(int opponent) { return YMAX - sprites_opponent.get(opponent).y; }
	float getEnergyOpponent(int sprite) { return sprites_opponent.get(sprite).energy; }
	int getBombCount() { return bombs_flying.size(); }

	float getBombTargetX(int bomb) {
		if(sprites_self == sprites_blue)
			return bombs_flying.get(bomb).xEnd;
		else
			return XMAX - bombs_flying.get(bomb).xEnd;
	}

	float getBombTargetY(int bomb) {
		if(sprites_self == sprites_blue)
			return bombs_flying.get(bomb).yEnd;
		else
			return YMAX - bombs_flying.get(bomb).yEnd;
	}

	void setDestination(int sprite, float x, float y) {
		Sprite s = sprites_self.get(sprite);
		if(s.energy >= 0) { // when you are dead, you cannot change your destination
			for(int i = 0; i < sprites_self.size(); i++) {
				Sprite t = sprites_self.get(i);
				if(i != sprite && (x - t.xDestination) * (x - t.xDestination) + (y - t.yDestination) * (y - t.yDestination) < 100) {
					x += 15;
					y += 10;
				}
			}
			s.xDestination = x;
			s.yDestination = y;
		}
	}

	double getDistanceToDestination(int sprite) {
		Sprite s = sprites_self.get(sprite);
		return Math.sqrt((s.x - s.xDestination) * (s.x - s.xDestination) + (s.y - s.yDestination) * (s.y - s.yDestination));
	}

	void throwBomb(int sprite, float x, float y) {
		Sprite s = sprites_self.get(sprite);
		if(s.energy < BOMB_COST || s.cooldown > 0)
			return;
		s.energy -= BOMB_COST;
		s.cooldown = BOMB_COOL_DOWN;
		float xStart;
		float yStart;
		if(sprites_self == sprites_blue) {
			xStart = s.x;
			yStart = s.y;
		}
		else {
			xStart = XMAX - s.x;
			yStart = YMAX - s.y;
			x = XMAX - x; // bombs are stored from the blue perspective
			y = YMAX - y; // bombs are stored from the blue perspective
		}
		float d = (float)Math.sqrt((x - xStart) * (x - xStart) + (y - yStart) * (y - yStart));
		if(d > MAX_THROW_RADIUS) {
			x = ((x - xStart) * MAX_THROW_RADIUS / d) + xStart;
			y = ((y - yStart) * MAX_THROW_RADIUS / d) + yStart;
		}
		bombs_throwing.add(new Bomb(xStart, yStart, x, y));
	}

	class Sprite {
		float x;
		float y;
		float xDestination;
		float yDestination;
		float energy;
		int cooldown;

		Sprite(float x, float y) {
			this.x = x;
			this.y = y;
			this.xDestination = x;
			this.yDestination = y;
			this.energy = 1.0f;
			this.cooldown = 0;
		}

		Sprite clone(Model m) {
			Sprite s = m.new Sprite(x, y);
			s.xDestination = xDestination;
			s.yDestination = yDestination;
			s.energy = energy;
			s.cooldown = cooldown;
			return s;
		}

		void update() {
			float speed = Model.this.getTravelSpeed(this.x, this.y);
			if(energy < 0.0f) {
				speed *= BROKEN_CRAWL_RATE;
				if((x - Model.XFLAG) * (x - Model.XFLAG) + (y - Model.YFLAG) * (y - Model.YFLAG) < Model.BLAST_RADIUS * Model.BLAST_RADIUS)
					energy = 0.0f;
			}
			else
				this.energy = Math.min(1.0f, this.energy + ENERGY_RECHARGE_RATE);
			float dx = this.xDestination - this.x;
			float dy = this.yDestination - this.y;
			float dist = (float)Math.sqrt(dx * dx + dy * dy);
			if(dist < EPSILON && energy >= 0.0f && this.cooldown == 0)
				this.energy = Math.min(1.0f, this.energy + REST_RECHARGE_BONUS);
			float t = speed / Math.max(speed, dist);
			dx *= t;
			dy *= t;
			this.x += dx;
			this.y += dy;
			this.x = Math.max(0.0f, Math.min(XMAX, this.x));
			this.y = Math.max(0.0f, Math.min(YMAX, this.y));
			this.cooldown = Math.max(0, this.cooldown - 1);
		}

		void onDetonation(Bomb b, boolean red) {
			if(!b.doesHit(red ? XMAX - x : x, red ? YMAX - y : y) || energy < 0.0f)
				return;
			energy -= BOMB_DAMAGE_TO_SPRITE;
			if(energy < 0.0f) { // Robot is broken
				xDestination = Model.XFLAG; // return to the flag
				yDestination = Model.YFLAG; // return to the flag
			}
		}
	}

	static class Bomb {
		float xBegin;
		float yBegin;
		float xEnd;
		float yEnd;
		float distance;
		float position;
		float prevPos;

		Bomb(float xB, float yB, float xE, float yE) {
			xBegin = xB;
			yBegin = yB;
			xEnd = xE;
			yEnd = yE;
			distance = (float)Math.sqrt((xE - xB) * (xE - xB) + (yE - yB) * (yE - yB));
			position = 0.0f;
			prevPos = 0.0f;
		}

		protected Bomb clone() {
			Bomb b = new Bomb(xBegin, yBegin, xEnd, yEnd);
			b.distance = distance;
			b.position = position;
			b.prevPos = prevPos;
			return b;
		}

		boolean doesHit(float x, float y) {
			return (x - xEnd) * (x - xEnd) + (y - yEnd) * (y - yEnd) < BLAST_RADIUS * BLAST_RADIUS;
		}

		boolean update() {
			prevPos = position;
			position += 3.5;
			return position < distance + BLAST_RADIUS;
		}

		float getX() { return (Math.min(1.0f, (position / distance)) * (xEnd - xBegin)) + xBegin; }
		float getY() { return (Math.min(1.0f, (position / distance)) * (yEnd - yBegin)) + yBegin; }
		boolean isDetonating() { return prevPos < distance && position >= distance; }
	}
}

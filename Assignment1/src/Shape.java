
public class Shape {
	int size;
	int currentIndex;
	Point origin;
	Point[] pos;
	
	Shape(int size, Point origin) {
		this.size = size;
		pos = new Point[size];
		this.origin = origin;
		pos[0] = origin;
		currentIndex = 0;
	}
	
	Shape(Shape copy) {
		size = copy.size;
		currentIndex = copy.currentIndex;
		origin = copy.origin;
		pos = copy.pos;
	}
	
	void addPoint(Point p) {
		if(currentIndex != size-1) {
			currentIndex++;
			pos[currentIndex] = p;
		} else {
			System.out.println("this shape cannot fit more points!");
		}
	}
	
	void addPoint(int xRel, int yRel) {
		//creates points relative to the shape origin
		if(currentIndex != size-1) {
			currentIndex++;
			pos[currentIndex] = new Point(pos[0].getX() + xRel, pos[0].getY() + yRel);
		} else {
			System.out.println("this shape cannot fit more points!");
		}
	}
	
	int getCurrentSize() {
		return currentIndex + 1;
	}
	
	Point getPos(int index) {
		return pos[index];
	}
	
	void shift(int x, int y) {
		for(int i = 0; i < currentIndex + 1; i++) {
			pos[i].shift(x, y);
		}
	}
	
}

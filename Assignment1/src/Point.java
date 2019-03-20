
public class Point {

	int x,y;
	
	Point() {
		this.x = -1;
		this.y = -1;
	}
	
	Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	int getX() {
		return x;
	}
	
	int getY() {
		return y;
	}
	
	void setX(int x) {
		this.x = x;
	}
	
	void setY(int y) {
		this.y = y;
	}
	
	void shift(int x, int y) {
		this.x += x;
		this.y += y;
	}
	
	String print() {
		return "(" + this.x + "," + this.y + ")";
	}
	
	void output() {
		System.out.printf("(%d,%d)\n",this.x,this.y);
	}
	
}

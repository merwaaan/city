import java.awt.Color;

public enum Density {

	 EMPTY,
	 LOW,
	 HIGH;

	 public int index() {

		  switch(this) {
		  case EMPTY:
				return 0;
		  case LOW:
				return 1;
		  case HIGH:
				return 2;
		  }

		  return -1;
	 }

	 public Color color(int alpha) {

		  switch(this) {
		  case EMPTY:
				return new Color(255, 255, 255, alpha);
		  case LOW:
				return new Color(255, 145, 145, alpha);
		  case HIGH:
				return new Color(255, 48, 48, alpha);
		  }

		  return Color.GREEN;
	 }

}

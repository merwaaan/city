import java.awt.Color;

public enum Density {

	 LOW,
	 MEDIUM,
	 HIGH;

	 public int index() {

		  switch(this) {
		  case LOW:
				return 0;
		  case MEDIUM:
				return 1;
		  case HIGH:
				return 2;
		  }

		  return -1;
	 }

	 public Color color(int alpha) {

		  switch(this) {
		  case LOW:
				return new Color(255, 255, 255, alpha);
		  case MEDIUM:
				return new Color(255, 145, 145, alpha);
		  case HIGH:
				return new Color(255, 48, 48, alpha);
		  }

		  return Color.GREEN;
	 }

}

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
				return new Color(255, 220, 220, alpha);
		  case MEDIUM:
				return new Color(255, 130, 130, alpha);
		  case HIGH:
				return new Color(255, 50, 50, alpha);
		  }

		  return Color.GREEN;
	 }

	 public int value() {

		  switch(this) {
		  case LOW:
				return 1;
		  case MEDIUM:
				return 2;
		  case HIGH:
				return 3;
		  }

		  return 0;
	 }

}

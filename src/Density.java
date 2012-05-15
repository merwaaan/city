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
}

package ColePole;

public class JaMa {

	/**
	 * Theta math
	 */

	static float pointToThea(float[] point) {
		float pointThea = (float) Math.atan(point[1] / point[0]);
		// sayVect("point", point);
		// System.out.println("pointFirst: " + pointThea + " ("
		// + (pointThea * (180 / Math.PI) + ")"));
		if (point[1] > 0 && pointThea < 0) {
			// System.out.println("change1");
			pointThea = (float) Math.PI + pointThea;
		} else if (point[1] < 0 && pointThea > 0) {
			// y is less than zero and thea is greater than zero.
			// System.out.println("change2");
			pointThea = -(float) Math.PI + pointThea;
		}
		// System.out.println("pointMid: " + pointThea + " ("
		// + (pointThea * (180 / Math.PI) + ")"));
		if (pointThea == 0 && point[0] < 0) {
			// System.out.println("zero to 360.");
			pointThea = (float) Math.PI;
		}
		return pointThea;
	}

	static float[] theaToPoint(float thea, float radius) {
		return new float[] { (float) Math.cos(thea) * radius,
				(float) Math.sin(thea) * radius };
	}

	static float[] rotPoint(float thea, float[] point) {
		// System.out.println("**rotPoint**");
		// System.out.println("inThea: " + thea + " (" + ((thea) * (180 /
		// Math.PI)));
		float pointa = Vect2d.norm(point);
		boolean bothNeg = false;
		if (point[0] < 0 && point[1] < 0) {
			// bothNeg = true;
		}
		// converts point to thea.
		float pointThea = pointToThea(point);
		// sayVect("point", point);
		// System.out.println("pointThea: " + pointThea);

		// adds theatas.
		float newThea = pointThea + thea;
		// System.out.println("old THEA: " + newThea + " ("
		// + (newThea * (180 / Math.PI)) + ")");
		if (newThea > Math.PI) {
			newThea = newThea - (float) Math.PI * 2;
		} else if (newThea < -Math.PI) {
			newThea = (float) (2 * Math.PI) + newThea;
		}

		// thea to point.
		// newPoint is hypotnuse.
		float[] newPoint = new float[2];
		// float nx = (float) Math.cos(newThea) * pointa;
		// float ny = (float) Math.tan(newThea) * nx;
		// System.out.println("sin: " + Math.sin(newThea) + "    pointa: "
		// + pointa);
		// System.out.println("tan: " + Math.tan(newThea));
		// System.out.println("NEW THEA: " + newThea + " ("
		// + (newThea * (180 / Math.PI)) + ")");
		/**
		 * CHEATE. radius should be pointa, but in order to make the program
		 * lookbetter it is set to radius. Fix the code and make it so pinta is
		 * equal to radius anyway.
		 */
		// System.out.println("newThea: " + newThea + "(" + newThea + ")");
		float ny = (float) Math.sin(newThea) * pointa;
		float nx = 1 / ((float) Math.tan(newThea) / ny);
		if (newThea > Math.PI / 2) {
			// System.out.println("bug ++");
		} else if (newThea < -Math.PI / 2) {
			// System.out.println("bug --");
		}
		if (Float.isNaN(nx)) {
			nx = pointa;
		}
		// System.out.println("bothNeg: " + bothNeg);
		if (bothNeg) {
			nx = -nx;
			ny = -ny;
		}
		// System.out.println("afterRot (" + nx + ", " + ny + ")");

		return new float[] { nx, ny };
	}

	static float theaAdd(float thea1, float thea2) {
		// adds two theas between -180 and 180 (in radians) and returned a thea
		// between -180 and 180
		float tempThea = thea1 + thea2;
		if (tempThea > Math.PI) {
			tempThea = -(float) Math.PI * 2 + tempThea;
		} else if (tempThea < -Math.PI) {
			tempThea = (float) Math.PI * 2 + tempThea;
		}
		return tempThea;
	}

	static float theaSub(float thea1, float thea2) {
		// System.out.println("thea1: " + thea1 + "   thea2: " + thea2);
		// adds two theas between -180 and 180 (in radians) and returned a thea
		// between -180 and 180
		float tempThea = thea1 - thea2;
		if (tempThea > Math.PI) {
			tempThea = -(float) Math.PI * 2 + tempThea;
		} else if (tempThea < -Math.PI) {
			tempThea = (float) Math.PI * 2 + tempThea;
		}
		// System.out.println("tempThea: " + tempThea);
		return tempThea;
	}

	// Jacob Math

	static int[] appendIntAr(int[] st, int appendage) {
		int[] temp = new int[st.length + 1];
		for (int a = 0; a < st.length; a++) {
			temp[a] = st[a];
		}
		temp[temp.length - 1] = appendage;
		return temp;
	}

	static int[][] appendIntArAr(int[][] st, int[] appendage) {
		int[][] temp = new int[st.length + 1][];
		for (int a = 0; a < st.length; a++) {
			temp[a] = st[a];
		}
		temp[temp.length - 1] = appendage;
		return temp;
	}

	static float[] appendFloatAr(float[] st, float appendage) {
		float[] temp = new float[st.length + 1];
		for (int a = 0; a < st.length; a++) {
			temp[a] = st[a];
		}
		temp[temp.length - 1] = appendage;
		return temp;
	}

	static float[] appendArFloatAr(float[] st, float[] appendage) {
		float[] temp = new float[st.length + appendage.length];
		for (int a = 0; a < st.length; a++) {
			temp[a] = st[a];
		}
		for (int a = 0; a < appendage.length; a++) {
			temp[st.length + a] = appendage[a];
		}
		return temp;
	}

	static float[][] appendFloatArAr(float[][] st, float[] appendage) {
		float[][] temp = new float[st.length + 1][];
		for (int a = 0; a < st.length; a++) {
			temp[a] = st[a];
		}
		temp[temp.length - 1] = appendage;
		return temp;
	}

	static float[][][] appendFloatArArAr(float[][][] st, float[][] appendage) {
		float[][][] temp = new float[st.length + 1][][];
		for (int a = 0; a < st.length; a++) {
			temp[a] = st[a];
		}
		temp[temp.length - 1] = appendage;
		return temp;
	}

	static int[] injectIntAr(int[] ar, int app, int loc) {
		System.out.println("ar.l: " + ar.length);
		int[] buff = new int[ar.length + 1];
		boolean added = false;
		for (int a = 0; a < buff.length; a++) {
			if (a == loc) {
				buff[a] = app;
				added = true;
			} else {
				if (added) {
					buff[a] = ar[a - 1];
				} else {
					buff[a] = ar[a];
				}
			}
		}
		if (!added) {
			buff[loc] = app;
		}
		return buff;
	}

	static int[][] injectIntArAr(int[][] ar, int[] app, int loc) {
		int[][] buff = new int[ar.length + 1][];
		boolean added = false;
		for (int a = 0; a < buff.length; a++) {
			if (a == loc) {
				buff[a] = app;
				added = true;
			} else {
				if (added) {
					buff[a + 1] = ar[a];
				} else {
					buff[a] = ar[a];
				}
			}
		}
		if (!added) {
			buff[loc] = app;
		}
		return buff;
	}

	static float[] injectFloatAr(float[] ar, float app, int loc) {
		// System.out.println("ar.l: " + ar.length);
		float[] buff = new float[ar.length + 1];
		boolean added = false;
		for (int a = 0; a < buff.length; a++) {
			if (a == loc) {
				buff[a] = app;
				added = true;
			} else {
				if (added) {
					buff[a] = ar[a - 1];
				} else {
					buff[a] = ar[a];
				}
			}
		}
		if (!added) {
			buff[loc] = app;
		}
		return buff;
	}

	static float[][] injectFloatArAr(float[][] ar, float[] app, int loc) {
		// System.out.println("ar.l: " + ar.length);
		float[][] buff = new float[ar.length + 1][];
		boolean added = false;
		for (int a = 0; a < buff.length; a++) {
			if (a == loc) {
				buff[a] = app;
				added = true;
			} else {
				if (added) {
					buff[a] = ar[a - 1];
				} else {
					buff[a] = ar[a];
				}
			}
		}
		if (!added) {
			buff[loc] = app;
		}
		return buff;
	}

	static float[] sortLowToHigh(float[] a) {
		// run through and find the lowest a's
		//
		// [0] = a
		// [1] = o
		float[] order = { a[0] };
		for (int o = 1; o < a.length; o++) {
			boolean stuckIn = false;
			bloop: for (int l = 0; l < order.length; l++) {
				if (a[o] < order[l]) {
					// stick in before and kill loop
					order = JaMa.injectFloatAr(order, a[o], l);
					stuckIn = true;
					break bloop;
				} else {
					// check the next
				}
			}
			if (stuckIn == false) {
				order = JaMa.appendFloatAr(order, a[o]);
			}
		}
		return order;
	}

	int[] shortenIntAr(int[] in, int numToRemove) {
		// This removed the [numToRemove] variable from an AR and compresses
		int[] temp = new int[in.length - 1];
		boolean reachedYet = false;
		for (int a = 0; a < in.length; a++) {
			System.out.println("a: " + a);
			if (a == numToRemove) {
				reachedYet = true;
				a++;
				System.out.println("newA: " + a);
			}
			if (a < in.length) {
				if (reachedYet) {
					temp[a - 1] = in[a];
				} else {
					temp[a] = in[a];
				}
			}
		}
		return temp;
	}

static	float[] removeFirstFloatAr(float[] ar, int upTo) {
		// removes the first (upTo) values of the Ar.
		float[] temp = new float[ar.length - upTo];
		for (int i = 0; i < temp.length; i++) {
			temp[i] = ar[i + upTo];
		}
		return temp;
	}
}

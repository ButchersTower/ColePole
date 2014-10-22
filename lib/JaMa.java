package ColePole.lib;

public class JaMa {

	// Jacob Math

	public static int[] appendIntAr(int[] st, int appendage) {
		int[] temp = new int[st.length + 1];
		for (int a = 0; a < st.length; a++) {
			temp[a] = st[a];
		}
		temp[temp.length - 1] = appendage;
		return temp;
	}

	public static int[][] appendIntArAr(int[][] st, int[] appendage) {
		int[][] temp = new int[st.length + 1][];
		for (int a = 0; a < st.length; a++) {
			temp[a] = st[a];
		}
		temp[temp.length - 1] = appendage;
		return temp;
	}

	public static float[] appendFloatAr(float[] st, float appendage) {
		float[] temp = new float[st.length + 1];
		for (int a = 0; a < st.length; a++) {
			temp[a] = st[a];
		}
		temp[temp.length - 1] = appendage;
		return temp;
	}

	public static float[] appendArFloatAr(float[] st, float[] appendage) {
		float[] temp = new float[st.length + appendage.length];
		for (int a = 0; a < st.length; a++) {
			temp[a] = st[a];
		}
		for (int a = 0; a < appendage.length; a++) {
			temp[st.length + a] = appendage[a];
		}
		return temp;
	}

	public static float[][] appendFloatArAr(float[][] st, float[] appendage) {
		float[][] temp = new float[st.length + 1][];
		for (int a = 0; a < st.length; a++) {
			temp[a] = st[a];
		}
		temp[temp.length - 1] = appendage;
		return temp;
	}

	public static float[][][] appendFloatArArAr(float[][][] st,
			float[][] appendage) {
		float[][][] temp = new float[st.length + 1][][];
		for (int a = 0; a < st.length; a++) {
			temp[a] = st[a];
		}
		temp[temp.length - 1] = appendage;
		return temp;
	}

	public static int[] injectIntAr(int[] ar, int app, int loc) {
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

	public static int[][] injectIntArAr(int[][] ar, int[] app, int loc) {
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

	public static float[] injectFloatAr(float[] ar, float app, int loc) {
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

	public static float[][] injectFloatArAr(float[][] ar, float[] app, int loc) {
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

	public static float[] sortLowToHigh(float[] a) {
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

	public static float[] removeFirstFloatAr(float[] ar, int upTo) {
		// removes the first (upTo) values of the Ar.
		float[] temp = new float[ar.length - upTo];
		for (int i = 0; i < temp.length; i++) {
			temp[i] = ar[i + upTo];
		}
		return temp;
	}
}

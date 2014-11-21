package ColePole.lib;

public class Vect2d {
	public static float dot(float[] a, float[] b) {
		float dp = (a[0] * b[0]) + (a[1] * b[1]);
		return dp;
	}

	public static float[] normalize(float[] vect) {
		float hyp = norm(vect);
		// stops deviding by zero resulting in NaN.
		if (hyp == 0) {
			return new float[] { 0, 0 };
		} else {
			return new float[] { vect[0] / hyp, vect[1] / hyp };
		}
	}

	public static float norm(float[] v) {
		return (float) Math.sqrt(dot(v, v));
	}

	public static float[] vectMultScalar(float scalar, float[] vect) {
		// this only works for 2d arrays but can make one that is less efficient
		// that works for anything.
		return new float[] { vect[0] * scalar, vect[1] * scalar };
	}

	public static float[] vectDivScalar(float scalar, float[] vect) {
		return new float[] { vect[0] / scalar, vect[1] / scalar };
	}

	public static float[] vectDivScalar(float scalar, int[] vect) {
		return new float[] { (float) (vect[0]) / scalar,
				(float) vect[1] / scalar };
	}

	public static float[] vectAdd(float[] a, float[] b) {
		return new float[] { a[0] + b[0], a[1] + b[1] };
	}

	public static float[] vectSub(float[] a, float[] b) {
		// a minus b.
		// b subtracted from a.
		return new float[] { a[0] - b[0], a[1] - b[1] };
	}

	public static void sayVect(String name, float[] vect) {
		System.out.println(name + " (" + vect[0] + ", " + vect[1] + ")");
	}

	public static float[] scaleVectTo(float[] vect, float scale) {
		// System.out.println("scale: " + scale);
		float vecta = norm(vect);
		vect[0] *= scale / vecta;
		vect[1] *= scale / vecta;
		// vecta = norm(vect);
		// System.out.println("vecta: " + vecta);
		return vect;
	}

	/**
	 * Vector projection
	 */

	static float[] a1Project(float[] a, float[] b) {
		// return point of projecting point a onto wall b.
		// |b|
		float ba = (float) norm(b);
		float[] bhat = { b[0] / ba, b[1] / ba };
		float ascalar = dot(a, b) / ba;
		float[] a1 = vectMultScalar(ascalar, bhat);
		return a1;
	}

	public static float scalarOfProject(float[] a, float[] b) {
		// return scalar of b of the projection.
		// project point a onto wall b.
		// |b|
		float ba = (float) norm(b);
		float ascalar = dot(a, b) / ba;
		ascalar /= ba;
		return ascalar;
	}

	/**
	 * Theta math
	 */

	public static float pointToThea(float[] point) {
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

	public static float[] theaToPoint(float thea, float radius) {
		return new float[] { (float) Math.cos(thea) * radius,
				(float) Math.sin(thea) * radius };
	}

	public static float[] rotPoint(float thea, float[] point) {
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

	public static float theaAdd(float thea1, float thea2) {
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

	public static float theaSub(float thea1, float thea2) {
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
}

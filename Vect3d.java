package ColePole;

public class Vect3d {
	static float dot(float[] a, float[] b) {
		float dp = (a[0] * b[0]) + (a[1] * b[1]) + (a[2] * b[2]);
		return dp;
	}

	static float[] normalise(float[] v) {
		float h = (float) Math.sqrt(dot(v, v));
		return new float[] { v[0] / h, v[1] / h, v[2] / h };
	}

	static float norm(float[] v) {
		return (float) Math.sqrt(dot(v, v));
	}

	static float[] vectMultScalar(float scalar, float[] vect) {
		// this only works for 3d arrays but can make one that is less efficient
		// that works for anything. (i think less effiecient)
		return new float[] { vect[0] * scalar, vect[1] * scalar,
				vect[2] * scalar };
	}

	static float[] vectDivScalar(float scalar, float[] vect) {
		return new float[] { vect[0] / scalar, vect[1] / scalar,
				vect[2] / scalar };
	}

	static float[] vectAdd(float[] a, float[] b) {
		return new float[] { a[0] + b[0], a[1] + b[1], a[2] + b[2] };
	}

	static float[] vectSub(float[] a, float[] b) {
		// a minus b.
		// b subtracted from a.
		return new float[] { a[0] - b[0], a[1] - b[1], a[2] - b[2] };
	}

	static void sayVect(String name, float[] vect) {
		System.out.println(name + " (" + vect[0] + ", " + vect[1] + ", "
				+ vect[2] + ")");
	}
}

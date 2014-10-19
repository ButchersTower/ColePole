package ColePole;

public class Vect2d {
	static float dot(float[] a, float[] b) {
		float dp = (a[0] * b[0]) + (a[1] * b[1]);
		return dp;
	}

	static float[] normalize(float[] vect) {
		float hyp = norm(vect);
		// stops deviding by zero resulting in NaN.
		if (hyp == 0) {
			return new float[] { 0, 0 };
		} else {
			return new float[] { vect[0] / hyp, vect[1] / hyp };
		}
	}

	static float norm(float[] v) {
		return (float) Math.sqrt(dot(v, v));
	}

	static float[] vectMultScalar(float scalar, float[] vect) {
		// this only works for 2d arrays but can make one that is less efficient
		// that works for anything.
		return new float[] { vect[0] * scalar, vect[1] * scalar };
	}

	static float[] vectDivScalar(float scalar, float[] vect) {
		return new float[] { vect[0] / scalar, vect[1] / scalar };
	}

	static float[] vectDivScalar(float scalar, int[] vect) {
		return new float[] { (float) (vect[0]) / scalar,
				(float) vect[1] / scalar };
	}

	static float[] vectAdd(float[] a, float[] b) {
		return new float[] { a[0] + b[0], a[1] + b[1] };
	}

	static float[] vectSub(float[] a, float[] b) {
		// a minus b.
		// b subtracted from a.
		return new float[] { a[0] - b[0], a[1] - b[1] };
	}

	static void sayVect(String name, float[] vect) {
		System.out.println(name + " (" + vect[0] + ", " + vect[1] + ")");
	}

	static float scalarOfProject(float[] a, float[] b) {
		// return scalar of b of the projection.
		// project point a onto wall b.
		// |b|
		float ba = (float) norm(b);
		// float[] bhat = { b[0] / ba, b[1] / ba };
		float ascalar = dot(a, b) / ba;
		// float[] a1 = vectMultScalar(ascalar, bhat);
		ascalar /= ba;
		// ascalar * ba = hyp of projected point
		// sayVect("a1", a1);
		return ascalar;
	}
}

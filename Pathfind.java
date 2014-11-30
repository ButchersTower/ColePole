package ColePole;

import java.util.ArrayList;

import ColePole.lib.JaMa;
import ColePole.lib.Vect2d;

public class Pathfind {

	// Player
	private float[] path = new float[0];
	private ArrayList<float[]> paths = new ArrayList<float[]>();

	// Trees
	// 0 = x, 1 = y, 2 = radius
	private float[][] trees;

	// drawing
	boolean makePath = false;

	/**
	 * Methods go below here.
	 * 
	 */

	// void tic() {
	// if (makePath) {
	// moving = false;
	// makePath(pRadius, pLoc, new float[] { (float) tarLoc[0],
	// (float) tarLoc[1] });
	// // path = find.magic(pLoc, pRadius, new float[] {
	// // (float) tarLoc[0], (float) tarLoc[1] });
	// moving = true;
	// makePath = false;
	// }
	//
	// if (moving) {
	// playSpeedLeft = playSpeed;
	// followPath();
	// }
	//
	// drwGm();
	// }

	public Pathfind(float[][] trees) {
		this.trees = trees;
	}

	float[] magic(float[] unitLoc, float unitRadius, int[] tarLoc) {
		float[] tar = { (float) tarLoc[0], (float) tarLoc[1] };
		makePath(unitRadius, unitLoc, tar);
		return path;
	}

	private void makePath(float unitRadius, float[] tempLoc, float[] finTarLoc) {
		paths.clear();
		path = new float[0];
		float[][] seg = { tempLoc, finTarLoc };
		int oldTreeIndex = segmentInterAnyTree(unitRadius, seg);
		if (oldTreeIndex < 0) {
			System.out
					.println("Linear Collision Detection, no collisions.\nGo straight to target.");
			float deltaa = Vect2d.norm(Vect2d.vectSub(finTarLoc, tempLoc));
			float[] delta = Vect2d.vectDivScalar(deltaa,
					Vect2d.vectSub(finTarLoc, tempLoc));
			path = new float[] { 0, delta[0], delta[1], deltaa };
			return;
		} else {
			System.out.println("Linear Collision DetectionM, closest tree is: "
					+ oldTreeIndex);
			paths.add(new float[0]);
			splitPathTo(unitRadius, tempLoc, oldTreeIndex, finTarLoc, 0);
			sortPaths();
			return;
		}
	}

	private void splitPathTo(float unitRadius, float[] tempLoc,
			int oldTreeIndex, float[] finTarLoc, int pathIndex) {
		System.out
				.println("Splitting paths at tangent point of the collided circle.");
		float[][] tangentPoints = getTangentPoints(tempLoc, unitRadius,
				trees[oldTreeIndex], trees[oldTreeIndex][2]);

		pathTo(unitRadius, tempLoc, oldTreeIndex, finTarLoc, tangentPoints[0],
				pathIndex, true);

		pathIndex = paths.size();
		paths.add(new float[0]);

		pathTo(unitRadius, tempLoc, oldTreeIndex, finTarLoc, tangentPoints[1],
				pathIndex, false);
	}

	private void pathTo(float unitRadius, float[] tempLoc, int oldTreeIndex,
			float[] finTarLoc, float[] tangentPoint, int pathIndex, boolean add) {
		System.out.println("*loopOne*");
		int newTreeIndex = segmentInterAnyTreeIgnore(unitRadius, new float[][] {
				tempLoc, Vect2d.vectAdd(tempLoc, tangentPoint) }, oldTreeIndex);
		if (newTreeIndex < 0) {
			System.out.println("No collision to tangent point.");
			float deltaa = Vect2d.norm(tangentPoint);
			float[] delta = Vect2d.vectDivScalar(deltaa, tangentPoint);
			float thea = Vect2d
					.pointToThea((Vect2d.vectSub(
							Vect2d.vectAdd(tempLoc, tangentPoint),
							trees[oldTreeIndex])));
			paths.set(
					pathIndex,
					JaMa.appendArFloatAr(paths.get(pathIndex), new float[] { 0,
							delta[0], delta[1], deltaa }));
			pathBack(unitRadius, oldTreeIndex, finTarLoc, thea, pathIndex, add);
		} else {
			System.out.println("Linear Collision Detection1, closest tree is: "
					+ oldTreeIndex);
			splitPathTo(unitRadius, tempLoc, newTreeIndex, finTarLoc, pathIndex);
		}
	}

	private void pathBack(float unitRadius, int oldTreeIndex,
			float[] finTarLoc, float entranceThea, int pathIndex, boolean add) {
		System.out.println("*pathBack*");
		float[][] tangentPoints = getTangentPoints(finTarLoc, unitRadius,
				trees[oldTreeIndex], trees[oldTreeIndex][2]);
		int newTreeIndex;
		float[] tanPoint;
		float[][] seg;
		if (add) {
			tanPoint = Vect2d.vectAdd(finTarLoc, tangentPoints[1]);
			seg = new float[][] { tanPoint, finTarLoc };
			newTreeIndex = segmentInterAnyTreeIgnore(unitRadius, seg,
					oldTreeIndex);
		} else {
			tanPoint = Vect2d.vectAdd(finTarLoc, tangentPoints[0]);
			seg = new float[][] { tanPoint, finTarLoc };
			newTreeIndex = segmentInterAnyTreeIgnore(unitRadius, seg,
					oldTreeIndex);
		}

		if (newTreeIndex < 0) {
			System.out.println("Linear Collision Detection, no collisions.");
			float[] pointRelTree = Vect2d
					.vectSub(tanPoint, trees[oldTreeIndex]);
			float thea = Vect2d.pointToThea(pointRelTree);

			float deltaa;
			float[] delta;
			if (add) {
				deltaa = Vect2d.norm(tangentPoints[1]);
				delta = Vect2d.vectDivScalar(-deltaa, tangentPoints[1]);
			} else {
				deltaa = Vect2d.norm(tangentPoints[0]);
				delta = Vect2d.vectDivScalar(-deltaa, tangentPoints[0]);
			}

			float[] path = { 1, entranceThea, thea,
					trees[oldTreeIndex][2] + unitRadius, oldTreeIndex, 0,
					delta[0], delta[1], deltaa };
			paths.set(pathIndex,
					JaMa.appendArFloatAr(paths.get(pathIndex), path));
		} else {
			System.out.println("Linear Collision DetectionB, closest tree is: "
					+ newTreeIndex);
			paths.set(pathIndex,
					JaMa.appendArFloatAr(paths.get(pathIndex), path));
			circCircTans(unitRadius, oldTreeIndex, newTreeIndex, finTarLoc,
					entranceThea, pathIndex, add);
		}
	}

	private void circCircTans(float unitRadius, int oldTreeIndex,
			int newTreeIndex, float[] finTarLoc, float oldEntranceThea,
			int pathIndex, boolean add) {
		System.out.println("*circCircTans*");
		float[][] innerSeg = tan2circ(trees[oldTreeIndex],
				trees[oldTreeIndex][2] + unitRadius, trees[newTreeIndex],
				trees[newTreeIndex][2] + unitRadius, add);

		int newPathIndex = paths.size();
		paths.add(paths.get(pathIndex).clone());

		int innerTreeIndex = segmentInterAnyTreeIgnore2(unitRadius, innerSeg,
				oldTreeIndex, newTreeIndex);
		if (innerTreeIndex < 0) {
			System.out
					.println("Inner Segment Detection, no collision to innerTreeIndex");
			float[] exitRelOld = Vect2d.vectSub(innerSeg[0],
					trees[oldTreeIndex]);
			float exitThea = Vect2d.pointToThea(exitRelOld);
			float[] entrRelNew = Vect2d.vectSub(innerSeg[1],
					trees[newTreeIndex]);
			float newEntranceThea = Vect2d.pointToThea(entrRelNew);

			float[] delta = Vect2d.vectSub(innerSeg[1], innerSeg[0]);
			float deltaa = Vect2d.norm(delta);
			delta = Vect2d.vectDivScalar(deltaa, delta);

			float[] part = { 1, oldEntranceThea, exitThea,
					trees[oldTreeIndex][2] + unitRadius, oldTreeIndex, 0,
					delta[0], delta[1], deltaa };
			add = !add;
			if (add) {
				paths.set(pathIndex,
						JaMa.appendArFloatAr(paths.get(pathIndex), part));
				pathBack(unitRadius, newTreeIndex, finTarLoc, newEntranceThea,
						pathIndex, add);
			} else {
				paths.set(pathIndex,
						JaMa.appendArFloatAr(paths.get(pathIndex), part));
				pathBack(unitRadius, newTreeIndex, finTarLoc, newEntranceThea,
						pathIndex, add);
			}
		} else {
			System.out.println("Inner Segment Detection, closest tree is: "
					+ innerTreeIndex);
			circCircTans(unitRadius, oldTreeIndex, innerTreeIndex, finTarLoc,
					oldEntranceThea, pathIndex, add);
			add = !add;
		}

		// When it the path is coming straight from the first path to circ.
		// It needs add to be inverted.

		float[][] outerSeg = getOuterAdjPre(oldTreeIndex,
				trees[oldTreeIndex][2] + unitRadius, newTreeIndex,
				trees[newTreeIndex][2] + unitRadius, oldEntranceThea,
				pathIndex, add);

		int outerTreeIndex = segmentInterAnyTreeIgnore2(unitRadius, outerSeg,
				oldTreeIndex, newTreeIndex);
		if (outerTreeIndex < 0) {
			System.out
					.println("Outer Segment Detection, no collision to outerTreeIndex");

			float[] exitRelOld = Vect2d.vectSub(outerSeg[0],
					trees[oldTreeIndex]);

			float exitThea = Vect2d.pointToThea(exitRelOld);

			float[] entrRelNew = Vect2d.vectSub(outerSeg[1],
					trees[newTreeIndex]);
			float newEntranceThea = Vect2d.pointToThea(entrRelNew);

			float[] delta = Vect2d.vectSub(outerSeg[1], outerSeg[0]);
			float deltaa = Vect2d.norm(delta);
			delta = Vect2d.vectDivScalar(deltaa, delta);

			float[] part = { 1, oldEntranceThea, exitThea,
					trees[oldTreeIndex][2] + unitRadius, oldTreeIndex, 0,
					delta[0], delta[1], deltaa };
			add = !add;
			paths.set(newPathIndex,
					JaMa.appendArFloatAr(paths.get(newPathIndex), part));
			pathBack(unitRadius, newTreeIndex, finTarLoc, newEntranceThea,
					newPathIndex, add);
		} else {
			System.out.println("Outer Segment Detection, closest tree is: "
					+ outerTreeIndex);
			circCircTans(unitRadius, oldTreeIndex, outerTreeIndex, finTarLoc,
					oldEntranceThea, newPathIndex, add);
		}

	}

	/**
	 * Path follow
	 */

	private void sortPaths() {
		float[] sums = new float[paths.size()];
		for (int p = 0; p < paths.size(); p++) {
			// run through each part of the path add up the length.
			int checkIndex = 0;
			while (checkIndex < paths.get(p).length) {
				if (paths.get(p)[checkIndex] == 0) {
					sums[p] += paths.get(p)[checkIndex + 3];
					checkIndex += 4;
				} else if (paths.get(p)[checkIndex] == 1) {
					// find deltaThea and then multiple that by length.
					float arcLength = Math.abs(Vect2d.theaSub(
							paths.get(p)[checkIndex + 1],
							paths.get(p)[checkIndex + 2])
							* paths.get(p)[checkIndex + 3]);
					sums[p] += arcLength;
					checkIndex += 5;
				}
			}
		}
		// pick the shortest path and set it as path.
		if (sums.length == 0) {
			path = new float[0];
		} else {
			int shortest = 0;
			for (int s = 1; s < sums.length; s++) {
				if (sums[s] < sums[shortest]) {
					shortest = s;
				}
			}
			path = paths.get(shortest);
		}
	}

	/**
	 * Vector Methods
	 */

	private int segmentInterAnyTree(float unitRadius, float[][] seg) {
		// Runs through all trees and returns the index of the tree that is the
		// closest intersection from seg[0].
		// Return -1 if no trees intersect.

		float[] vect = Vect2d.vectSub(seg[1], seg[0]);
		float sega = Vect2d.norm(vect);

		ArrayList<float[]> scalars = new ArrayList<float[]>();
		for (int t = 0; t < trees.length; t++) {
			float treeDist = Vect2d.norm(new float[] { trees[t][0] - seg[0][0],
					trees[t][1] - seg[0][1] });
			// This is the unnecessary check.
			if (treeDist - trees[t][2] < sega) {
				// make tree relative player and see if it intersects with delta
				if (distPointToVect(Vect2d.vectSub(new float[] { trees[t][0],
						trees[t][1] }, seg[0]), vect) < trees[t][2]
						+ unitRadius) {
					// Tree(s) which intersect.
					// Now find at what scalar of delta they intersect.
					float[] theseScalars = scalarOfVectOnCirc(seg[0],
							unitRadius,
							new float[] { trees[t][0], trees[t][1] },
							trees[t][2], vect);
					scalars.add(new float[] { theseScalars[0], theseScalars[1],
							t });
				}
			}
		}
		// Search thought scalars and find the lowest one.
		int lowestTree = -1;
		int lowS = -1;
		boolean zer = true;
		if (scalars.size() != 0) {
			lowestTree = (int) scalars.get(0)[2];
			float lowestScalar = scalars.get(0)[0];
			lowS = 0;
			for (int s = 0; s < scalars.size(); s++) {
				if (scalars.get(s)[0] < lowestScalar) {
					lowestScalar = scalars.get(s)[0];
					lowestTree = (int) scalars.get(s)[2];
					lowS = s;
					zer = true;
				}
				if (scalars.get(s)[1] < lowestScalar) {
					lowestScalar = scalars.get(s)[1];
					lowestTree = (int) scalars.get(s)[2];
					lowS = s;
					zer = false;
				}
			}
		}
		return lowestTree;
	}

	private int segmentInterAnyTreeIgnore(float unitRadius, float[][] seg,
			int ignore) {
		// Runs through all trees and returns the index of the tree that is the
		// closest intersection from seg[0].
		// Return -1 if no trees intersect.

		float[] vect = Vect2d.vectSub(seg[1], seg[0]);
		float sega = Vect2d.norm(vect);
		ArrayList<float[]> scalars = new ArrayList<float[]>();
		for (int t = 0; t < trees.length; t++) {
			if (t != ignore) {
				float treeDist = Vect2d.norm(new float[] {
						trees[t][0] - seg[0][0], trees[t][1] - seg[0][1] });
				// This is the unnecessary check.
				if (treeDist - trees[t][2] < sega) {
					// make tree relative player and see if it intersects with
					// delta
					if (distPointToVect(
							Vect2d.vectSub(new float[] { trees[t][0],
									trees[t][1] }, seg[0]), vect) < trees[t][2]
							+ unitRadius) {
						// Tree(s) which intersect.
						// Now find at what scalar of delta they intersect.
						float[] theseScalars = scalarOfVectOnCirc(seg[0],
								unitRadius, new float[] { trees[t][0],
										trees[t][1] }, trees[t][2], vect);
						scalars.add(new float[] { theseScalars[0],
								theseScalars[1], t });
					}
				}
			}
		}
		// Search thought scalars and find the lowest one.
		int lowestTree = -1;
		int lowS = -1;
		boolean zer = true;
		if (scalars.size() != 0) {
			lowestTree = (int) scalars.get(0)[2];
			float lowestScalar = scalars.get(0)[0];
			lowS = 0;
			for (int s = 0; s < scalars.size(); s++) {
				if (scalars.get(s)[0] < lowestScalar) {
					lowestScalar = scalars.get(s)[0];
					lowestTree = (int) scalars.get(s)[2];
					lowestTree = (int) scalars.get(s)[2];
					lowS = s;
					zer = true;
				}
				if (scalars.get(s)[1] < lowestScalar) {
					lowestScalar = scalars.get(s)[1];
					lowestTree = (int) scalars.get(s)[2];
					lowestTree = (int) scalars.get(s)[2];
					lowS = s;
					zer = false;
				}
			}
		}
		return lowestTree;
	}

	private int segmentInterAnyTreeIgnore2(float unitRadius, float[][] seg,
			int ignore1, int ignore2) {
		// Runs through all trees and returns the index of the tree that is the
		// closest intersection from seg[0].
		// Return -1 if no trees intersect.

		float[] vect = Vect2d.vectSub(seg[1], seg[0]);
		float sega = Vect2d.norm(vect);
		ArrayList<float[]> scalars = new ArrayList<float[]>();
		for (int t = 0; t < trees.length; t++) {
			if (!(t == ignore1 || t == ignore2)) {
				float treeDist = Vect2d.norm(new float[] {
						trees[t][0] - seg[0][0], trees[t][1] - seg[0][1] });
				// This is the unnecessary check.
				if (treeDist - trees[t][2] < sega) {
					// make tree relative player and see if it intersects with
					// delta
					if (distPointToVect(
							Vect2d.vectSub(new float[] { trees[t][0],
									trees[t][1] }, seg[0]), vect) < trees[t][2]
							+ unitRadius) {
						// Tree(s) which intersect.
						// Now find at what scalar of delta they intersect.
						float[] theseScalars = scalarOfVectOnCirc(seg[0],
								unitRadius, new float[] { trees[t][0],
										trees[t][1] }, trees[t][2], vect);
						scalars.add(new float[] { theseScalars[0],
								theseScalars[1], t });
					}
				}
			}
		}
		// Search thought scalars and find the lowest one.
		int lowestTree = -1;
		int lowS = -1;
		boolean zer = true;
		if (scalars.size() != 0) {
			lowestTree = (int) scalars.get(0)[2];
			float lowestScalar = scalars.get(0)[0];
			lowS = 0;
			for (int s = 0; s < scalars.size(); s++) {
				if (scalars.get(s)[0] < lowestScalar) {
					lowestScalar = scalars.get(s)[0];
					lowestTree = (int) scalars.get(s)[2];
					lowestTree = (int) scalars.get(s)[2];
					lowS = s;
					zer = true;
				}
				if (scalars.get(s)[1] < lowestScalar) {
					lowestScalar = scalars.get(s)[1];
					lowestTree = (int) scalars.get(s)[2];
					lowestTree = (int) scalars.get(s)[2];
					lowS = s;
					zer = false;
				}
			}
		}
		return lowestTree;
	}

	private float distPointToVect(float[] point, float[] vect) {
		// Project point onto vect.
		// If projection scalar is greater than 1 or less than 0
		// then take hypotenuse of closest and edge and point.
		// if the scalar is on the line then reject and that is dist.

		float dist;
		float projScalar = Vect2d.scalarOfProject(point, vect);
		if (projScalar > 1) {
			// get dist from the end of seg.
			dist = Vect2d.norm(Vect2d.vectSub(point, vect));
		} else if (projScalar < 0) {
			// get the dist from the start of seg.
			dist = Vect2d.norm(point);
		} else {
			// dist is point rej proj
			dist = Vect2d.norm(Vect2d.vectSub(point,
					Vect2d.vectMultScalar(projScalar, vect)));
		}
		return dist;
	}

	private float[] scalarOfVectOnCirc(float[] play, float playR, float[] circ,
			float circR, float[] vect) {
		/**
		 * Need to handle if vectX is zero.
		 */

		// get point slope formula of the vect.
		// relative 0? for now.
		float yinter = vect[1] * -play[0] / vect[0] + play[1];
		float m = vect[1] / vect[0];
		float a = m * m + 1;
		float b = 2 * m * yinter - 2 * circ[0] - 2 * m * circ[1];
		float c = circ[0] * circ[0] + yinter * yinter + circ[1] * circ[1] - 2
				* yinter * circ[1] - (playR + circR) * (playR + circR);
		float[] quad = quadEq(a, b, c);
		// subtract playLoc from quad. or dont.
		// use x to get vect y. or use x to get scalar of vect.
		float vectX1 = quad[0] - play[0];
		float xScale1 = vectX1 / vect[0];

		float vectX2 = quad[1] - play[0];
		float xScale2 = vectX2 / vect[0];

		return new float[] { xScale1, xScale2 };
	}

	private float[] quadEq(float a, float b, float c) {
		float ans1 = (float) (-b + Math.sqrt(b * b - 4 * a * c)) / (2 * a);
		float ans2 = (float) (-b - Math.sqrt(b * b - 4 * a * c)) / (2 * a);
		float[] answ = new float[0];
		try {
			answ = JaMa.appendFloatAr(answ, ans1);
		} catch (Exception ex) {
		}
		try {
			answ = JaMa.appendFloatAr(answ, ans2);
		} catch (Exception ex) {
		}
		return answ;
	}

	private float[][] tan2circ(float[] c1Loc, float c1r, float[] c2Loc,
			float c2r, boolean add) {
		// make everything relative c1Loc
		float[] c1toc2 = Vect2d.vectSub(c2Loc, c1Loc);
		// ratio of c1r to c2r
		float c1ratc2 = c1r / (c1r + c2r);
		// mid point is vect * ratio;
		float[] midPoint = Vect2d.vectMultScalar(c1ratc2, c1toc2);
		// get tangent of the midpoint on both circles.

		float[][] al1 = getTangentPoints(midPoint, 0, new float[] { 0, 0 }, c1r);
		float[][] al2 = getTangentPoints(midPoint, 0, c1toc2, c2r);
		if (!add) {
			float[][] fag = {
					new float[] { c1Loc[0] + midPoint[0] + al1[0][0],
							c1Loc[1] + midPoint[1] + al1[0][1] },
					new float[] { c1Loc[0] + midPoint[0] + al2[0][0],
							c1Loc[1] + midPoint[1] + al2[0][1] } };
			return fag;
		} else {
			float[][] fag = {
					new float[] { c1Loc[0] + midPoint[0] + al1[1][0],
							c1Loc[1] + midPoint[1] + al1[1][1] },
					new float[] { c1Loc[0] + midPoint[0] + al2[1][0],
							c1Loc[1] + midPoint[1] + al2[1][1] } };
			return fag;
		}
	}

	private float[][] outerEdgeOne(float[] tanPointRelTwo, float[] circOne,
			float[] circTwo, float circTwoR, boolean add) {
		float[] absTanPoint = Vect2d.vectAdd(tanPointRelTwo, circTwo);
		float[] tanPointRelOne = Vect2d.vectSub(absTanPoint, circOne);
		// scale part1 to pointR
		float[] scaledTanPointRelOne = Vect2d.scaleVectTo(tanPointRelOne,
				circTwoR);
		float[] absTanPointOffTwo = Vect2d.vectAdd(circTwo,
				scaledTanPointRelOne);

		float[] tangentLineSub = Vect2d.vectSub(absTanPoint, circTwo);

		if (add) {
			float[][] seg = { absTanPointOffTwo,
					Vect2d.vectAdd(absTanPointOffTwo, tangentLineSub) };
			return seg;
		} else {
			float[][] seg = { absTanPointOffTwo,
					Vect2d.vectAdd(absTanPointOffTwo, tangentLineSub) };
			return seg;
		}
	}

	private float[][] getOuterAdjPre(int oldTreeIndex, float circOneR,
			int newTreeIndex, float circTwoR, float oldEntranceThea,
			int pathIndex, boolean add) {
		System.out.println("*getOuterAdjPre*");
		float[][] seg;
		if (circTwoR > circOneR) {
			// System.out.println("greater than.");
			seg = getOuterAdjTrim(newTreeIndex, circTwoR, oldTreeIndex,
					circOneR, oldEntranceThea, pathIndex, !add);
			float[] temp = seg[0];
			seg[0] = seg[1];
			seg[1] = temp;
		} else if (circTwoR == circOneR) {
			// System.out.println("equal to");
			float thea = Vect2d.pointToThea(Vect2d.vectSub(trees[oldTreeIndex],
					trees[newTreeIndex]));
			float addThea = Vect2d.theaAdd(thea, (float) Math.PI / 2);
			float subThea = Vect2d.theaSub(thea, (float) Math.PI / 2);
			float[] point;
			if (add) {
				point = Vect2d.theaToPoint(addThea, circTwoR);
			} else {
				point = Vect2d.theaToPoint(subThea, circTwoR);
			}
			seg = new float[][] { Vect2d.vectAdd(trees[oldTreeIndex], point),
					Vect2d.vectAdd(trees[newTreeIndex], point) };
		} else {
			// System.out.println("less than");
			seg = getOuterAdjTrim(oldTreeIndex, circOneR, newTreeIndex,
					circTwoR, oldEntranceThea, pathIndex, add);
		}
		return seg;
	}

	private float[][] getOuterAdjTrim(int oldTreeIndex, float circOneR,
			int newTreeIndex, float circTwoR, float oldEntranceThea,
			int pathIndex, boolean add) {
		// return segment from oldTree tan point to newTree tan point
		float[][] tanPs;
		if (circOneR - circTwoR < 0) {
			circTwoR -= (circOneR - circTwoR);
			tanPs = new float[][] { { 0, 0 }, { 0, 0 } };
		} else {
			tanPs = getTangentPoints(trees[newTreeIndex], 0,
					trees[oldTreeIndex], circOneR - circTwoR);
		}
		// scale vect (tree -> tanP) to playRadus.
		// add it to tanP. add it to playLoc.
		// Make a vect of thoes two points.
		if (add) {
			float[][] seg = outerEdgeOne(tanPs[0], trees[oldTreeIndex],
					trees[newTreeIndex], circTwoR, true);

			return new float[][] { seg[1], seg[0] };
		} else {
			float[][] seg = outerEdgeOne(tanPs[1], trees[oldTreeIndex],
					trees[newTreeIndex], circTwoR, false);
			return new float[][] { seg[1], seg[0] };
		}
	}

	private float[][] getTangentPoints(float[] play, float pRad, float[] tree,
			float tRad) {
		// Plug in play circle and tree circle, return the two lines from
		// playLoc to the points tangent tree.
		// All points are relative play.
		// [0 + 1] is add tangent point vect relative to play
		// [2 + 3] is sub tangent point vect relative to play
		// [4] is -1 because it is supposed to set tree index outside this
		// method.
		float[] delta = Vect2d.vectSub(tree, play);
		float hyp = Vect2d.norm(delta);
		float opp = pRad + tRad;
		float adj = (float) Math.sqrt(hyp * hyp - opp * opp);
		float treeThea = Vect2d.pointToThea(delta);
		float shapeThea = Vect2d.pointToThea(new float[] { adj, opp });
		float addThea = Vect2d.theaAdd(treeThea, shapeThea);
		float subThea = Vect2d.theaSub(treeThea, shapeThea);
		float[] addPoint = Vect2d.theaToPoint(addThea, adj);
		float[] subPoint = Vect2d.theaToPoint(subThea, adj);

		return new float[][] { addPoint, subPoint };
	}
}

package ColePole;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JPanel;

import ColePole.lib.JaMa;
import ColePole.lib.Vect2d;

public class PolyCirc extends JPanel implements Runnable, MouseListener,
		KeyListener {

	// FOR GOING AROUND EDGE IN PATH[] IT HAS 5 VARIABLES INSTEAD OF 4-LIKE THE
	// OLD PROGRAM.

	// One moving player circle
	// Multiple static tree circles.

	int width = 540;
	int height = 450;

	Image[] imageAr;

	Thread thread;
	Image image;
	Graphics g;

	// Vars for gLoop Below
	int tps = 20;
	int mpt = 1000 / tps;
	long lastTick = 0;
	int sleepTime = 0;
	long lastSec = 0;
	int ticks = 0;
	long startTime;
	long runTime;
	long nextTick = mpt;
	boolean running = false;

	// Vars for gLoop Above

	boolean mC = false;
	int[] clickInfo = new int[3];

	float[] pLoc = { 120, 160 };
	float pRadius = 20;
	float playSpeed = 12;

	// 0 = x, 1 = y, 2 = radius
	// float[][] trees = { { 200, 100, 40 }, { 250, 210, 30 }, { 220, 310, 30 }
	// };

	float[][] trees = { { 205, 150, 40 }, { 180, 270, 30 } };

	int[] tarLoc = new int[2];

	float[] path = new float[0];

	public PolyCirc() {
		super();

		setPreferredSize(new Dimension(width, height));
		setFocusable(true);
		requestFocus();
	}

	public void addNotify() {
		super.addNotify();
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}

	public void run() {
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		g = (Graphics2D) image.getGraphics();
		this.setSize(new Dimension(width, height));

		addMouseListener(this);
		addKeyListener(this);

		startTime = System.currentTimeMillis();
		gStart();
	}

	/**
	 * Methods go below here.
	 * 
	 */

	public void gStart() {
		// getOuterAdj(new float[] { 50, 50 }, 20, new float[] { 90, 40 }, 10);

		float thea1 = (float) Math.PI / 180 * 170;
		float thea2 = (float) Math.PI / 180 * 115;
		System.out.println(180 / Math.PI * Vect2d.theaSub(thea1, thea2));

		running = true;
		gLoop();
	}

	boolean moving = false;

	public void gLoop() {
		while (running) {
			// Do the things you want the gLoop to do below here

			clickHande();
			drawScene();

			if (makePath) {
				moving = false;
				makePath(pLoc, new float[] { (float) tarLoc[0],
						(float) tarLoc[1] }, 0);
				makePath = false;
			}

			if (moving) {
				playSpeedLeft = playSpeed;
				followPath();
			}

			drwGm();

			// And above here.

			ticks++;

			// Runs once a second and keeps track of ticks;
			// 1000 ms since last output
			if (timer() - lastSec > 1000) {
				if (ticks < tps - 1 || ticks > tps + 1) {
					if (timer() - startTime < 2000) {
						System.out.println("Ticks this second: " + ticks);
						System.out.println("timer(): " + timer());
						System.out.println("nextTick: " + nextTick);
					}
				}

				ticks = 0;
				lastSec = (System.currentTimeMillis() - startTime);
			}

			// Limits the ticks per second

			// if nextTick is later then timer then sleep till next tick
			// System.out.println("nextTick: " + nextTick);
			// System.out.println("timer: " + timer());
			if (nextTick > timer()) {
				sleepTime = (int) (nextTick - timer());
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
				}

				nextTick += mpt;
			}

			// if next tick is past the current time then don't sleep and add
			// time so it runs a tick after adding.
			if (nextTick < timer()) {
				nextTick += mpt;
			}

			// if the game is more than 2 seconds behind it updates the ticks.
			if (nextTick + 2000 < timer()) {
				nextTick = timer() + mpt;
			}
		}
	}

	void clickHande() {
		if (mC) {
			i = 1;
			System.out.println("** click **");
			if (clickInfo[2] == 3) {
				tarLoc[0] = clickInfo[0];
				tarLoc[1] = clickInfo[1];
				makePath = true;
			}
			// scalarOfVectOnCirc(new float[] { 5, 1 }, 1, new float[] { 3, 6 },
			// 3, new float[] { 1, 9 });
			mC = false;
		}
	}

	void testThea() {
		float[] cent = { 200, 200 };
		drawCircle(Color.LIGHT_GRAY, cent, 20);
		float[] deg45 = Vect2d.theaToPoint((float) (Math.PI / 4), 21);
		drawCircle(Color.LIGHT_GRAY, Vect2d.vectAdd(cent, deg45), 4);
		float[] deg45neg = Vect2d.theaToPoint((float) (-Math.PI / 4), 21);
		drawCircle(Color.LIGHT_GRAY, Vect2d.vectAdd(cent, deg45neg), 4);
		float[] deg115 = Vect2d.theaToPoint((float) (Math.PI / 180 * 115), 21);
		drawCircle(Color.LIGHT_GRAY, Vect2d.vectAdd(cent, deg115), 4);
		System.out.println(Vect2d.pointToThea(deg45neg));
	}

	ArrayList<float[]> paths = new ArrayList<float[]>();

	void makePath(float[] tempLoc, float[] finTarLoc, int pathIndex) {
		System.out.println("makePath: " + pathIndex);
		paths.clear();
		float[] curTarLoc = finTarLoc.clone();
		int oldTreeIndex = segmentInterAnyTree(new float[][] { tempLoc,
				finTarLoc });
		if (oldTreeIndex < 0) {
			moving = true;
			float deltaa = Vect2d.norm(Vect2d.vectSub(finTarLoc, tempLoc));
			float[] delta = Vect2d.vectDivScalar(deltaa,
					Vect2d.vectSub(finTarLoc, tempLoc));
			path = new float[] { 0, delta[0], delta[1], deltaa };
			return;
		} else {
			// intersection so 2 paths off the bat.
			int newIndex = paths.size();
			paths.add(new float[0]);
			splitLoopOne(tempLoc, oldTreeIndex, finTarLoc, pathIndex);
			sortPaths();
			// pick shortest path and set it as path.
			// then set moving
			moving = true;
			return;
		}
	}

	// It makes 2 paths initially if there is a collision.
	// It then builds them.
	// So one path needs to be ultimately returned to the first loopOne.
	// Everything else just makes more paths, copies what it had already made.
	// And build it independently so it gets passed down in the end.

	void splitLoopOne(float[] tempLoc, int oldTreeIndex, float[] finTarLoc,
			int pathIndex) {
		System.out.println("*splitLoopOne: " + pathIndex);
		// relative tempLoc
		float[][] tangentPoints = getTangentPoints(tempLoc, pRadius,
				trees[oldTreeIndex], trees[oldTreeIndex][2]);
		loopOne(tempLoc, oldTreeIndex, finTarLoc, tangentPoints[0], pathIndex,
				true);
		pathIndex = paths.size();
		paths.add(new float[0]);
		loopOne(tempLoc, oldTreeIndex, finTarLoc, tangentPoints[1], pathIndex,
				false);
	}

	void loopOne(float[] tempLoc, int oldTreeIndex, float[] finTarLoc,
			float[] tangentPoint, int pathIndex, boolean add) {
		System.out.println("*loopOne: " + pathIndex);
		int newTreeIndex = segmentInterAnyTreeIgnore(new float[][] { tempLoc,
				Vect2d.vectAdd(tempLoc, tangentPoint) }, oldTreeIndex);
		drawLine(Color.ORANGE, tempLoc, Vect2d.vectAdd(tempLoc, tangentPoint));
		if (newTreeIndex < 0) {
			// there are no trees in the way to the tangent point of
			// oldTreeIndex
			// loopTwo(tempLoc, oldTreeIndex, finTarLoc);
			drawCircle(Color.RED, Vect2d.vectAdd(tempLoc, tangentPoint), 6);
			float deltaa = Vect2d.norm(tangentPoint);
			float[] delta = Vect2d.vectDivScalar(deltaa, tangentPoint);
			// give thea of tanP rel tree
			// point to thea (tree - (tempL + tanP))
			float thea = Vect2d
					.pointToThea((Vect2d.vectSub(
							Vect2d.vectAdd(tempLoc, tangentPoint),
							trees[oldTreeIndex])));
			System.out.println("ADD path to");
			paths.set(
					pathIndex,
					JaMa.appendArFloatAr(paths.get(pathIndex), new float[] { 0,
							delta[0], delta[1], deltaa }));
			pathBack(oldTreeIndex, finTarLoc, thea, pathIndex, add);
		} else {
			// it splits in here.
			// return splitLoopOne(tempLoc, newTreeIndex, finTarLoc, pathIndex);
			splitLoopOne(tempLoc, newTreeIndex, finTarLoc, pathIndex);
			// do i set path 0 and path 1? Because it creats more paths in later
			// methods.
			// I need to pass along currentPath and whenever i make a new one
			// just copy current and add it to the end, then build the rest.
			// return new float[0];
		}
	}

	boolean wait = true;

	void sleep(long t) {
		// while (wait) {
		// try {
		// thread.sleep(500);
		//
		// } catch (Exception ex) {
		//
		// }
		// }
		// wait = true;
	}

	int i = 0;

	int dee = 0;

	void pathBack(int oldTreeIndex, float[] finTarLoc, float entranceThea,
			int pathIndex, boolean add) {
		System.out.println("*pathBack: " + pathIndex);
		float[][] tangentPoints = getTangentPoints(finTarLoc, pRadius,
				trees[oldTreeIndex], trees[oldTreeIndex][2]);
		if (i < 2) {
			drawLine(Color.WHITE, finTarLoc,
					Vect2d.vectAdd(finTarLoc, tangentPoints[0]));
			drawLine(Color.WHITE, finTarLoc,
					Vect2d.vectAdd(finTarLoc, tangentPoints[1]));
			drwGm();
			sleep(2000);
		}

		// invert pathBack and do tree detection.
		int newTreeIndex;
		float[] tanPoint;
		if (add) {
			tanPoint = Vect2d.vectAdd(finTarLoc, tangentPoints[1]);
			float[][] seg = new float[][] { tanPoint, finTarLoc };
			if (dee == 1) {
				sleep(2000);
			}
			dee++;
			fillCircle(Color.BLUE, seg[0], 6);
			fillCircle(Color.BLUE, seg[1], 6);
			drawLine(Color.RED, seg[0], seg[1]);
			System.out.println("1.oTI: " + oldTreeIndex);
			newTreeIndex = segmentInterAnyTreeIgnore(seg, oldTreeIndex);
			System.out.println("1.nTI: " + newTreeIndex);
		} else {
			tanPoint = Vect2d.vectAdd(finTarLoc, tangentPoints[0]);
			float[][] seg = new float[][] { tanPoint, finTarLoc };
			fillCircle(Color.MAGENTA, seg[0], 6);
			fillCircle(Color.MAGENTA, seg[1], 6);
			drawLine(Color.RED, seg[0], seg[1]);
			System.out.println("2.oTI: " + oldTreeIndex);
			newTreeIndex = segmentInterAnyTreeIgnore(seg, oldTreeIndex);
			System.out.println("2.nTI: " + newTreeIndex);
		}
		if (newTreeIndex < 0) {
			System.out.println("clear back");
			float[] pointRelTree = Vect2d
					.vectSub(tanPoint, trees[oldTreeIndex]);
			float thea = Vect2d.pointToThea(pointRelTree);

			// make arc section and return arc + final straight.
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
					trees[oldTreeIndex][2] + pRadius, oldTreeIndex, 0,
					delta[0], delta[1], deltaa };
			System.out.println("ADD path back");
			paths.set(pathIndex,
					JaMa.appendArFloatAr(paths.get(pathIndex), path));
			System.out.println("finish path: " + pathIndex);
		} else {
			System.out.println("backTreeIndex: " + newTreeIndex);
			// intersection, split into
			// outside tangents
			// inside tangents
			System.out.println("ADD path back");
			paths.set(pathIndex,
					JaMa.appendArFloatAr(paths.get(pathIndex), path));
			circCircTans(oldTreeIndex, newTreeIndex, finTarLoc, entranceThea,
					pathIndex, add);
		}
	}

	void circCircTans(int oldTreeIndex, int newTreeIndex, float[] finTarLoc,
			float oldEntranceThea, int pathIndex, boolean add) {
		System.out.println("*circCircTans");
		float[][] innerSeg = tan2circ(trees[oldTreeIndex],
				trees[oldTreeIndex][2] + pRadius, trees[newTreeIndex],
				trees[newTreeIndex][2] + pRadius, add);

		// can always pursue inner circ. But outer circ needs a check to see if
		// a straight line to the second circ would not intersect with the first
		// circ.

		/**
		 * How to copy the current path since it has not been returned yet.
		 */
		// Copy old and make new.
		int newPathIndex = paths.size();
		System.out.println("CLONE");
		paths.add(paths.get(pathIndex).clone());

		int innerTreeIndex = segmentInterAnyTreeIgnore2(innerSeg, oldTreeIndex,
				newTreeIndex);
		if (innerTreeIndex < 0) {
			// no collision of inner seg.
			// add
			// Arc
			// Seg
			// do returning check which then develops the second arc.

			// find entrance and exit thea of seg.
			float[] exitRelOld = Vect2d.vectSub(innerSeg[0],
					trees[oldTreeIndex]);
			float exitThea = Vect2d.pointToThea(exitRelOld);
			System.out.println("exitThea: " + exitThea);
			float[] entrRelNew = Vect2d.vectSub(innerSeg[1],
					trees[newTreeIndex]);
			float newEntranceThea = Vect2d.pointToThea(entrRelNew);

			float[] delta = Vect2d.vectSub(innerSeg[1], innerSeg[0]);
			float deltaa = Vect2d.norm(delta);
			delta = Vect2d.vectDivScalar(deltaa, delta);

			// float[] part = { 0, delta[0], delta[1], deltaa };
			float[] part = { 1, oldEntranceThea, exitThea,
					trees[oldTreeIndex][2] + pRadius, oldTreeIndex, 0,
					delta[0], delta[1], deltaa };
			add = !add;
			if (add) {
				System.out.println("ADD inner vect");
				paths.set(pathIndex,
						JaMa.appendArFloatAr(paths.get(pathIndex), part));
				pathBack(newTreeIndex, finTarLoc, newEntranceThea, pathIndex,
						add);
			} else {
				System.out.println("ADD inner vect");
				paths.set(pathIndex,
						JaMa.appendArFloatAr(paths.get(pathIndex), part));
				pathBack(newTreeIndex, finTarLoc, newEntranceThea, pathIndex,
						add);
			}
		} else {
			// inner collisions
			// return new float[0];
		}

		float[][] outerSeg = getOuterAdjPre(oldTreeIndex,
				trees[oldTreeIndex][2] + pRadius, newTreeIndex,
				trees[newTreeIndex][2] + pRadius, oldEntranceThea, pathIndex,
				add);

		fillCircle(Color.YELLOW, outerSeg[0], 8);
		fillCircle(Color.RED, outerSeg[1], 12);
		// fillCircle(Color.ORANGE, outerSeg[0], 12);

		int outerTreeIndex = segmentInterAnyTreeIgnore2(outerSeg, oldTreeIndex,
				newTreeIndex);
		if (outerTreeIndex < 0) {
			float[] exitRelOld = Vect2d.vectSub(outerSeg[0],
					trees[oldTreeIndex]);

			float exitThea = Vect2d.pointToThea(exitRelOld);
			System.out.println("exitThea: " + exitThea);

			float[] entrRelNew = Vect2d.vectSub(outerSeg[1],
					trees[newTreeIndex]);
			float newEntranceThea = Vect2d.pointToThea(entrRelNew);

			float[] delta = Vect2d.vectSub(outerSeg[1], outerSeg[0]);
			float deltaa = Vect2d.norm(delta);
			delta = Vect2d.vectDivScalar(deltaa, delta);

			// float[] part = { 0, delta[0], delta[1], deltaa };
			float[] part = { 1, oldEntranceThea, exitThea,
					trees[oldTreeIndex][2] + pRadius, oldTreeIndex, 0,
					delta[0], delta[1], deltaa };
			add = !add;
			System.out.println("ADD outer vect");
			paths.set(newPathIndex,
					JaMa.appendArFloatAr(paths.get(newPathIndex), part));
			pathBack(newTreeIndex, finTarLoc, newEntranceThea, newPathIndex,
					add);
		} else {

		}

		// float[] exitRelOld = Vect2d.vectSub(seg[0], trees[oldTreeIndex]);
		// float exitThea = Vect2d.pointToThea(exitRelOld);
		// float[] arc = { 1, oldEntranceThea, exitThea,
		// trees[oldTreeIndex][2] + pRadius, oldTreeIndex };
		//
		// pathBack(newTreeIndex, finTarLoc, newEntranceThea, pathIndex, add);
	}

	// gets tangent points.
	// Don't normalize before returning
	// Don't return the magnitude.

	float[][] getTangentPoints(float[] play, float pRad, float[] tree,
			float tRad) {
		// Plug in play circle and tree circle, return the two lines from
		// playLoc to the points tangent tree.
		// I BELIEIVE ALL POINTS ARE RELATIVE PLAYER
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
		// make sub thea and plus thea relative to tree.
		// plus point minus tree
		// drawCircle(Color.ORANGE, Vect2d.vectAdd(play, addPoint), 4);
		// drawCircle(Color.MAGENTA, Vect2d.vectAdd(play, subPoint), 4);
		return new float[][] { addPoint, subPoint };
	}

	/**
	 * Path follow
	 */

	float playSpeedLeft = 0;

	void followPath() {
		while (path.length > 0 && playSpeedLeft > 0) {
			// System.out.println("path.l: " + path.length);
			// System.out.println("while");
			sortFollowPath();
		}
		if (path.length == 0) {
			// System.out.println("stop moving");
			moving = false;
		}
	}

	void sortFollowPath() {
		if (path[0] == 0) {
			// System.out.println("path 0");
			// linear so go straight
			if (path[3] > playSpeedLeft) {
				pLoc[0] += path[1] * playSpeedLeft;
				pLoc[1] += path[2] * playSpeedLeft;
				path[3] -= playSpeedLeft;
				playSpeedLeft = 0;
			} else {
				pLoc[0] += path[1] * path[3];
				pLoc[1] += path[2] * path[3];
				playSpeedLeft -= path[3];
				// delete the four. and move on.
				// System.out.println("remove4");
				path = JaMa.removeFirstFloatAr(path, 4);
			}
		} else if (path[0] == 1) {
			// System.out.println("path 1");
			// around edge
			System.out.println("sub: "
					+ Math.abs(Vect2d.theaSub(path[1], path[2])));
			float edgeLength = Math.abs(Vect2d.theaSub(path[1], path[2])
					* path[3]);
			if (edgeLength > playSpeedLeft) {
				// figure out if plus thea is closer or minus thea, then move
				// accordingly inorder to fuffil moveSpeedLeft.
				float possableThea = playSpeedLeft / path[3];
				float newThea;
				System.out.println("path[1]" + path[1]);
				System.out.println("path[2]" + path[2]);
				if (Vect2d.theaSub(path[1], path[2]) < 0) {
					newThea = Vect2d.theaAdd(path[1], possableThea);
				} else {
					newThea = Vect2d.theaSub(path[1], possableThea);
				}
				path[1] = newThea;
				float[] newLoc = Vect2d.theaToPoint(newThea, path[3]);
				pLoc[0] = trees[(int) path[4]][0] + newLoc[0];
				pLoc[1] = trees[(int) path[4]][1] + newLoc[1];
				// pathing = false;
				playSpeedLeft = 0;
			} else {
				float[] newLoc = Vect2d.theaToPoint(path[2], path[3]);
				pLoc[0] = trees[(int) path[4]][0] + newLoc[0];
				pLoc[1] = trees[(int) path[4]][1] + newLoc[1];
				path = JaMa.removeFirstFloatAr(path, 5);
				playSpeedLeft -= edgeLength;
			}
		}
	}

	void sortPaths() {
		float[] sums = new float[paths.size()];
		// System.out.println("paths.s: " + paths.size());
		for (int p = 0; p < paths.size(); p++) {
			// run through each part of the path add up the length.
			int checkIndex = 0;
			while (checkIndex < paths.get(p).length) {
				System.out.println("while: " + checkIndex);
				try {
					System.out.println("paths.get(p)[checkIndex-3]: "
							+ paths.get(p)[checkIndex - 3]);
				} catch (Exception ex) {

				}
				try {
					System.out.println("paths.get(p)[checkIndex-2]: "
							+ paths.get(p)[checkIndex - 2]);
				} catch (Exception ex) {

				}
				try {
					System.out.println("paths.get(p)[checkIndex-1]: "
							+ paths.get(p)[checkIndex - 1]);
				} catch (Exception ex) {

				}
				try {
					System.out.println("paths.get(p)[checkIndex]: "
							+ paths.get(p)[checkIndex]);
				} catch (Exception ex) {

				}
				try {
					System.out.println("paths.get(p)[checkIndex+1]: "
							+ paths.get(p)[checkIndex + 1]);
				} catch (Exception ex) {

				}
				try {
					System.out.println("paths.get(p)[checkIndex+2]: "
							+ paths.get(p)[checkIndex + 2]);
				} catch (Exception ex) {

				}
				try {
					System.out.println("paths.get(p)[checkIndex+3]: "
							+ paths.get(p)[checkIndex + 3]);
				} catch (Exception ex) {

				}
				if (paths.get(p)[checkIndex] == 0) {
					System.out.println("linear add: "
							+ paths.get(p)[checkIndex + 3]);
					sums[p] += paths.get(p)[checkIndex + 3];
					checkIndex += 4;
					System.out.println("checkI: " + checkIndex);
				} else if (paths.get(p)[checkIndex] == 1) {
					// find deltaThea and then multiple that by length.
					float arcLength = Math.abs(Vect2d.theaSub(
							paths.get(p)[checkIndex + 1],
							paths.get(p)[checkIndex + 2])
							* paths.get(p)[checkIndex + 3]);
					System.out.println("arcLength: " + arcLength);
					sums[p] += arcLength;
					checkIndex += 5;
					System.out.println("checkI5: " + checkIndex);
				}
			}
		}
		// pick the shortest path and set it as path.
		if (sums.length == 0) {
			path = new float[0];
		} else {
			int shortest = 0;
			System.out.println("paths(0).l: " + paths.get(0).length);
			System.out.println("sum[0]: " + sums[0]);
			for (int s = 1; s < sums.length; s++) {
				System.out.println("paths(" + s + "): " + paths.get(s).length);
				System.out.println("sum[" + s + "]: " + sums[1]);
				if (sums[s] < sums[shortest]) {
					shortest = s;
				}
			}
			path = paths.get(shortest);
			System.out.println("shortest: " + shortest);
		}
	}

	/**
	 * Vector Methods
	 */

	// Does this segment intersect with any trees.
	// If NO return -1.
	// If SO return the closest tree's index.
	int segmentInterAnyTree(float[][] seg) {
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
				System.out
						.println("distPointToVect(Vect2d.vectSub(new float[] { trees[t][0], trees[t][1] }, pLoc), vect): "
								+ distPointToVect(Vect2d.vectSub(new float[] {
										trees[t][0], trees[t][1] }, seg[0]),
										vect));
				System.out.println("t: " + t);
				if (distPointToVect(Vect2d.vectSub(new float[] { trees[t][0],
						trees[t][1] }, seg[0]), vect) < trees[t][2] + pRadius) {
					// Tree(s) which intersect.
					// Now find at what scalar of delta they intersect.
					float[] theseScalars = scalarOfVectOnCirc(seg[0], pRadius,
							new float[] { trees[t][0], trees[t][1] },
							trees[t][2], vect);
					// Vect2d.sayVect("theseScalars", theseScalars);
					scalars.add(new float[] { theseScalars[0], theseScalars[1],
							t });
					System.out.println("addScalar: " + t);
				}
			}
		}
		// Search thought scalars and find the lowest one.
		int lowestTree = -1;
		if (scalars.size() != 0) {
			lowestTree = (int) scalars.get(0)[2];
			float lowestScalar = scalars.get(0)[0];
			for (int s = 1; s < scalars.size(); s++) {
				// System.out.println("s [0] : " + scalars.get(s)[0] +
				// "    [1] : " + scalars.get(s)[1]);
				if (scalars.get(s)[0] < lowestScalar) {
					lowestScalar = scalars.get(s)[0];
					lowestTree = (int) scalars.get(s)[2];
				}
				if (scalars.get(s)[1] < lowestScalar) {
					lowestScalar = scalars.get(s)[1];
					lowestTree = (int) scalars.get(s)[2];
				}
			}
		}
		return lowestTree;
	}

	int segmentInterAnyTreeIgnore(float[][] seg, int ignore) {
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
					System.out
							.println("distPointToVect(Vect2d.vectSub(new float[] { trees[t][0], trees[t][1] }, pLoc), vect): "
									+ distPointToVect(Vect2d.vectSub(
											new float[] { trees[t][0],
													trees[t][1] }, seg[0]),
											vect));
					System.out.println("t: " + t);
					if (distPointToVect(
							Vect2d.vectSub(new float[] { trees[t][0],
									trees[t][1] }, seg[0]), vect) < trees[t][2]
							+ pRadius) {
						// Tree(s) which intersect.
						// Now find at what scalar of delta they intersect.
						float[] theseScalars = scalarOfVectOnCirc(seg[0],
								pRadius,
								new float[] { trees[t][0], trees[t][1] },
								trees[t][2], vect);
						// Vect2d.sayVect("theseScalars", theseScalars);
						scalars.add(new float[] { theseScalars[0],
								theseScalars[1], t });
						System.out.println("addScalarI: " + t);
					}
				}
			}
		}
		// Search thought scalars and find the lowest one.
		int lowestTree = -1;
		if (scalars.size() != 0) {
			lowestTree = (int) scalars.get(0)[2];
			float lowestScalar = scalars.get(0)[0];
			for (int s = 1; s < scalars.size(); s++) {
				System.out.println("s [0] : " + scalars.get(s)[0]
						+ "    [1] : " + scalars.get(s)[1]);
				if (scalars.get(s)[0] < lowestScalar) {
					lowestScalar = scalars.get(s)[0];
					lowestTree = (int) scalars.get(s)[2];
				}
				if (scalars.get(s)[1] < lowestScalar) {
					lowestScalar = scalars.get(s)[1];
					lowestTree = (int) scalars.get(s)[2];
				}
			}
		}
		return lowestTree;
	}

	int segmentInterAnyTreeIgnore2(float[][] seg, int ignore1, int ignore2) {
		System.out.println("*segmentInterAnyTreeIgnore2: " + ignore1
				+ "   ignore2: " + ignore2);
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
							+ pRadius) {
						// Tree(s) which intersect.
						// Now find at what scalar of delta they intersect.
						float[] theseScalars = scalarOfVectOnCirc(seg[0],
								pRadius,
								new float[] { trees[t][0], trees[t][1] },
								trees[t][2], vect);
						// Vect2d.sayVect("theseScalars", theseScalars);
						scalars.add(new float[] { theseScalars[0],
								theseScalars[1], t });
					}
				}
			}
		}
		// Search thought scalars and find the lowest one.
		int lowestTree = -1;
		if (scalars.size() != 0) {
			lowestTree = (int) scalars.get(0)[2];
			float lowestScalar = scalars.get(0)[0];
			for (int s = 1; s < scalars.size(); s++) {
				System.out.println("s [0] : " + scalars.get(s)[0]
						+ "    [1] : " + scalars.get(s)[1]);
				if (scalars.get(s)[0] < lowestScalar) {
					lowestScalar = scalars.get(s)[0];
					lowestTree = (int) scalars.get(s)[2];
				}
				if (scalars.get(s)[1] < lowestScalar) {
					lowestScalar = scalars.get(s)[1];
					lowestTree = (int) scalars.get(s)[2];
				}
			}
		}
		return lowestTree;
	}

	float distPointToVect(float[] point, float[] vect) {
		// project, is projection scalar is farther than the line then take
		// hypotnuse of closest and edge and point. if the scalar is on the line
		// then reject and that is dist.

		float dist;
		float projScalar = Vect2d.scalarOfProject(point, vect);
		// System.out.println("projScalar: " + projScalar);
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

	float[] scalarOfVectOnCirc(float[] play, float playR, float[] circ,
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
		float vectY1 = vect[1] * xScale1;

		float vectX2 = quad[1] - play[0];
		float xScale2 = vectX2 / vect[0];
		float vectY2 = vect[1] * xScale2;

		return new float[] { xScale1, xScale2 };
	}

	float[] quadEq(float a, float b, float c) {
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

	// I want blue green when going down right.
	// I only want one of the tangent lines.
	float[][] tan2circ(float[] c1Loc, float c1r, float[] c2Loc, float c2r,
			boolean add) {
		// make everything relative c1Loc
		// Draws the two circle
		drawCircle(Color.RED, c1Loc, c1r);
		drawCircle(Color.RED, c2Loc, c2r);
		// find mid point
		// circle 1 to circle 2 vect
		float[] c1toc2 = Vect2d.vectSub(c2Loc, c1Loc);
		// ratio of c1r to c2r
		float c1ratc2 = c1r / (c1r + c2r);
		// mid point is vect * ratio;
		float[] midPoint = Vect2d.vectMultScalar(c1ratc2, c1toc2);
		// get tangent of the midpoint on both circles.

		// draws midpoint.
		drawCircle(Color.RED, Vect2d.vectAdd(c1Loc, midPoint), 2);
		// get four tangent points.
		float[][] al1 = getTangentPoints(midPoint, 0, new float[] { 0, 0 }, c1r);
		float[][] al2 = getTangentPoints(midPoint, 0, c1toc2, c2r);
		if (!add) {

			drawLine(new Color(255, 255, 255), new float[] {
					c1Loc[0] + midPoint[0] + al1[0][0],
					c1Loc[1] + midPoint[1] + al1[0][1] }, new float[] {
					c1Loc[0] + midPoint[0] + al2[0][0],
					c1Loc[1] + midPoint[1] + al2[0][1] });
			float[][] fag = {
					new float[] { c1Loc[0] + midPoint[0] + al1[0][0],
							c1Loc[1] + midPoint[1] + al1[0][1] },
					new float[] { c1Loc[0] + midPoint[0] + al2[0][0],
							c1Loc[1] + midPoint[1] + al2[0][1] } };
			return fag;
		} else {
			drawLine(Color.BLUE, new float[] {
					c1Loc[0] + midPoint[0] + al1[1][0],
					c1Loc[1] + midPoint[1] + al1[1][1] }, new float[] {
					c1Loc[0] + midPoint[0] + al2[1][0],
					c1Loc[1] + midPoint[1] + al2[1][1] });
			float[][] fag = {
					new float[] { c1Loc[0] + midPoint[0] + al1[1][0],
							c1Loc[1] + midPoint[1] + al1[1][1] },
					new float[] { c1Loc[0] + midPoint[0] + al2[1][0],
							c1Loc[1] + midPoint[1] + al2[1][1] } };
			return fag;
		}
	}

	// retuns a seg
	float[][] outerEdgeOne(float[] tanPointRelTwo, float[] circOne,
			float[] circTwo, float circTwoR, boolean add) {
		// solo part is the tangent part.
		float[] absTanPoint = Vect2d.vectAdd(tanPointRelTwo, circTwo);
		// float[] partSub = Vect2d.vectSub(point, soloPart);
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
			drawLine(Color.BLUE, absTanPointOffTwo,
					Vect2d.vectAdd(absTanPointOffTwo, tangentLineSub));
			return seg;
		} else {
			float[][] seg = { absTanPointOffTwo,
					Vect2d.vectAdd(absTanPointOffTwo, tangentLineSub) };
			drawLine(Color.RED, absTanPointOffTwo,
					Vect2d.vectAdd(absTanPointOffTwo, tangentLineSub));
			return seg;
		}
	}

	float[][] getOuterAdjPre(int oldTreeIndex, float circOneR,
			int newTreeIndex, float circTwoR, float oldEntranceThea,
			int pathIndex, boolean add) {
		float[][] seg;
		if (circTwoR > circOneR) {
			seg = getOuterAdjTrim(newTreeIndex, circTwoR, oldTreeIndex,
					circOneR, oldEntranceThea, pathIndex, !add);
			float[] temp = seg[0];
			seg[0] = seg[1];
			seg[1] = temp;
		} else {
			seg = getOuterAdjTrim(oldTreeIndex, circOneR, newTreeIndex,
					circTwoR, oldEntranceThea, pathIndex, add);
		}
		return seg;
	}

	// old tree loc + radius
	// new tree loc + radius
	float[][] getOuterAdjTrim(int oldTreeIndex, float circOneR,
			int newTreeIndex, float circTwoR, float oldEntranceThea,
			int pathIndex, boolean add) {
		// return segment from oldTree tan point to newTree tan point
		System.out.println("getOuterAdjTrim");
		float[][] tanPs;
		System.out.println("circRad - pointR: " + (circOneR - circOneR));
		if (circOneR - circTwoR < 0) {
			circTwoR -= (circOneR - circTwoR);
			tanPs = new float[][] { { 0, 0 }, { 0, 0 } };
		} else {
			tanPs = getTangentPoints(trees[newTreeIndex], 0,
					trees[oldTreeIndex], circOneR - circTwoR);
		}
		drawCircle(Color.YELLOW, Vect2d.vectAdd(trees[newTreeIndex], tanPs[0]),
				10);
		drawCircle(Color.YELLOW, Vect2d.vectAdd(trees[newTreeIndex], tanPs[1]),
				10);
		// scale vect tree -> tanP to playRadus.
		// add it to tanP. add it to playLoc.
		// Make a vect of thoes two points.
		if (add) {
			float[][] seg = outerEdgeOne(tanPs[0], trees[oldTreeIndex],
					trees[newTreeIndex], circTwoR, true);

			float[] vect = Vect2d.vectSub(seg[1], seg[0]);
			// paths.set(pathIndex,
			// JaMa.appendArFloatAr(paths.get(pathIndex), vect));
			// return seg;
			return new float[][] { seg[1], seg[0] };
		} else {
			float[][] seg = outerEdgeOne(tanPs[1], trees[oldTreeIndex],
					trees[newTreeIndex], circTwoR, false);
			float[] vect = Vect2d.vectSub(seg[1], seg[0]);
			// return seg;
			return new float[][] { seg[1], seg[0] };
			// paths.set(pathIndex,
			// JaMa.appendArFloatAr(paths.get(pathIndex), new float[] {1, 0,
			// vect[0]));
		}
	}

	/**
	 * Drawing
	 */

	void drawScene() {
		// testThea();
		// tan2circ(new float[] { 80, 160 }, 20, new float[] { 100, 60 }, 50,
		// false);
		// draws background
		if (drawBackground) {
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, width, height);
			drawBackground = false;
		}
		// draws trees
		for (int t = 0; t < trees.length; t++) {
			drawCircle(Color.GREEN, trees[t], trees[t][2]);
		}
		// draws player
		drawCircle(Color.BLUE, pLoc, pRadius);
	}

	void drawCircle(Color color, float[] circLoc, float radius) {
		g.setColor(color);
		g.drawOval((int) (circLoc[0] - radius), (int) (circLoc[1] - radius),
				(int) (radius * 2), (int) (radius * 2));
	}

	void fillCircle(Color color, float[] circLoc, float radius) {
		g.setColor(color);
		g.fillOval((int) (circLoc[0] - radius), (int) (circLoc[1] - radius),
				(int) (radius * 2), (int) (radius * 2));
	}

	void drawLine(Color color, float[] p1, float[] p2) {
		g.setColor(color);
		g.drawLine((int) (p1[0] + .5f), (int) (p1[1] + .5f),
				(int) (p2[0] + .5f), (int) (p2[1] + .5f));
	}

	/**
	 * And above here
	 */

	public long timer() {
		return System.currentTimeMillis() - startTime;

	}

	public void drwGm() {
		Graphics g2 = this.getGraphics();
		g2.drawImage(image, 0, 0, null);
		g2.dispose();
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	boolean drawBackground = false;

	@Override
	public void keyPressed(KeyEvent ke) {
		if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
			drawBackground = true;
		}
		if (ke.getKeyCode() == KeyEvent.VK_SPACE) {
			wait = false;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	boolean makePath = false;

	@Override
	public void mousePressed(MouseEvent me) {
		mC = true;
		clickInfo[0] = me.getX();
		clickInfo[1] = me.getY();
		clickInfo[2] = me.getButton();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent me) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

}

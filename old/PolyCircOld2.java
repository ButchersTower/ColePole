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

public class PolyCircOld2 extends JPanel implements Runnable, MouseListener,
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

	float[] pLoc = { 120, 150 };
	float pRadius = 20;
	float playSpeed = 12;

	// 0 = x, 1 = y, 2 = radius
	float[][] trees = { { 200, 100, 40 }, { 250, 210, 30 }, { 220, 310, 30 } };

	int[] tarLoc = new int[2];

	float[] path = new float[0];

	public PolyCircOld2() {
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
				makePathsFirstCheck(tarLoc[0], tarLoc[1]);
				sortDirections();
				makePath = false;
			}
			if (moving) {
				System.out.println("moving");
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

	void drawScene() {
		// draws background
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, width, height);
		// draws trees
		for (int t = 0; t < trees.length; t++) {
			drawCircle(Color.GREEN, trees[t], trees[t][2]);
		}
		// draws player
		drawCircle(Color.BLUE, pLoc, pRadius);
	}

	void clickHande() {
		if (mC) {
			if (clickInfo[2] == 3) {
				tarLoc[0] = clickInfo[0];
				tarLoc[1] = clickInfo[1];
				makePath = true;
			}
			// scalarOfVectOnCirc(new float[] { 5, 1 }, 1, new float[] { 3, 6 },
			// 3,
			// new float[] { 1, 9 });
			mC = false;
		}
	}

	void makePathsFirstCheck(int tarX, int tarY) {
		allPaths = new float[1][0];
		handlePart(tarX, tarY, 0);
		if (allPaths.length == 1) {
			path = allPaths[0];
		} else {

		}
	}

	void handlePart(int tarX, int tarY, int focus) {
		float[] delta = { tarX - pLoc[0], tarY - pLoc[1] };
		// Do an innitial check for each tree to see if it is in range. If so
		// then project and reject.
		float deltaa = Vect2d.norm(delta);
		// if treeDist - treeRadius is less than deltaa then project and reject
		// to see if intersect.
		// [0, 1] = scalars
		// [2] = tree number.
		ArrayList<float[]> scalars = new ArrayList<float[]>();
		for (int t = 0; t < trees.length; t++) {
			float treeDist = Vect2d.norm(new float[] { trees[t][0] - pLoc[0],
					trees[t][1] - pLoc[1] });
			// This is the unnecessary check.
			if (treeDist - trees[t][2] < deltaa) {
				// make tree relative player and see if it intersects with delta
				if (distPointToVect(Vect2d.vectSub(new float[] { trees[t][0],
						trees[t][1] }, pLoc), delta) < trees[t][2] + pRadius) {
					// Tree(s) which intersect.
					// Now find at what scalar of delta they intersect.
					float[] theseScalars = scalarOfVectOnCirc(pLoc, pRadius,
							new float[] { trees[t][0], trees[t][1] },
							trees[t][2], delta);
					Vect2d.sayVect("theseScalars", theseScalars);
					scalars.add(new float[] { theseScalars[0], theseScalars[1],
							t });
				}
			}
		}
		// Search thought scalars and find the lowest one.
		int lowestTree = -1;
		if (scalars.size() == 0) {
			// no intersects go to target
			// path = new float[] { 0, delta[0] / deltaa, delta[1] / deltaa,
			// deltaa };
			moving = true;
			allPaths[focus] = JaMa.appendArFloatAr(allPaths[focus],
					new float[] { 0, delta[0] / deltaa, delta[1] / deltaa,
							deltaa });
		} else {
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
			System.out.println("lowestTree: " + lowestTree);
			// get two tangent points and split the possible path and add
			// add/sub to each.
			float[][] reurnTempList = pointToCirc(pLoc, new float[] { tarX,
					tarY }, lowestTree);
			// Now do check from exit point to tar, if no collision then go to.
			// If collision then do this check again.

			// I want to double what is currently there for the focus tree and
			// append one part of the return to on copy. and the other part of
			// the return to the other copy.
			System.out.println("alP.l: " + allPaths.length);
			System.out.println("alP[0].l: " + allPaths[0].length);
			// makes a copy of focus path
			allPaths = JaMa.appendFloatArAr(allPaths, allPaths[focus]);
			// stick add to focus path
			allPaths[focus] = JaMa.appendArFloatAr(allPaths[focus],
					reurnTempList[0]);
			// stick sub to new focus copy
			allPaths[allPaths.length - 1] = JaMa.appendArFloatAr(
					allPaths[allPaths.length - 1], reurnTempList[1]);
			moving = true;
			System.out.println("getHere");
		}
	}

	void findShortestPath() {
		// search all paths and find the shorted one.
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

	float[] scalarOfVectOnCirc(float[] play, float playR, float[] circ,
			float circR, float[] vect) {
		/**
		 * Need to handle if vectX is zero.
		 */

		// get point slope formula of the vect.
		// relative 0? for now.
		// y = (vectY / vectX) ( x - playX) + playY
		// (x - tx)^2 + (y - ty)^2 = (pR + tR)^2
		// (x - tx)^2 + (y - ty)^2 - (pR + tR)^2 = 0
		// simplify to a quadratic then use formula.
		// (x - tx)^2 = x^2 - 2xtx + tx^2
		// x^2 - 2x*tx + tx^2 + ((vectY / vectX) (x - playX) + playY)^2 -
		// 2*((vectY / vectX) (x - circX) + vectY)*tx + tx^2 - (pR + tR)^2 = 0
		// float xinter = vect[0] * -play[1] / vect[1] + play[0];
		float yinter = vect[1] * -play[0] / vect[0] + play[1];
		System.out.println("xinter: " + yinter);
		float m = vect[1] / vect[0];
		float a = m * m + 1;
		// float b = 2 * (m * xinter + m * circ[1] - circ[1]);
		// float b = -2 * circ[0] + 2 * m * xinter - 2 * m * circ[1];
		float b = 2 * m * yinter - 2 * circ[0] - 2 * m * circ[1];
		// float c = 2 * circ[1] * circ[1] - 2 * yinter * circ[1] + yinter
		// * yinter;
		float c = circ[0] * circ[0] + yinter * yinter + circ[1] * circ[1] - 2
				* yinter * circ[1] - (playR + circR) * (playR + circR);
		System.out.println("(a, b, c) : (" + a + ", " + b + ", " + c + ")");
		float[] quad = quadEq(a, b, c);
		System.out.println("quad[0]: " + quad[0]);
		System.out.println("quad[1]: " + quad[1]);
		// subtract playLoc from quad. or dont.
		// use x to get vect y. or use x to get scalar of vect.
		float vectX1 = quad[0] - play[0];
		float xScale1 = vectX1 / vect[0];
		float vectY1 = vect[1] * xScale1;
		Vect2d.sayVect("vect", vect);
		System.out.println("vectY: " + vectY1);

		float vectX2 = quad[1] - play[0];
		float xScale2 = vectX2 / vect[0];
		float vectY2 = vect[1] * xScale2;
		Vect2d.sayVect("vect", vect);
		System.out.println("vectY: " + vectY2);

		return new float[] { xScale1, xScale2 };
	}

	float[][] allPaths = new float[0][];

	int lowest = 0;

	void sortDirections() {
		// get the shortest path
		float[] sums = new float[allPaths.length];
		for (int d = 0; d < allPaths.length; d++) {
			for (int i = 0; i < allPaths[d].length / 4; i++) {
				if (allPaths[d][i * 4] == 0) {
					sums[d] += allPaths[d][i * 4 + 3];
				} else {
					sums[d] += Math.abs(Vect2d.theaSub(allPaths[d][i * 4 + 1],
							allPaths[d][i * 4 + 2]) * allPaths[d][i * 4 + 3]);
				}
			}
		}
		// find the lowest sum and follow that.
		lowest = 0;
		// System.out.println("sums[" + 0 + "]: " + sums[0]);
		for (int s = 1; s < sums.length; s++) {
			// System.out.println("sums[" + s + "]: " + sums[s]);
			if (sums[s] < sums[lowest]) {
				lowest = s;
			}
		}
		path = allPaths[lowest];
		// System.out.println("lowest: " + lowest);
	}

	float playSpeedLeft = 0;

	void followPath() {
		while (path.length > 0 && playSpeedLeft > 0) {
			// System.out.println("while");
			sortFollowPath();
		}
		if (path.length == 0) {
			moving = false;
		}
	}

	void sortFollowPath() {
		if (path[0] == 0) {
			System.out.println("path 0");
			// System.out.println("line");
			// linear so go straight
			if (path[3] > playSpeedLeft) {
				System.out.println("playSpeedLeft : " + playSpeedLeft);
				System.out.println("path[3]: " + path[3]);
				// Vect2d.sayVect("path", path);
				pLoc[0] += path[1] * playSpeedLeft;
				pLoc[1] += path[2] * playSpeedLeft;
				// System.out.println("path[1]: " + path[1]);
				// System.out.println("path[2]: " + path[2]);
				// System.out.println("xAdd: " + path[1] * playSpeedLeft);
				// System.out.println("yAdd: " + path[2] * playSpeedLeft);
				path[3] -= playSpeedLeft;
				playSpeedLeft = 0;
			} else {
				pLoc[0] += path[1] * path[3];
				pLoc[1] += path[2] * path[3];
				// System.out.println("tarX: " + tarX);
				// System.out.println("tarY: " + tarY);
				// System.out.println("pX: " + pX);
				// System.out.println("pY: " + pY);
				playSpeedLeft -= path[3];
				// delete the four. and move on.
				System.out.println("remove4");
				path = JaMa.removeFirstFloatAr(path, 4);
			}
		} else if (path[0] == 1) {
			System.out.println("path 1");
			// around edge
			float edgeLength = Math.abs(Vect2d.theaSub(path[1], path[2])
					* path[3]);
			System.out.println("edgeLength: " + edgeLength);
			if (edgeLength > playSpeedLeft) {
				// figure out if plus thea is closer or minus thea, then move
				// accordingly inorder to fuffil moveSpeedLeft.
				float possableThea = playSpeedLeft / path[3];
				System.out.println("possableThea: " + possableThea);
				float newThea;
				if (lowest == 1) {
					newThea = Vect2d.theaAdd(path[1], possableThea);
					// add thea
				} else {
					newThea = Vect2d.theaSub(path[1], possableThea);
					// sub thea
				}
				path[1] = newThea;
				float[] newLoc = Vect2d.theaToPoint(newThea, path[3]);
				// g.setColor(Color.MAGENTA);
				// g.drawOval((int) (treeInfo[0] + newLoc[0]) - 3,
				// (int) (treeInfo[1] + newLoc[1]) - 3, 6, 6);
				pLoc[0] = trees[(int) path[4]][0] + newLoc[0];
				pLoc[1] = trees[(int) path[4]][1] + newLoc[1];
				// System.out.println("pX: " + pX + ",   pY: " + pY);
				// pathing = false;
				playSpeedLeft = 0;
			} else {
				float[] newLoc = Vect2d.theaToPoint(path[2], path[3]);
				pLoc[0] = trees[(int) path[4]][0] + newLoc[0];
				pLoc[1] = trees[(int) path[4]][1] + newLoc[1];
				System.out.println("remove5");
				path = JaMa.removeFirstFloatAr(path, 5);
				playSpeedLeft -= edgeLength;
			}
		}
	}

	// This method just returns to be put into allPaths
	float[][] makePathOne(float[] tempLoc, float[] curTarLoc, float[] absTarLoc) {
		// Checks to see if a straight path from tempLoc to tarLoc intersects
		// with any trees.
		int treeIndex = segmentInterAnyTree(new float[][] { tempLoc, curTarLoc });
		// If there are no intersections then return a straight line to tarLoc.
		if (treeIndex < 0) {
			float[] delta = Vect2d.vectSub(curTarLoc, tempLoc);
			float deltaa = Vect2d.norm(delta);
			delta = Vect2d.vectDivScalar(deltaa, delta);
			return new float[][] { { 0, delta[0], delta[1], deltaa } };
		} else {
			// If there is an intersection treeIndex is the index of the first
			// tree intersected with.
			// Find the tangent points of that tree and check if there are any
			// intersection with the two lines going straight to those points.
			// If there are no intersections find the tan points of the tree
			// with tarLoc.
			// Check those two lines for intersections.
			// If there are none then figure out the part around the tree.
			// And then return the (straight) (curved) (straight) sections.

			float[] tanPs = getTangentPointsTrim(tempLoc, pRadius, new float[] {
					trees[treeIndex][0], trees[treeIndex][1] },
					trees[treeIndex][2]);
			// check both paths.
			// if it intersects then forget about the old circ and start over on
			// the new one.
			// these should be the deltas since tanPs should be rel play.
			float[] addP = Vect2d.vectMultScalar(tanPs[6], new float[] {
					tanPs[0], tanPs[1] });
			float[] subP = Vect2d.vectMultScalar(tanPs[6], new float[] {
					tanPs[3], tanPs[4] });

			int addTreeIndex = segmentInterAnyTree(new float[][] { tempLoc,
					Vect2d.vectAdd(tempLoc, addP) });
			if (addTreeIndex < 0) {
				// do circ part
			} else {
				//
			}

			int subTreeIndex = segmentInterAnyTree(new float[][] { tempLoc,
					Vect2d.vectAdd(tempLoc, subP) });
			if (subTreeIndex < 0) {

			}

			// float[][] reurnTempList = doTreeTrim(pLoc, new float[] {
			// tarLoc[0], tarLoc[1] }, treeIndex);

			// straight line to tan point.
			// angle around circ.
			// straight line to tar.

			// return reurnTempList;
		}
		// Shouldn't be here.
		return new float[][] { { 1 } };
	}

	// Figure out two lines tangent of circ.
	// check them for intersect. If so forget about the old circ and start over
	// from this one.
	float[][] doTreeTrim(float[] playLoc, float[] tarLoc, int treeIndex) {
		float[] tangents = getTangentPoints(playLoc, pRadius,
				trees[(int) treeIndex], trees[(int) treeIndex][2]);

		/**
		 * Straight line to tan point on circ
		 */
		float[][] tempList = new float[0][0];
		tempList = JaMa.appendFloatArAr(tempList, new float[] { 0, tangents[0],
				tangents[1], tangents[6] });
		tempList = JaMa.appendFloatArAr(tempList, new float[] { 0, tangents[3],
				tangents[4], tangents[6] });

		/**
		 * start of circle part
		 */
		tempList[0] = JaMa.appendArFloatAr(tempList[0], new float[] { 1,
				tangents[2] });
		tempList[1] = JaMa.appendArFloatAr(tempList[1], new float[] { 1,
				tangents[5] });

		tangents = getTangentPoints(tarLoc, pRadius, new float[] {
				trees[treeIndex][0], trees[treeIndex][1] }, trees[treeIndex][2]);
		// System.out.println("tangents[2]: " + tangents[2]);
		// System.out.println("tangents[5]: " + tangents[5]);
		// tangents from tar
		// plusThea from player should get subThea from tar.
		/**
		 * End of circle part
		 */
		tempList[0] = JaMa.appendArFloatAr(tempList[0], new float[] {
				tangents[5], pRadius + trees[treeIndex][2], treeIndex });
		tempList[1] = JaMa.appendArFloatAr(tempList[1], new float[] {
				tangents[2], pRadius + trees[treeIndex][2], treeIndex });

		/**
		 * Straight line ending up back to target.
		 */
		tempList[0] = JaMa.appendArFloatAr(tempList[0], new float[] { 0,
				-tangents[3], -tangents[4], tangents[6] });
		tempList[1] = JaMa.appendArFloatAr(tempList[1], new float[] { 0,
				-tangents[0], -tangents[1], tangents[6] });
		return tempList;
	}

	float[] getTangentPointsTrim(float[] playLoc, float pRad, float[] treeLoc,
			float tRad) {
		// Plug in play circle and tree circle, return the two lines from
		// playLoc to the points tangent tree.
		// I BELIEIVE ALL POINTS ARE RELATIVE PLAYER
		// [0 + 1] is (x, y) of add thea NOMALIZED
		// [2] is the tan point's thea on tree of add.
		// [3 + 4] is (x, y) of sub thea NOMALIZED
		// [5] is the tan point's thea on tree of sub.
		// [6] is the length from play to each point.
		float[] delta = Vect2d.vectSub(treeLoc, playLoc);
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
		float[] relAddPoint = Vect2d.vectSub(Vect2d.vectAdd(playLoc, addPoint),
				treeLoc);
		float[] relSubPoint = Vect2d.vectSub(Vect2d.vectAdd(playLoc, subPoint),
				treeLoc);
		float relAddThea = Vect2d.pointToThea(relAddPoint);
		float relSubThea = Vect2d.pointToThea(relSubPoint);
		addPoint = Vect2d.normalize(addPoint);
		subPoint = Vect2d.normalize(subPoint);
		return new float[] { addPoint[0], addPoint[1], relAddThea, subPoint[0],
				subPoint[1], relSubThea, adj };
	}

	/**
	 * Trimmed Methods
	 */

	// Does this segment intersect with any trees.
	// If NO return -1.
	// If SO return the closest tree's index.
	int segmentInterAnyTree(float[][] seg) {

		float[] vect = Vect2d.vectSub(seg[1], seg[0]);
		float sega = Vect2d.norm(vect);

		ArrayList<float[]> scalars = new ArrayList<float[]>();
		for (int t = 0; t < trees.length; t++) {
			float treeDist = Vect2d.norm(new float[] { trees[t][0] - pLoc[0],
					trees[t][1] - pLoc[1] });
			// This is the unnecessary check.
			if (treeDist - trees[t][2] < sega) {
				// make tree relative player and see if it intersects with delta
				if (distPointToVect(Vect2d.vectSub(new float[] { trees[t][0],
						trees[t][1] }, pLoc), vect) < trees[t][2] + pRadius) {
					// Tree(s) which intersect.
					// Now find at what scalar of delta they intersect.
					float[] theseScalars = scalarOfVectOnCirc(pLoc, pRadius,
							new float[] { trees[t][0], trees[t][1] },
							trees[t][2], vect);
					Vect2d.sayVect("theseScalars", theseScalars);
					scalars.add(new float[] { theseScalars[0], theseScalars[1],
							t });
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

	/**
	 * Vector Methods
	 */

	float[][] pointToCirc(float[] playLoc, float[] tarLoc, int treeIndex) {
		float[] tangents = getTangentPoints(playLoc, pRadius,
				trees[(int) treeIndex], trees[(int) treeIndex][2]);

		/**
		 * Straight line to tan point on circ
		 */
		float[][] tempList = new float[0][0];
		tempList = JaMa.appendFloatArAr(tempList, new float[] { 0, tangents[0],
				tangents[1], tangents[6] });
		tempList = JaMa.appendFloatArAr(tempList, new float[] { 0, tangents[3],
				tangents[4], tangents[6] });

		/**
		 * start of circle part
		 */
		tempList[0] = JaMa.appendArFloatAr(tempList[0], new float[] { 1,
				tangents[2] });
		tempList[1] = JaMa.appendArFloatAr(tempList[1], new float[] { 1,
				tangents[5] });

		tangents = getTangentPoints(tarLoc, pRadius, new float[] {
				trees[treeIndex][0], trees[treeIndex][1] }, trees[treeIndex][2]);
		// System.out.println("tangents[2]: " + tangents[2]);
		// System.out.println("tangents[5]: " + tangents[5]);
		// tangents from tar
		// plusThea from player should get subThea from tar.
		/**
		 * End of circle part
		 */
		tempList[0] = JaMa.appendArFloatAr(tempList[0], new float[] {
				tangents[5], pRadius + trees[treeIndex][2], treeIndex });
		tempList[1] = JaMa.appendArFloatAr(tempList[1], new float[] {
				tangents[2], pRadius + trees[treeIndex][2], treeIndex });

		/**
		 * Straight line ending up back to target.
		 */
		tempList[0] = JaMa.appendArFloatAr(tempList[0], new float[] { 0,
				-tangents[3], -tangents[4], tangents[6] });
		tempList[1] = JaMa.appendArFloatAr(tempList[1], new float[] { 0,
				-tangents[0], -tangents[1], tangents[6] });
		return tempList;
	}

	void getOuterAdj(float[] circ, float circRad, float[] point, float pointR) {
		drawCircle(Color.BLUE, point, pointR);
		drawCircle(Color.ORANGE, circ, circRad);
		float[] tanPs = getTangentPoints(point, 0, circ, circRad - pointR);
		// scale vect tree -> tanP to playRadus.
		// add it to tanP. add it to playLoc.
		// Make a vect of thoes two points.
		float[] partAdd = Vect2d.vectAdd(
				point,
				Vect2d.vectMultScalar(tanPs[6], new float[] { tanPs[0],
						tanPs[1] }));
		float[] partSub = Vect2d.vectAdd(
				point,
				Vect2d.vectMultScalar(tanPs[6], new float[] { tanPs[3],
						tanPs[4] }));

		float[] soloPart = Vect2d.vectMultScalar(tanPs[6], new float[] {
				tanPs[3], tanPs[4] });

		// drawCircle(Color.GREEN, partAdd, 10f);
		// drawCircle(Color.GREEN, partSub, 10f);
		float[] part1 = Vect2d.vectSub(partAdd, circ);
		// scale part1 to pointR
		float[] scaledPart1 = Vect2d.scaleVectTo(part1, pointR);
		// drawLine(circ, Vect2d.vectAdd(circ, part1));
		// drawLine(circ, Vect2d.vectAdd(partAdd, scaledPart1));
		float[] partOffCirc = Vect2d.vectAdd(partAdd, scaledPart1);
		float[] pointPlusScaledPart1 = Vect2d.vectAdd(point, scaledPart1);
		float[] tangentLineAdd = Vect2d.vectSub(partOffCirc,
				pointPlusScaledPart1);
		drawLine(pointPlusScaledPart1,
				Vect2d.vectAdd(pointPlusScaledPart1, tangentLineAdd));
		/**
		 * Subtraction part
		 */
		float[] part1Sub = Vect2d.vectSub(partSub, circ);
		// scale part1 to pointR
		float[] scaledPart1Sub = Vect2d.scaleVectTo(part1Sub, pointR);
		// drawLine(circ, Vect2d.vectAdd(circ, part1));
		// drawLine(circ, Vect2d.vectAdd(partSub, scaledPart1Sub));
		float[] partOffCircSub = Vect2d.vectAdd(partSub, scaledPart1Sub);
		float[] pointPlusScaledPart1Sub = Vect2d.vectAdd(point, scaledPart1Sub);
		float[] tangentLineSub = Vect2d.vectSub(partOffCircSub,
				pointPlusScaledPart1Sub);
		drawLine(pointPlusScaledPart1Sub,
				Vect2d.vectAdd(pointPlusScaledPart1Sub, tangentLineSub));
	}

	void outerEdgeOne(float[] soloPart, float[] circ, float[] point,
			float pointR) {
		float[] partSub = Vect2d.vectAdd(soloPart, point);

		float[] part1Sub = Vect2d.vectSub(partSub, circ);
		// scale part1 to pointR
		float[] scaledPart1Sub = Vect2d.scaleVectTo(part1Sub, pointR);
		// drawLine(circ, Vect2d.vectAdd(circ, part1));
		// drawLine(circ, Vect2d.vectAdd(partSub, scaledPart1Sub));
		float[] partOffCircSub = Vect2d.vectAdd(partSub, scaledPart1Sub);
		float[] pointPlusScaledPart1Sub = Vect2d.vectAdd(point, scaledPart1Sub);
		float[] tangentLineSub = Vect2d.vectSub(partOffCircSub,
				pointPlusScaledPart1Sub);
		drawLine(pointPlusScaledPart1Sub,
				Vect2d.vectAdd(pointPlusScaledPart1Sub, tangentLineSub));
	}

	void getOuterAdjTrim(float[] circ, float circRad, float[] point,
			float pointR) {
		float[] tanPs = getTangentPoints(point, 0, circ, circRad - pointR);
		// scale vect tree -> tanP to playRadus.
		// add it to tanP. add it to playLoc.
		// Make a vect of thoes two points.
		float[] partAdd = Vect2d.vectMultScalar(tanPs[6], new float[] {
				tanPs[0], tanPs[1] });
		float[] partSub = Vect2d.vectMultScalar(tanPs[6], new float[] {
				tanPs[3], tanPs[4] });
		outerEdgeOne(partAdd, circ, point, pointR);
		outerEdgeOne(partSub, circ, point, pointR);

	}

	float[] getTangentPoints(float[] play, float pRad, float[] tree, float tRad) {
		// Plug in play circle and tree circle, return the two lines from
		// playLoc to the points tangent tree.
		// I BELIEIVE ALL POINTS ARE RELATIVE PLAYER
		// [0 + 1] is (x, y) of add thea NOMALIZED
		// [2] is the tan point's thea on tree of add.
		// [3 + 4] is (x, y) of sub thea NOMALIZED
		// [5] is the tan point's thea on tree of sub.
		// [6] is the length from play to each point.
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
		float[] relAddPoint = Vect2d.vectSub(Vect2d.vectAdd(play, addPoint),
				tree);
		float[] relSubPoint = Vect2d.vectSub(Vect2d.vectAdd(play, subPoint),
				tree);
		float relAddThea = Vect2d.pointToThea(relAddPoint);
		float relSubThea = Vect2d.pointToThea(relSubPoint);
		addPoint = Vect2d.normalize(addPoint);
		subPoint = Vect2d.normalize(subPoint);
		return new float[] { addPoint[0], addPoint[1], relAddThea, subPoint[0],
				subPoint[1], relSubThea, adj };
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

	/**
	 * Drawing
	 */

	void drawCircle(Color color, float[] circLoc, float radius) {
		g.setColor(color);
		g.drawOval((int) (circLoc[0] - radius), (int) (circLoc[1] - radius),
				(int) (radius * 2), (int) (radius * 2));
	}

	void drawLine(float[] p1, float[] p2) {
		g.setColor(Color.WHITE);
		g.drawLine((int) p1[0], (int) p1[1], (int) p2[0], (int) p2[1]);
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

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub

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

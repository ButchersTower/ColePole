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

	float[] pLoc = { 120, 150 };
	float pRadius = 20;
	float playSpeed = 12;

	// 0 = x, 1 = y, 2 = radius
	float[][] trees = { { 200, 100, 40 }, { 250, 210, 30 }, { 220, 310, 30 } };

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
			if (clickInfo[2] == 3) {
				tarLoc[0] = clickInfo[0];
				tarLoc[1] = clickInfo[1];
				makePath(pLoc, new float[] { (float) tarLoc[0],
						(float) tarLoc[1] });
				makePath = true;
			}
			// scalarOfVectOnCirc(new float[] { 5, 1 }, 1, new float[] { 3, 6 },
			// 3,
			// new float[] { 1, 9 });
			mC = false;
		}
	}

	void makePath(float[] tempLoc, float[] finTarLoc) {
		float[][] paths = new float[0][];
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
			path = loopOne(tempLoc, oldTreeIndex, finTarLoc, paths);
			moving = true;
			return;
		}
	}

	float[] loopOne(float[] tempLoc, int oldTreeIndex, float[] finTarLoc,
			float[][] paths) {
		// gets tangent circ from point.
		float[] tangentPoints = getTangentPoints(tempLoc, pRadius,
				trees[oldTreeIndex], trees[oldTreeIndex][2]);

		float[] tempsTan = Vect2d.vectAdd(tempLoc, new float[] {
				tangentPoints[0], tangentPoints[1] });
		int newTreeIndexAdd = segmentInterAnyTreeIgnore(new float[][] {
				tempLoc, tempsTan }, oldTreeIndex);

		if (newTreeIndexAdd < 0) {
			// there are no trees in the way to the tangent point of
			// oldTreeIndex
			// loopTwo(tempLoc, oldTreeIndex, finTarLoc);
			float deltaa = Vect2d.norm(tangentPoints);
			float[] delta = Vect2d.vectDivScalar(deltaa, tangentPoints);
			// give thea of tanP rel tree
			// point to thea (tree - (tempL + tanP))
			float thea = Vect2d
					.pointToThea((Vect2d.vectSub(trees[oldTreeIndex],
							Vect2d.vectAdd(tempLoc, tangentPoints))));
			float[] nextPart = loop1noInter(tempLoc, oldTreeIndex, finTarLoc,
					thea);
			return JaMa.appendArFloatAr(new float[] { 0, delta[0], delta[1],
					deltaa }, nextPart);
		} else {
			loopOne(tempLoc, newTreeIndexAdd, finTarLoc, paths);
			return new float[0];
		}
	}

	float[] loop1noInter(float[] tempLoc, int oldTreeIndex, float[] curTarLoc,
			float entraceThea) {
		// straight line to tanPoint is good, need to figure angle of that point
		// relative tree.
		// CurTarLoc - OldTreeIndex (tangent points)
		// Invert that path before doing intersection detection.
		float[] tangentPoints = getTangentPoints(curTarLoc, pRadius,
				trees[oldTreeIndex], trees[oldTreeIndex][2]);

		float[] tarsTanAdd = Vect2d.vectAdd(curTarLoc, new float[] {
				tangentPoints[0], tangentPoints[1] });
		int newTreeIndexAdd = segmentInterAnyTreeIgnore(new float[][] {
				tarsTanAdd, curTarLoc }, oldTreeIndex);
		// tangentPoints is currently relative
		if (newTreeIndexAdd < 0) {
			System.out.println("oldTreeIndex: " + oldTreeIndex);
			// there are no trees in the way to the tangent point of
			// oldTreeIndex
			float exitThea = Vect2d
					.pointToThea((Vect2d.vectSub(trees[oldTreeIndex],
							Vect2d.vectAdd(tempLoc, tangentPoints))));
			float[] part = { 1, entraceThea, exitThea,
					trees[oldTreeIndex][2] + pRadius, oldTreeIndex };

			float deltaa = Vect2d.norm(tangentPoints);
			float[] delta = Vect2d.vectDivScalar(-deltaa, tangentPoints);

			part = JaMa.appendArFloatAr(part, new float[] { 0, delta[0],
					delta[1], deltaa });
			return part;
		} else {
			// leaving the initial circle it collides with another circle.
			// 2 paths, inner line tan circs. outer line tan circs.
			// float[] innerLineTan
			// tang2circ(trees[oldTreeIndex], trees[oldTreeIndex][2] + pRadius,
			// trees[newTreeIndexAdd], trees[newTreeIndexAdd][2] + pRadius);
			loopTwo(tempLoc, oldTreeIndex, newTreeIndexAdd, curTarLoc, true);
		}
		return new float[0];
	}

	void loopTwo(float[] tempLoc, int oldTreeIndex, int newTreeIndex,
			float[] curTarLoc, boolean add) {

		// leaving the initial circle it collides with another circle.
		// 2 paths, inner line tan circs. outer line tan circs.
		// float[] innerLineTan
		tang2circ(trees[oldTreeIndex], trees[oldTreeIndex][2] + pRadius,
				trees[newTreeIndex], trees[newTreeIndex][2] + pRadius, add);
	}

	// gets tangent points.
	// Don't normalize before returning
	// Don't return the magnitude.

	float[] getTangentPoints(float[] play, float pRad, float[] tree, float tRad) {
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
		drawCircle(Color.ORANGE, Vect2d.vectAdd(play, addPoint), 4);
		drawCircle(Color.MAGENTA, Vect2d.vectAdd(play, subPoint), 4);
		return new float[] { addPoint[0], addPoint[1], subPoint[0],
				subPoint[1], -1 };
	}

	/**
	 * Path follow
	 */

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
				// if (lowest == 1) {
				// newThea = Vect2d.theaAdd(path[1], possableThea);
				// // add thea
				// } else {
				newThea = Vect2d.theaSub(path[1], possableThea);
				// sub thea
				// }
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
						trees[t][0] - pLoc[0], trees[t][1] - pLoc[1] });
				// This is the unnecessary check.
				if (treeDist - trees[t][2] < sega) {
					// make tree relative player and see if it intersects with
					// delta
					if (distPointToVect(
							Vect2d.vectSub(new float[] { trees[t][0],
									trees[t][1] }, pLoc), vect) < trees[t][2]
							+ pRadius) {
						// Tree(s) which intersect.
						// Now find at what scalar of delta they intersect.
						float[] theseScalars = scalarOfVectOnCirc(pLoc,
								pRadius,
								new float[] { trees[t][0], trees[t][1] },
								trees[t][2], vect);
						Vect2d.sayVect("theseScalars", theseScalars);
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
	void tang2circ(float[] c1Loc, float c1r, float[] c2Loc, float c2r,
			boolean add) {
		// make everything relative c1Loc
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
		g.fillOval((int) (c1Loc[0] + midPoint[0] - 2), (int) (c1Loc[1]
				+ midPoint[1] - 2), 4, 4);

		// get four tangent points.
		float[] al1 = getTangentPoints(midPoint, 0, new float[] { 0, 0 }, c1r);
		float[] al2 = getTangentPoints(midPoint, 0, c1toc2, c2r);
		// Vect2d.sayVect("midPoint", midPoint);
		// System.out.println("al1 (" + al1[0] + ", " + al1[1] + ", " + al1[2]
		// + ", " + al1[3] + ", " + al1[4] + ", " + al1[5] + ", " + al1[6]
		// + ")");
		if (!add) {
			g.setColor(new Color(255, 255, 255));
			g.drawLine((int) (c1Loc[0] + midPoint[0] + .5f), (int) (c1Loc[1]
					+ midPoint[1] + .5f), (int) (c1Loc[0] + midPoint[0]
					+ al1[0] + .5f),
					(int) (c1Loc[1] + midPoint[1] + al1[1] + .5f));
		} else {
			g.setColor(Color.BLUE);
			g.drawLine((int) (c1Loc[0] + midPoint[0] + .5f), (int) (c1Loc[1]
					+ midPoint[1] + .5f), (int) (c1Loc[0] + midPoint[0]
					+ al1[2] + .5f),
					(int) (c1Loc[1] + midPoint[1] + al1[3] + .5f));
		}
		if (!add) {
			g.setColor(Color.RED);
			g.drawLine((int) (c1Loc[0] + midPoint[0] + .5f), (int) (c1Loc[1]
					+ midPoint[1] + .5f), (int) (c1Loc[0] + midPoint[0]
					+ al2[0] + .5f),
					(int) (c1Loc[1] + midPoint[1] + al2[1] + .5f));
		} else {
			g.setColor(Color.GREEN);
			g.drawLine((int) (c1Loc[0] + midPoint[0] + .5f), (int) (c1Loc[1]
					+ midPoint[1] + .5f), (int) (c1Loc[0] + midPoint[0]
					+ al2[2] + .5f),
					(int) (c1Loc[1] + midPoint[1] + al2[3] + .5f));
		}
	}

	/**
	 * Drawing
	 */

	void drawScene() {
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

	boolean drawBackground = false;

	@Override
	public void keyPressed(KeyEvent ke) {
		if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
			drawBackground = true;
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

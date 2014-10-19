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

import javax.swing.JPanel;

public class OneCircPanelOld1 extends JPanel implements Runnable, MouseListener,
		KeyListener {

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

	float pX = 60;
	float pY = 90;
	float pRadius = 20;
	float pSpeed = 12;

	float tarX = 0;
	float tarY = 0;

	// [0] = x
	// [1] = y
	// [2] = radius
	float[] treeInfo = { 120, 150, 12 };

	float[][] direction = new float[0][0];
	int pathPart = 0;

	boolean setPath = false;
	float[] path = new float[0];
	boolean moving = false;
	int lowest = 0;

	public Panel() {
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
		running = true;
		gLoop();
	}

	public void gLoop() {
		while (running) {
			// Do the things you want the gLoop to do below here

			if (setPath) {
				playMoveWhole();
				moving = true;
				setPath = false;
			}
			if (moving) {
				if (path.length == 2) {
					float pa = Vect2d.norm(path);
					if (pa > pSpeed) {
						float[] pVect = Vect2d
								.vectMultScalar(pSpeed / pa, path);
						pX += pVect[0];
						pY += pVect[1];
						path = Vect2d.vectSub(path, pVect);
					}
					pa = Vect2d.norm(path);
					if (pa == 0) {
						moving = false;
					}
				}
				// if (pX == tarX && pY == tarY) {
				// moving = false;
				// }
				draw();
				drwGm();
			}
			// System.out.println("runTime: " + timer());
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

	void playerMoveSeg() {
		// get delta vector. scale to moveSpeed.
		float[] deltaVect = { tarX - pX, tarY - pY };
		float deltaR = Vect2d.norm(deltaVect);
		if (deltaR > pSpeed) {
			float scalarMult = pSpeed / deltaR;
			deltaVect = Vect2d.vectMultScalar(scalarMult, deltaVect);
		}

		if (distPointToVect(Vect2d.vectSub(new float[] { treeInfo[0],
				treeInfo[1] }, new float[] { pX, pY }), deltaVect) < treeInfo[2]
				+ pRadius) {
			moving = false;
			// cant move
		} else {
			pX += deltaVect[0];
			pY += deltaVect[1];
		}
	}

	void playMoveWhole() {
		// Plus thea from player should get sub thea from tar.

		direction = new float[0][];
		// get delta vector. scale to moveSpeed.
		float[] deltaVect = { tarX - pX, tarY - pY };
		float deltaR = Vect2d.norm(deltaVect);
		// if (deltaR > pSpeed) {
		// float scalarMult = pSpeed / deltaR;
		// deltaVect = Vect2d.vectMultScalar(scalarMult, deltaVect);
		// }

		if (distPointToVect(Vect2d.vectSub(new float[] { treeInfo[0],
				treeInfo[1] }, new float[] { pX, pY }), deltaVect) < treeInfo[2]
				+ pRadius) {
			// cant move
			// moving = false;
			float[] tangents = myAngleThing(new float[] { pX, pY }, pRadius,
					new float[] { treeInfo[0], treeInfo[1] }, treeInfo[2]);
			path = new float[0];
			direction = JaMa.appendFloatArAr(direction, new float[] { 0,
					tangents[0], tangents[1], tangents[6] });
			direction = JaMa.appendFloatArAr(direction, new float[] { 0,
					tangents[3], tangents[4], tangents[6] });

			// tangents from play
			direction[0] = JaMa.appendArFloatAr(direction[0], new float[] { 1,
					tangents[2] });

			direction[1] = JaMa.appendArFloatAr(direction[1], new float[] { 1,
					tangents[5] });

			tangents = myAngleThing(new float[] { tarX, tarY }, pRadius,
					new float[] { treeInfo[0], treeInfo[1] }, treeInfo[2]);

			// float[][] toTar = new float[0][];
			// toTar = JaMa.appendFloatArAr(direction, new float[] {
			// tangents[0],
			// tangents[1], tangents[6] });
			// toTar = JaMa.appendFloatArAr(direction, new float[] {
			// tangents[3],
			// tangents[4], tangents[6] });

			// tangents from tar
			// plusThea from player should get subThea from tar.
			direction[0] = JaMa.appendArFloatAr(direction[0], new float[] {
					tangents[5], pRadius + treeInfo[2] });
			direction[1] = JaMa.appendArFloatAr(direction[1], new float[] {
					tangents[2], pRadius + treeInfo[2] });

			direction[0] = JaMa.appendArFloatAr(direction[0], new float[] { 0,
					tarX, tarY, tangents[6] });
			direction[1] = JaMa.appendArFloatAr(direction[1], new float[] { 0,
					tarX, tarY, tangents[6] });
			System.out.println("makePath");
			sortDirections();

		} else {
			path = deltaVect;
			// pX += deltaVect[0];
			// pY += deltaVect[1];
		}
	}

	void sortDirections() {
		// get the shortest path

		float[] sums = new float[direction.length];
		for (int d = 0; d < direction.length; d++) {
			for (int i = 0; i < direction[d].length / 4; i++) {
				if (direction[d][i * 4] == 0) {
					// System.out.println("direction[" + d + "][" + (i * 4 + 3)
					// + "]: " + direction[d][i * 4 + 3]);
					sums[d] += direction[d][i * 4 + 3];
				} else {
					// System.out.println("direction["+d+"][" + (i * 4 + 1) +
					// "]"
					// + direction[d][i * 4 + 1]);
					// System.out.println("direction["+d+"][" + (i * 4 + 2) +
					// "]"
					// + direction[d][i * 4 + 2]);
					// System.out.println("direction["+d+"][" + (i * 4 + 3) +
					// "]"
					// + direction[d][i * 4 + 3]);
					// System.out
					// .println("Get dist of edge: "
					// + (JaMa.theaSub(direction[d][i * 4 + 1],
					// direction[d][i * 4 + 2]) * direction[d][i * 4 + 3]));
					sums[d] += Math.abs(JaMa.theaSub(direction[d][i * 4 + 1],
							direction[d][i * 4 + 2]) * direction[d][i * 4 + 3]);
				}
			}
		}
		// find the lowest sum and follow that.
		lowest = 0;
		System.out.println("sums[" + 0 + "]: " + sums[0]);
		for (int s = 1; s < sums.length; s++) {
			System.out.println("sums[" + s + "]: " + sums[s]);
			if (sums[s] < sums[lowest]) {
				lowest = s;
			}
		}
		System.out.println("lowest: " + lowest);

		// g.setColor(lowest == 0 ? Color.RED : Color.ORANGE);
		// g.drawLine((int) pX, (int) pY, (int) (pX + direction[0][1]),
		// (int) (pY + direction[0][2]));
		// g.setColor(lowest == 1 ? Color.RED : Color.ORANGE);
		// g.drawLine((int) pX, (int) pY, (int) (pX + direction[1][1]),
		// (int) (pY + direction[1][2]));
		// drwGm();

		// now follow the path
		// moving = false;
	}

	void followPath() {
		float[] myPath = direction[lowest];
		for (int r = 0; r < myPath.length / 4; r++) {
			if (myPath[r * 4] == 0) {
				// linear so go straight
			} else if (myPath[r * 4] == 1) {
				// around edge
			}
		}
	}

	float distPointToSeg(float[] point, float[][] seg) {
		// project, is projection scalar is farther than the line then take
		// hypotnuse of closest and edge and point. if the scalar is on the line
		// then reject and that is dist.

		float[] pointRelSeg = Vect2d.vectSub(point, seg[0]);
		float[] segRelSeg = Vect2d.vectSub(seg[1], seg[0]);
		//
		float dist;
		float projScalar = Vect2d.scalarOfProject(pointRelSeg, segRelSeg);
		if (projScalar > 1) {
			// get dist from the end of seg.
			dist = Vect2d.norm(new float[] { seg[1][0] - point[0],
					seg[1][1] - point[1] });
		} else if (projScalar < 0) {
			// get the dist from the start of seg.
			dist = Vect2d.norm(new float[] { seg[0][0] - point[0],
					seg[0][1] - point[1] });
		} else {
			// dist is point rej proj
			dist = Vect2d.norm(Vect2d.vectSub(point,
					Vect2d.vectMultScalar(projScalar, segRelSeg)));
		}
		return dist;
	}

	float distPointToVect(float[] point, float[] vect) {
		// project, is projection scalar is farther than the line then take
		// hypotnuse of closest and edge and point. if the scalar is on the line
		// then reject and that is dist.

		float dist;
		float projScalar = Vect2d.scalarOfProject(point, vect);
		System.out.println("projScalar: " + projScalar);
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

	void playGoTo() {
		// player go to targetLoc and.
		// proj a vector of the length of
		// get vecto
	}

	void draw() {
		// g.setColor(Color.BLACK);
		// g.fillRect(0, 0, width, height);
		g.setColor(Color.BLUE);
		g.drawOval((int) (pX - pRadius + .5f), (int) (pY - pRadius + .5f),
				(int) (pRadius * 2 + .5f), (int) (pRadius * 2 + .5f));

		g.setColor(Color.GREEN);
		g.fillOval((int) (treeInfo[0] - treeInfo[2]),
				(int) (treeInfo[1] - treeInfo[2]), (int) treeInfo[2] * 2,
				(int) treeInfo[2] * 2);
	}

	float[] myAngleThing(float[] play, float pRad, float[] tree, float tRad) {
		// if |delta| < pRad + tRad
		// get thea of play and project it out from tree to a dist of pRad+tRad.
		// This is the plusPoint and subPoint.
		float[] delta = Vect2d.vectSub(tree, play);
		float hyp = Vect2d.norm(delta);
		float opp = pRad + tRad;
		float adj = (float) Math.sqrt(hyp * hyp - opp * opp);
		// float treeThea = (float) Math.atan(delta[1] / delta[0]);
		float treeThea = JaMa.pointToThea(delta);
		// float shapeThea = (float) Math.asin(opp / hyp);
		float shapeThea = JaMa.pointToThea(new float[] { adj, opp });
		// how to tell is to subtract or add shape thea.
		// the two possible points are plus shape thea and minus shape thea
		// scaled to adjacent and added to play.
		// return float[]
		// [0 + 1] is (x, y) of plus thea
		// [2 + 3] is (x, y) of minus thea
		// [4] is the length from play to each point.
		float addThea = JaMa.theaAdd(treeThea, shapeThea);
		float subThea = JaMa.theaSub(treeThea, shapeThea);
		// System.out.println("plusThea: " + addThea + "   : subThea: " +
		// subThea);
		float[] plusPoint = JaMa.theaToPoint(addThea, adj);
		float[] subPoint = JaMa.theaToPoint(subThea, adj);
		// make sub thea and plus thea relative to tree.
		// plus point minus tree
		float[] relAddPoint = Vect2d.vectSub(Vect2d.vectAdd(play, plusPoint),
				tree);
		float[] relSubPoint = Vect2d.vectSub(Vect2d.vectAdd(play, subPoint),
				tree);
		// Vect2d.sayVect("relPlusPoint", relAddPoint);
		// Vect2d.sayVect("relSubPoint", relSubPoint);
		g.setColor(Color.GREEN);
		g.drawOval((int) (relAddPoint[0] + tree[0]) - 4,
				(int) (relAddPoint[1] + tree[1]) - 4, 8, 8);
		g.setColor(Color.CYAN);
		g.drawOval((int) (relSubPoint[0] + tree[0]) - 4,
				(int) (relSubPoint[1] + tree[1]) - 4, 8, 8);
		float relPlusThea = JaMa.pointToThea(relAddPoint);
		float relSubThea = JaMa.pointToThea(relSubPoint);
		// System.out.println("relPlusThea: " + relPlusThea +
		// "  :  relSubThea: "
		// + relSubThea);
		// Vect2d.sayVect("plusPoint", plusPoint);
		// Vect2d.sayVect("subPoint", subPoint);
		return new float[] { plusPoint[0], plusPoint[1], relPlusThea,
				subPoint[0], subPoint[1], relSubThea, adj };
	}

	/**
	 * Methods go above here.
	 * 
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
	public void mousePressed(MouseEvent me) {
		if (me.getButton() == MouseEvent.BUTTON3) {
			// moving = true;
			setPath = true;
			tarX = me.getX();
			tarY = me.getY();
		} else {
			// moving = false;
			setPath = false;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent ke) {
		if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, width, height);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}
}

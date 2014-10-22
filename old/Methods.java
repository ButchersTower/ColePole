package ColePole.old;

import ColePole.lib.JaMa;
import ColePole.lib.Vect2d;

public class Methods {
	void playMoveWholePush() {
		// Plus thea from player should get sub thea from tar.

		direction = new float[0][];
		// get delta vector. scale to moveSpeed.
		float[] deltaVect = { tarX - pX, tarY - pY };

		if (distPointToVect(Vect2d.vectSub(new float[] { treeInfo[0],
				treeInfo[1] }, new float[] { pX, pY }), deltaVect) < treeInfo[2]
				+ pRadius) {
			// if the target point was inside of a tree then project it out.
			float[] tarRelTree = new float[] { tarX - treeInfo[0],
					tarY - treeInfo[1] };
			if (Vect2d.norm(tarRelTree) <= pRadius + treeInfo[2]) {
				// add small num to bypass rounding mistakes.
				// float[] pushedTar = Vect2d.theaToPoint(
				// Vect2d.pointToThea(tarRelTree), pRadius + treeInfo[2]
				// + smallNum);
				/**
				 * Instead of pushing it out pick the closest point between play
				 * and tree.
				 */
				float[] pushedTar = Vect2d.scaleVectTo(tarRelTree, pRadius
						+ treeInfo[2] + smallNum);
				tarX = treeInfo[0] + pushedTar[0];
				tarY = treeInfo[1] + pushedTar[1];
				playMoveWhole();
				return;
			}
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
			System.out.println("tangents[2]: " + tangents[2]);
			System.out.println("tangents[5]: " + tangents[5]);
			direction[1] = JaMa.appendArFloatAr(direction[1], new float[] { 1,
					tangents[5] });

			tangents = myAngleThing(new float[] { tarX, tarY }, pRadius,
					new float[] { treeInfo[0], treeInfo[1] }, treeInfo[2]);
			System.out.println("tangents[2]: " + tangents[2]);
			System.out.println("tangents[5]: " + tangents[5]);
			// tangents from tar
			// plusThea from player should get subThea from tar.
			direction[0] = JaMa.appendArFloatAr(direction[0], new float[] {
					tangents[5], pRadius + treeInfo[2] });
			direction[1] = JaMa.appendArFloatAr(direction[1], new float[] {
					tangents[2], pRadius + treeInfo[2] });

			direction[0] = JaMa.appendArFloatAr(direction[0], new float[] { 0,
					-tangents[3], -tangents[4], tangents[6] });
			direction[1] = JaMa.appendArFloatAr(direction[1], new float[] { 0,
					-tangents[0], -tangents[1], tangents[6] });
			System.out.println("direction[0][" + 9 + "]: " + direction[0][9]);
			System.out.println("direction[0][" + 10 + "]: " + direction[0][10]);
			System.out.println("direction[1][" + 9 + "]: " + direction[1][9]);
			System.out.println("direction[1][" + 10 + "]: " + direction[1][10]);

			System.out.println("makePath");
			sortDirections();
			for (int d = 0; d < direction[lowest].length; d++) {
				System.out.println("direction[" + d + "]: "
						+ direction[lowest][d]);
			}
			myPath = direction[lowest];
			pathing = true;
		} else {
			path = deltaVect;
		}
	}

	void followPathSwitchBreak() {
		// uses switch
		// Assumes that
		// if [0] == 0 then line.
		// if [0] == 1 then curve.
		float[] myPath = direction[lowest];
		float playSpeedLeft = pSpeed;
		// if (myPath[0] == 0) {
		System.out.println("(int) myPath[0]: " + ((int) myPath[0]));
		switch ((int) myPath[0]) {
		case 0:
			System.out.println("case 0");
			// System.out.println("line");
			// linear so go straight
			if (myPath[3] > playSpeedLeft) {
				System.out.println("playSpeedLeft : " + playSpeedLeft);
				System.out.println("myPath[3]: " + myPath[3]);
				// Vect2d.sayVect("myPath", myPath);
				pX += myPath[1] * playSpeedLeft;
				pY += myPath[2] * playSpeedLeft;
				myPath[3] -= playSpeedLeft;
			} else {

				pX += myPath[1] * myPath[3];
				pY += myPath[2] * myPath[3];
				playSpeedLeft -= myPath[3];
				// delete the four. and move on.
				System.out.println("myPath.l: " + myPath.length);
				myPath = JaMa.removeFirstFloatAr(myPath, 4);
				System.out.println("myPath.l: " + myPath.length);
			}
			// }
			// if (myPath[0] == 1) {
			break;
		case 1:
			System.out.println("case 1");
			// around edge
			float edgeLength = Vect2d.theaSub(myPath[1], myPath[2]) * myPath[3];
			break;
		}
	}
}

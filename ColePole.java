package ColePole;

import javax.swing.JFrame;

public class ColePole extends JFrame {
	public ColePole() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(new PolyCircSmall());
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setTitle("ColePole");
	}

	public static void main(String[] args) {
		new ColePole();
	}
}

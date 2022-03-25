package main.gui;

import main.nes.Palette;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class GUI extends JFrame {


	Color palette[] = Palette.defaultPalette().colors;

	private Dimension screenSize = new Dimension(256, 240);

	public BufferedImage img;

	private JLabel render;

	public GUI() {
		super("COCK");
		//screenSize = new Dimension(1, 1);

		//Theres 9129012 different ways of doing this but this worky
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/Nes_controller.svg.png")));


		img = new BufferedImage(screenSize.width, screenSize.height, BufferedImage.TYPE_INT_RGB);


		Random r = new Random();
		for(int y = 0; y < screenSize.height; y++) {
			for(int x = 0; x < screenSize.width; x++) {
				int i = r.nextInt();
				img.setRGB(x,y,i);
			}
		}


		render = new JLabel(new ImageIcon(img));


		LineBorder b = new LineBorder(Color.BLACK);

		//render.setBorder(BorderFactory.createStrokeBorder(new BasicStroke(5.0f)));
		//setContentPane(render);
		getContentPane().add(render, BorderLayout.CENTER);


		render.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				super.componentResized(e);
				repaint();
			}
		});


		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(500, 400));
		setSize(screenSize);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public void recock() {
		repaint();
	}

	@Override
	public void repaint() {
		super.repaint();

		Random r = new Random();
		for(int y = 0; y < screenSize.height; y++) {
			for(int x = 0; x < screenSize.width; x++) {
				int i = r.nextInt(palette.length);
				img.setRGB(x,y,palette[i].getRGB());
			}
		}
		Image newImg = img.getScaledInstance(render.getWidth(), render.getHeight(), Image.SCALE_DEFAULT);
		render.setIcon(new ImageIcon(newImg));


	}

	public static void main(String[] args) {

		GUI gui = new GUI();

		Random r = new Random();
		while(true) {
			try {
				Thread.sleep(16 * (r.nextInt(2) + 1) );
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			gui.recock();
		}
	}


}

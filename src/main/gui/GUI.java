package main.gui;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class GUI extends JFrame {
	private final Dimension screenSize = new Dimension(256, 240);

	public BufferedImage img;

	private JLabel render;

	public GUI() {
		super("COCK");

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
				int i = r.nextInt();
				img.setRGB(x,y,i);
			}
		}
		Image newImg = img.getScaledInstance(render.getWidth(), render.getHeight(), Image.SCALE_DEFAULT);
		render.setIcon(new ImageIcon(newImg));


	}

	public static void main(String[] args) {

		GUI gui = new GUI();


		while(true) {
			try {
				Thread.sleep(33);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			gui.recock();
		}
	}


}

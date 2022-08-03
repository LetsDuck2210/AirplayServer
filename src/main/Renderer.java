package main;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JLabel;

public class Renderer extends JLabel {
	private static final long serialVersionUID = 1L;
	
	private Image image;
	public Renderer() {
		
	}
	
	public void render(Image img) {
		this.image = img;
		
		repaint();
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if(image != null) {
			g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
		}
	}
}

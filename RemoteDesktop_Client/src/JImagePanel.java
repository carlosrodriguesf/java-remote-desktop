import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class JImagePanel extends JPanel implements ComponentListener {
	private BufferedImage image;

	private Rectangle imageBounds;
	private Image resizedImage;

	public JImagePanel() {
		this.image = null;
	}

	private void resizeImage() {
		if (this.image == null)
			return;

		float imageProportion = (float) this.image.getHeight() / this.image.getWidth();
		
		float width = this.image.getWidth(), height = this.image.getHeight(), x, y;
		
		if (height > this.getHeight()) {
			height = this.getHeight();
			width = height / imageProportion;
		} 
		
		if (width > this.getWidth()) {
			width = this.getWidth();
			height = width * imageProportion;
		}

		x = this.getWidth() - width;
		if (x > 0)
			x /= 2;

		y = this.getHeight() - height;
		if (y > 0)
			y /= 2;

		this.resizedImage = this.image.getScaledInstance((int) width, (int) height, Image.SCALE_AREA_AVERAGING);
		this.imageBounds = new Rectangle((int) x, (int) y, (int) width, (int) height);
		
		this.updateUI();
	}
	
	public void setImage(BufferedImage image) {
		this.image = image;
		this.resizeImage();
	}
	@Override
	public void componentResized(ComponentEvent e) {
		this.resizeImage();
	}
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (this.resizedImage == null)
			return;

		Graphics2D g2d = (Graphics2D) g.create();
		g2d.drawImage(this.resizedImage, (int) this.imageBounds.getX(), (int) this.imageBounds.getY(),
				(int) this.imageBounds.getWidth(), (int) this.imageBounds.getHeight(), null);
		g2d.dispose();
	}

	/**
	 * Not Used
	 */
	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}
}

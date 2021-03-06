package remotedesktop.server.autoscreenshot;

import java.awt.image.BufferedImage;

/**
 * Listener para receber as screenshots
 * 
 * @author Carlos Rodrigues (carlosrodriguesf96@gmail.com)
 */
public interface AutoScreenshotListener {
	public void onScreenshot(BufferedImage image);
}

package remotedesktop.server.autoscreenshot;

import java.awt.AWTException;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe responsável por tirar screenshots da tela e chamar um evento para
 * receber as imagens
 * 
 * @author Carlos Rodrigues (carlosrodriguesf96@gmail.com)
 */
public class AutoScreenshot extends Thread {

	private Robot robot;
	private Rectangle imageSize;
	private List<AutoScreenshotListener> listeners;
	private float widthP;

	public AutoScreenshot() throws AWTException {
		this.robot = new Robot();
		this.imageSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
		this.listeners = new ArrayList<AutoScreenshotListener>();
		this.widthP = (float) 0.8;
	}

	/**
	 * Adiciona um listener para ser chamado sempre que uma nova screenshot for
	 * obtida
	 * 
	 * @param listener
	 */
	public void addAutoScreenshotListener(AutoScreenshotListener listener) {
		this.listeners.add(listener);
	}

	/**
	 * Recebe um image e aplica alguns tratamento para reduzir a qualidade e
	 * consequentemente o tamanho
	 * 
	 * @param buffImage
	 * @return BufferedImage
	 */
	private BufferedImage prepare(BufferedImage buffImage) {
		float width = buffImage.getWidth();
		float height = buffImage.getHeight();

		/**
		 * Obtém a porcentagem da altura em relação a largura Reduz a largura à
		 * porcentagem definida Calcula a nova altura com base na porcentagem
		 * obtida anteriormente
		 */
		float percent = height / width;
		width = width * this.widthP;
		height = percent * width;

		/**
		 * Cria uma nova umagem vazia Gera o objeto Graphics2D(Responsável por
		 * desenhar a imagem antiga na nova imagem) Obtém uma imagem
		 * redimensionada e desenha ela na nova imagem
		 */
		BufferedImage newImage = new BufferedImage((int) width, (int) height, buffImage.getType());
		Graphics2D g2d = newImage.createGraphics();
		g2d.drawImage(buffImage.getScaledInstance((int) width, (int) height, Image.SCALE_AREA_AVERAGING), 0, 0,
				(int) width, (int) height, null);
		g2d.dispose();

		return newImage;
	}

	/**
	 * Método executado quando a thread for iniciada
	 */
	@Override
	public void run() {
		while (true) {
			BufferedImage buffImage = prepare(this.robot.createScreenCapture(this.imageSize)); // Tratamento
			for (AutoScreenshotListener listener : this.listeners) {
				try {
					listener.onScreenshot(buffImage);
				} catch (Exception ignore) {}
			}
		}
	}
}

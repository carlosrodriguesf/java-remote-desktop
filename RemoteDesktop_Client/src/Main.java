import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Cliente Simples para acesso remoto em java para fins educacionais. Recebe os
 * dados do servidor e exibe em uma janela
 * 
 * @author Carlos Rodrigues
 */
public class Main {
	private static Socket socket;
	private static ObjectInputStream inputStream;
	private static PrintStream outputStream;
	private static Thread listener;
	private static Dimension windowSize;

	private static void showException(Exception e) {
		JOptionPane.showMessageDialog(null, e.getMessage(), e.getClass().getName(), JOptionPane.ERROR_MESSAGE, null);
	}

	/**
	 * Cria uma janeça para exibir o desktop remoto
	 * 
	 * @return JLabel
	 */
	private static JImagePanel createFrame() {
		int width = 700, height = width;
		
		JImagePanel imagePanel = new JImagePanel();

		JFrame frm = new JFrame();
		frm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frm.setBounds(100, 100, 700, 530);
		frm.setContentPane(imagePanel);
		frm.setVisible(true);
		
		Main.windowSize = frm.getSize();
		
		/**
		 * quando a janela for fechada o código abaixo será executado e avisará ao servidor que 
		 * as conexões serão encerradas
		 */
		frm.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				try {
					Main.disconnect();
				} catch (IOException e1) {
					Main.showException(e1);
				}
			}
		});
		frm.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				Main.windowSize = e.getComponent().getSize();
			}
		});

		return imagePanel;
	}

	/**
	 * Inicia a thread que recebe os bytes, converte em image e define como
	 * ícone na JLabel
	 * 
	 * @param label
	 */
	private static void startListener(final JImagePanel imgPanel) {
		Main.listener = new Thread() {
			public void run() {
				Object data;
				while (true) {
					try {
						if (!((data = Main.inputStream.readObject()) instanceof byte[])) {
							continue;
						}

						imgPanel.setImage(ImageIO.read(new ByteArrayInputStream((byte[]) data)));
					} catch (ClassNotFoundException | IOException e) {
						Main.showException(e);
						try {
							Main.disconnect();
						} catch (IOException e1) {
							System.exit(0);
						}
					}
				}
			}
		};
		Main.listener.start();
	}

	/**
	 * Inicia a conexão com o servidor
	 * 
	 * @param addr
	 * @param port
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private static void connect(String addr, int port) throws UnknownHostException, IOException {
		Main.socket = new Socket(addr, port);
		Main.inputStream = new ObjectInputStream(Main.socket.getInputStream());
		Main.outputStream = new PrintStream(Main.socket.getOutputStream());
	}

	/**
	 * Encerra a conexão com o servidor
	 * 
	 * @throws IOException
	 */
	private static void disconnect() throws IOException {
		Main.listener.interrupt();
		Main.outputStream.println("DISCONNECT");
		Main.outputStream.close();
		Main.inputStream.close();
		Main.socket.close();

		System.exit(0);
	}

	public static void main(String[] args) {
		try {
			Main.connect("localhost", 12345);
			Main.startListener(Main.createFrame());
		} catch (IOException e) {
			Main.showException(e);
		}
	}

}

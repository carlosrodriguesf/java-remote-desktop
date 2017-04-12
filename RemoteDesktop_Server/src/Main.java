import java.awt.AWTException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import remotedesktop.server.Logger;
import remotedesktop.server.Server;

/**
 * Esse é um software de código aberto para área de trabalho remota desenvolvido em java.
 * Esse sistema foi desenvolvido para fins educacionais e não deve ser usado de forma comercial.
 * 
 * O Mesmo utiliza o protocolo TCP/IP para fazer o envio e recebimento de dados entre computadores.
 * 
 * Qualquer pessoa tem autorização para utilizar, modificar e redistribuir o código desde que não
 * seja cobrado pelo software em si.
 * 
 * Esse sistema não deve ser usado de forma maliciosa e aqueles que o fizerem serão devidamente
 * responsabilizados.
 * 
 * @author Carlos Rodrigues (carlosrodriguesf96@gmail.com)
 * @repository https://github.com/carlosrodriguesf/java-remote-desktop
 */

public class Main {
	private static final boolean ENABLE_LOGGING = false;
	
	public static void main(String[] args) {
		config();
		
		Logger logger = Logger.getInstance();
		
		logger.info("Iniciando Servidor...");
		try {
			Server server = new Server(12345);
			server.start();
		} catch (IOException | AWTException e) {
			logger.printStackTrace(e);
		}
	}
	
	private static void config() {
		Logger logger = Logger.getInstance();
		if(!ENABLE_LOGGING) {
			logger.disable();
		}
		//logger.addOutputStream(System.out);
		try {
			logger.addOutputStream(new FileOutputStream(new File("log.txt"), true));
		} catch (FileNotFoundException ignore) {
		}
		logger.start();
	}
}

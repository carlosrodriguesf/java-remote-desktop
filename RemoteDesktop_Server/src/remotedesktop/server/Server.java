package remotedesktop.server;

import java.awt.AWTException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import remotedesktop.server.autoscreenshot.AutoScreenshot;
import remotedesktop.server.autoscreenshot.AutoScreenshotListener;
import remotedesktop.server.client.Client;
import remotedesktop.server.client.ClientListener;

/**
 * Éssa é a classe Server. 
 * Extende da classe Thread, assumindo assim o comportamento da mesma de iniciar um processamento
 * paralelo. Necessário para que se fique escutando uma determinada porta e adicionando novas conexões
 * 
 * @author Carlos Rodrigues (carlosrodriguesf96@gmail.com)
 */
public class Server extends Thread implements AutoScreenshotListener, ClientListener {
	private Logger logger;
	private ServerSocket serverSocket;
	private AutoScreenshot screenShooter;
	private List<Client> clients;
	
	/**
	 * Construtor
	 * @param port
	 * @throws IOException
	 * @throws AWTException
	 */
	public Server(int port) throws IOException, AWTException {
		this.logger = Logger.getInstance();
		this.serverSocket = new ServerSocket(port);
		this.clients = new ArrayList<Client>();
	}
	
	/**
	 * Grencia inicia e encerra o processo de auto screenshot.
	 * Esse processo necessita dessa gerência, pois caso não haja nenhum cliente conectado,
	 * o mesmo estará desperdiçando recursos do sistema, o que pode acarretar em perda de 
	 * desempenho para o usuário do computador
	 * 
	 * Recebe um parâmetro booleano para definir se o processo será iniciado ou interrompido,
	 * true para iniciar e false para interromper.
	 * 
	 * Caso esteja tentando iniciar o processo, primeiramente é verificado se já existe
	 * um processo rodando, verificando se a variavel screeshooter é nula, se sim, não existem 
	 * processos de auto-screenshot, e é iniciado um novo processo, se não, nada é feito.
	 * 
	 * Para o caso de interrupção do processo, é feito o inverso do descrito anteriormente
	 * @param start
	 */
	private void startAutoScreenshot(boolean start) {
		if (!start) { 
			if (this.screenShooter == null) 
				return; 

			this.logger.info("Interrompendo Auto-Screenshot...");

			this.screenShooter.interrupt();
			this.screenShooter = null;

			return;
		}

		if (this.screenShooter != null) 
			return;

		this.logger.info("Iniciando auto screenshot");
		try {
			this.screenShooter = new AutoScreenshot();
			this.screenShooter.addAutoScreenshotListener(this);
			this.screenShooter.start();
		} catch (AWTException e) {
			this.logger.printStackTrace(e);
			System.exit(1);
		}

	}
	
	/**
	 * Adiciona um novo cliente à lista de clientes conectados.
	 * 
	 * Recebe um objeto do tipo Socket, e usa-o para instanciar uma classe cliente,
	 * que é a classe responsavel por tratar da comunicação com um cliente única.
	 * Após a instância do objeto cliente o mesmo é adicionado à lista de clientes conectados.
	 * 
	 * Após isso é chamado o método para iniciar o processo de auto screenshot
	 * 
	 * @param socket
	 * @throws IOException
	 */
	private void addNewClient(Socket socket) throws IOException {
		this.logger.info("Novo cliente conectado!");

		Client client = new Client(socket, this);
		client.start();

		this.clients.add(client);

		this.logger.info(client.getClientDetailsAsString());
		
		this.startAutoScreenshot(true);
	}
	
	/**
	 * Esse método é executado quando a nova thread é iniciada através do método start
	 */
	@Override
	public void run() {
		while (true) {
			try {
				this.addNewClient(this.serverSocket.accept());
			} catch (Exception e) {
				this.logger.printStackTrace(e);
			}
		}
	}

	/**
	 * Nova screenshot pronta tratada e pronta para ser enviada.
	 * Esse método é um dos métodos do AutoScreenshotListener, é chamado sempre que
	 * ele tira uma nova screenshot.
	 * 
	 * Esse método transforma a imagem em bytes, e percorre a lista de clientes solicitando
	 * o envio dos bytes para suas devidas máquinas
	 */
	@Override
	public void onScreenshot(BufferedImage image) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		try {
			ImageIO.write(image, "gif", output);
		} catch (IOException e) {
			e.printStackTrace();
		}

		byte[] bytes = output.toByteArray();

		/**
		 * O Codigo abaixo é um foreach, uma nova instância do array é criada
		 * pois existem outras threads que podem entar alterar a lista original
		 * durante o processamento do foreach, o que ocasionaria um erro, pois a lista
		 * não pode sofrer alterações durante esse processamento, então o foreach percorre a cópia
		 * enquanto a variavel original fica livre para receber novos elementos.
		 */
		for (Client client : new ArrayList<Client>(this.clients)) {
			client.sendBytes(bytes);
		}
	}
	
	/**
	 * Chamado quando um cliente é desconectado.
	 * 
	 * Sempre que um cliente é desconectado, o objeto Cliente é removido da lista.
	 * Caso não existam mais clientes na lista, significa que não existem clientes conectados,
	 * sendo assim, o processo de auto-screenshot é interrompido, para liberar os recursos do sistema
	 */
	@Override
	public void onDisconnected(Client client) {
		this.clients.remove(client);

		this.logger.info("Cliente desconectado: " + client.getClientId());

		if (this.clients.size() == 0) {
			this.startAutoScreenshot(false);
		}
	}
}

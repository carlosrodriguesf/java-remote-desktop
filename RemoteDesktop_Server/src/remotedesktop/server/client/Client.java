package remotedesktop.server.client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

import remotedesktop.server.Logger;
import remotedesktop.server.datasender.DataSender;
import remotedesktop.server.datasender.DataSenderListener;

/**
 * Éssa classe é responsável por tratar de toda a comunicação com um cliente conectado
 * 
 * @author Carlos Rodrigues (carlosrodriguesf96@gmail.com)
 */
public class Client extends Thread implements DataSenderListener {
	private static int idInc = 1;

	private Logger logger;
	private int id;
	private Socket socket;
	private ObjectOutputStream outputStream;
	private Scanner inputStream;
	private ClientListener listener;
	private boolean sendLocked;
	private int errorCount;

	/**
	 * No momento da instância da classe, o cliente recebe um id único
	 * 
	 * @param socket
	 * @param listener
	 * @throws IOException
	 */
	public Client(Socket socket, ClientListener listener) throws IOException {
		this.logger = Logger.getInstance();
		this.id = idInc++;
		this.socket = socket;
		this.outputStream = new ObjectOutputStream(socket.getOutputStream());
		this.inputStream = new Scanner(socket.getInputStream());
		this.sendLocked = false;
		this.listener = listener;
		
		
	}
	
	/**
	 * Retorna detalhes do cliente em uma string formatada
	 * 
	 * @return String
	 */
	public String getClientDetailsAsString() {
		StringBuilder builder = new StringBuilder();
		builder.append("\tClient Id: %s\n");
		builder.append("\tHost Address: %s\n");
		builder.append("\tHost Name: %s\n");
		builder.append("\tPort: %s\n");
		
		InetAddress inet = this.socket.getInetAddress();
		
		return String.format(builder.toString(), this.getClientId(), inet.getHostAddress(), inet.getHostName(), this.socket.getPort(), this.socket.getLocalPort());
	}

	public int getClientId() {
		return id;
	}
	
	/**
	 * Realiza os procedimentos de desconexão.
	 * Primeiramente o método tenta encerrar a conexão com o cliente,
	 * em caso de erro, primeiramente ele executa o evento de desconexão para que o servidor
	 * descarte o cliente. e posteriormente o erro é lançado
	 * 
	 * @throws IOException
	 */
	private void disconnect() throws IOException {
		this.sendLocked = true;

		IOException e = null;
		try {
			this.inputStream.close();
			this.outputStream.close();
			this.socket.close();
		} catch (IOException e1) {
			e = e1;
		}
		
		this.listener.onDisconnected(this);
		
		if(e != null) {
			throw e;
		}
	}
	
	/**
	 * Método chamado quando é necessario enviar dados em bytes para o cliente
	 * Esse método monta um pacote passando os bytes e o OutputStream, para que os dados sejam
	 * enviados de forma assincrona, assim caso um cliente tenha uma conexão muito lenta, ou
	 * não esteja recebendo os dados por algum motivo, esse cliente não travará o sistema
	 * inteiro, apenas o processo em que ele está rodando, que sendo um processó exclusivo para o 
	 * envio de dados, não trará danos ao sistema.
	 * 
	 * @param bytes
	 */
	public void sendBytes(byte[] bytes) {
		if (!this.sendLocked) {
			DataSender sender = new DataSender(bytes, this.outputStream, this);
			sender.start();
		}
	}

	/**
	 * Método executado quando a Thread é iniciada.
	 * É responsável por escutar o InputStream, caso o cliente envie algum comando.
	 * No momento o único comando implementado é o de desconectar, porém é possível implementar
	 * outros comandos posteriormente, como controle de mouse e teclado
	 */
	@Override
	public void run() {
		while (this.inputStream.hasNextLine()) {
			String line = this.inputStream.nextLine();
			if (line.equals("DISCONNECT")) {
				try {
					this.disconnect();
				} catch (IOException e) {
					this.logger.printStackTrace(e);
				}
				break;
			}
		}
	}

	/**
	 * Todos os métodos abaixo pertencem à implementação do DataSenderListener e são chamados
	 * pelos data senders durante o processo de envio de dados.
	 * 
	 * onPrepare é chamado logo no início, a thread de envio é iniciada. Nesse caso esse evento
	 * muda o valor da variável sendLocked, que impede que novos datasenders sejam criados.
	 * Caso alguma imagem chege durante o envio de uma anterior, a nova será descartada.
	 * 
	 * onStart é chamado quando o processo de envio é iniciado
	 * É verificado se ocorreu algum erro antes, para que seja emitido um logo customizado
	 * 
	 * onSuccess é chamado se o envio for concluído com erros.
	 * É verificado se ocorreu algum erro antes, para que seja emitido um logo customizado.
	 * Caso tenham ocorrido erros anteriormente, o contador de erros é zerado.
	 * 
	 * onErro é chamado caso ocorra algum erro, nesse caso é recebido uma Exception e um contador
	 * de erros é incrementado. Caso ocorram 10 erros seguidos, o cliente é desconectado.
	 * 
	 * onComplete sempre será chamado, é chamado por último, quando o processo de envio ja foi
	 * concluído, seja com erro ou não. Nesse evendo a variavel sendLocked muda de valor
	 * e a criação de novos pacotes de envio é liberada
	 */
	@Override
	public void onPrepare(DataSender clientDataSender) {
		this.sendLocked = true;
	}

	@Override
	public void onStart(DataSender clientDataSender) {
		if(this.errorCount > 0) {
			this.logger.info("Tentando novamente...");
		}
	}

	@Override
	public void onSuccess(DataSender clientDataSender) {
		if(this.errorCount > 0) {
			this.logger.info("Sucesso ao enviar dados, zerando contador de erros.");
			this.errorCount = 0;
		}
	}

	@Override
	public void onError(DataSender clientDataSender, IOException e) {
		++ this.errorCount;
		
		this.logger.error(String.format("Client (%d): Erro ao enviar dados. %d/10", this.getClientId(), this.errorCount));
		
		if( this.errorCount >= 10) {
			try {
				this.disconnect();
			} catch (IOException e1) {
				this.logger.printStackTrace(e1);
				System.exit(1);
			}
		}
	}

	@Override
	public void onComplete(DataSender clientDataSender) {
		this.sendLocked = false;
	}
}

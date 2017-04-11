package remotedesktop.server.datasender;

import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Éssa classe é responsável por enviar os dados por um outputStream de forma assincrona
 * 
 * @author Carlos Rodrigues (carlosrodriguesf96@gmail.com)
 */
public class DataSender extends Thread {
	private byte[] data;
	private ObjectOutputStream outputStream;
	private DataSenderListener listener;
	
	/**
	 * O Construtor recebe os dados a serem enviados, o outputStream e o listener, que escutará
	 * os eventos.
	 * 
	 * @param data
	 * @param outputStream
	 * @param listener
	 */
	public DataSender(byte[] data, ObjectOutputStream outputStream, DataSenderListener listener) {
		this.data = data;
		this.outputStream = outputStream;
		this.listener = listener;
	}
	
	/**
	 * Aqui o processo de envio é iniciado em um novo processo e os eventos são chamados de 
	 * acordo com o stado do processo.
	 */
	@Override
	public void run () {
		try {
			this.listener.onPrepare(this);
			
			this.listener.onStart(this);
			this.outputStream.writeObject(this.data);
			this.outputStream.flush();
			
			this.listener.onSuccess(this);
		} catch (IOException e) {
			this.listener.onError(this, e);
		}
		
		this.listener.onComplete(this);
	}
}

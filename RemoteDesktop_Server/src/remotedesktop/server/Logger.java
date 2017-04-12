package remotedesktop.server;

import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Essa classe grava os logs do sistema
 * 
 * @author Carlos Rodrigues (carlosrodriguesf96@gmail.com)
 */
public class Logger {
	enum Category {
		ERROR, INFO, WARNING
	}

	private boolean disabled = false;

	private List<PrintStream> printers;
	private SimpleDateFormat dateFormat;
	private boolean started;

	private Logger() {
		this.printers = new ArrayList<PrintStream>();
		this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		this.started = false;
	}

	private String printAll(String text) {
		for (PrintStream printer : this.printers) {
			printer.print(text);
		}
		return text;
	}

	private String getDataAtual() {
		return this.dateFormat.format(Calendar.getInstance().getTime());
	}

	public void start() {
		this.printAll("\nNovo log iniciado em " + this.getDataAtual() + "\n\n");
		this.started = true;
	}

	public void printStackTrace(Exception e) {
		e.printStackTrace();
	}

	/**
	 * Adiciona espaçoes em branco na frente do texto ate o mesmo ter o
	 * comprimento igual ao atributo length
	 * 
	 * @param text
	 * @param length
	 * @return String
	 */
	private String textFormat(String text, int length) {
		StringBuilder blankSpaces = new StringBuilder("");

		for (int i = 0; i < length - text.length(); i++) {
			blankSpaces.append(' ');
		}

		return blankSpaces.append(text).toString();
	}

	/**
	 * Adiciona stream de saida para o log
	 * 
	 * @param outputStream
	 * @return Logger
	 */
	public Logger addOutputStream(OutputStream outputStream) {
		if (this.started) {
			throw new RuntimeException("The logger is started!");
		}

		this.printers
				.add(outputStream instanceof PrintStream ? (PrintStream) outputStream : new PrintStream(outputStream));

		return this;
	}

	/**
	 * Grava os logs em todas as saídas adicionadas
	 * 
	 * @param category
	 * @param msg
	 * @return String
	 */
	public String log(Category category, String msg) {
		if (this.disabled) {
			return msg;
		}
		if (!this.started) {
			throw new RuntimeException("This logger is not started!");
		}

		msg = msg.replaceAll("\n", "\n\t");

		StackTraceElement traceElement = Thread.currentThread().getStackTrace()[3];
		String location = String.format("%s.%s(%d)", traceElement.getClassName(), traceElement.getMethodName(),
				traceElement.getLineNumber());

		return this.printAll(String.format("%s | %s | %s\n\t%s\n", this.getDataAtual(),
				this.textFormat(category.toString(), 7), location, msg));
	}

	public void info(String msg) {
		this.log(Category.INFO, msg);
	}

	public void error(String msg) {
		this.log(Category.ERROR, msg);
	}

	public void warning(String msg) {
		this.log(Category.WARNING, msg);
	}

	public void disable() {
		this.disabled = true;
	}

	private static Logger instance;

	public static Logger getInstance() {
		if (instance == null) {
			instance = new Logger();
		}
		return instance;
	}
}

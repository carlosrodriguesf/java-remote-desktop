package remotedesktop.server.datasender;

import java.io.IOException;

public interface DataSenderListener {
	public void onPrepare(DataSender clientDataSender);
	public void onStart(DataSender clientDataSender);
	public void onSuccess(DataSender clientDataSender);
	public void onError(DataSender clientDataSender, IOException e);
	public void onComplete(DataSender clientDataSender);
}

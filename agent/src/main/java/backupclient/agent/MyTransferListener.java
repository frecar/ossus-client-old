package backupclient.agent;

import java.util.Date;

import backupclient.commons.Machine;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;

public class MyTransferListener implements FTPDataTransferListener {

	private int totalBytesUploaded;
	private long totalBytes;
	private int percentCompleted;
	private Machine machine;
	private FTPClient client;
	private Date datetimeStarted;

	public MyTransferListener(Machine machine, FTPClient client, long l) {
		this.totalBytes = l;
		this.machine = machine;
		this.client = client;
		this.datetimeStarted = new Date();
	}

	public void started() {
		totalBytesUploaded = 0;
		percentCompleted = 0;
		this.machine.log_info("Transfer started");
	}

	public void transferred(int length) {

		totalBytesUploaded += length;

		Float p = ((float) totalBytesUploaded/(float) totalBytes);

		int percent = (int)(100 * (p));
		
		if(percent > this.percentCompleted) {


			long ms = new Date().getTime() - this.datetimeStarted.getTime();

			if(ms>=1000 && (percent % 10 == 0 || percent == 1)) {
				ms /= 1000;
				this.machine.log_info(percent + " % completed. " + (totalBytesUploaded/1024)/ms + " kb/s");				
			}

			this.percentCompleted = percent;
		}		
	}

	public void completed() {
		this.machine.log_info("Transfer complete");
	}

	public void aborted() {
		try {
			this.client.abortCurrentDataTransfer(true);
		} catch (Exception e) {
			this.machine.log_error("Transfer aborted");
		}
	}

	public void failed() {
		try {
			this.client.abortCurrentDataTransfer(true);
		} catch (Exception e) {
			this.machine.log_error("Transfer failed");
		}
	}
}
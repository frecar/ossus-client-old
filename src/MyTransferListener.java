import java.util.Date;

import it.sauronsoftware.ftp4j.FTPDataTransferListener;

public class MyTransferListener implements FTPDataTransferListener {

	private int totalBytesUploaded;
	private long totalBytes;
	private int percentCompleted;
	private Machine machine;
	private Date datetimeStarted;

	public MyTransferListener(Machine machine, long l) {
		this.totalBytes = l;
		this.machine = machine;
		this.datetimeStarted = new Date();
	}

	public void started() {
		totalBytesUploaded = 0;
		percentCompleted = 0;
		this.machine.log_error("Transfer started");
	}

	public void transferred(int length) {

		totalBytesUploaded += length;

		Float p = ((float) totalBytesUploaded/(float) totalBytes);

		int percent = (int)(100 * (p));
		
		if(percent >= this.percentCompleted+1) {


			long ms = new Date().getTime() - this.datetimeStarted.getTime();

			if(ms>=1000) {
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
		System.out.println("transfer abort");
		throw new RuntimeException("Transfer aborted");
	}

	public void failed() {
		System.out.println("transfer fail");
		throw new RuntimeException("Transfer failed");
	}

}
import java.util.ArrayList;
import java.util.List;

public class Schedule {

	private String id;
	private String name;
	private Boolean running_backup;
	private Boolean running_restore;
	private Integer current_version_in_loop;
	private String upload_path;

	private FTPStorage storage;
	private Machine machine;

	private List<FolderBackup> folderBackups = new ArrayList<FolderBackup>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getRunning_backup() {
		return running_backup;
	}

	public void setRunning_backup(Boolean running_backup) {
		this.running_backup = running_backup;
	}

	public Boolean getRunning_restore() {
		return running_restore;
	}

	public void setRunning_restore(Boolean running_restore) {
		this.running_restore = running_restore;
	}

	public Integer getCurrent_version_in_loop() {
		return current_version_in_loop;
	}

	public void setCurrent_version_in_loop(Integer current_version_in_loop) {
		this.current_version_in_loop = current_version_in_loop;
	}

	public String getUpload_path() {
		return upload_path;
	}

	public void setUpload_path(String upload_path) {
		this.upload_path = upload_path;
	}

	public FTPStorage getStorage() {
		return storage;
	}

	public void setStorage(FTPStorage storage) {
		this.storage = storage;
	}

	public Machine getMachine() {
		return machine;
	}

	public void setMachine(Machine machine) {
		this.machine = machine;
	}

	public void runBackup() {

		FTPStorage ftpStorage = new FTPStorage();

		try {
			ftpStorage.client.changeDirectory(this.upload_path);			

		} catch (Exception e) {

			try {
				ftpStorage.client.createDirectory(this.upload_path);			
			}
			catch (Exception es) {
			}
		}

		for (FolderBackup folderBackup : this.getFolderBackups()) {

			String filename_zip =  "file.zip";
			
			try {
				Zipper.zipDir(this.machine.local_temp_folder + filename_zip, folderBackup.getPath());

				ftpStorage.upload(this.upload_path, this.machine.local_temp_folder + filename_zip);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public List<FolderBackup> getFolderBackups() {
		return folderBackups;
	}

	public void addFolderBackup(FolderBackup folderBackup) {
		this.folderBackups.add(folderBackup);
	}

}

class FolderBackup {
	private String id;
	private String path;
	private Schedule schedule;

	public Schedule getSchedule() {
		return this.schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
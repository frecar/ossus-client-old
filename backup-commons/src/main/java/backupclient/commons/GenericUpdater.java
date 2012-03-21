package backupclient.commons;

import org.json.simple.JSONObject;

import java.io.*;
import java.net.URL;
import java.util.List;

abstract public class GenericUpdater implements Runnable {

    protected final Machine machine;
    private final APIHandler api_handler;

    protected GenericUpdater(Machine machine) {
        this.machine = machine;
        this.api_handler = machine.apiHandler;
    }
    
    
    protected abstract Version current_version();
    protected abstract Version selected_version();
    protected abstract String out_file_name();
    protected abstract String version_url();
    protected abstract URL download_link(Version v);

    protected abstract void download_done(Version new_version);
    
    public void run() {
        if (!machine.auto_update &&
                (current_version().equals(selected_version()))) {
            return;
        }

        Version selected = getSelectedVersion();
        if (!selected.equals(current_version())) {
            download_version(selected);
            download_done(selected);
        }
    }
    
    private Version getSelectedVersion() {
        if (!machine.auto_update) return selected_version();

        List<JSONObject> json_data = api_handler.get_api_data(version_url());
        if (json_data.size() != 1) {
            machine.log.log_error("Failed getting version data");
            return null;
        }

        JSONObject version_data = json_data.get(0);
        return Version.buildFromJson(version_data);
    }

    private void download_version(Version version) {
        machine.log_info("Downloading new version");
        machine.log_info("Current version: " + current_version());
        machine.log_info("New version: " + version);
        
        URL jar_url = download_link(version);
        if (jar_url != null) try {
            readAndWriteJar(jar_url);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void readAndWriteJar(URL jar_url) throws IOException {
        if (jar_url == null) throw new IOException("URL argument is null");  // not really an ioexception, fix?
        
        BufferedOutputStream file_out = create_updater_file();
        BufferedInputStream in = new BufferedInputStream(jar_url.openStream());

        machine.log_info("Starting download");

        int read, total = 1;
        byte[] buff = new byte[8192];
        while ((read = in.read(buff)) != -1) {
            total += read;
            file_out.write(buff, 0, read);
        }
            
        file_out.flush();
        machine.log_info("Done downloading new version. Total of " + total + "bytes read");
        file_out.close();
        in.close();

    }

    private BufferedOutputStream create_updater_file() throws IOException {
        machine.log_info("Creating local file: " + out_file_name() );
        File jar_file = new File(out_file_name());
        if (jar_file.exists()) {
            new RandomAccessFile(jar_file, "rw").setLength(0);
        }
        else {
            boolean created = jar_file.createNewFile();
            if (!created) throw new IOException("Could not create file " + jar_file );
        }

        machine.log_info("local file created");
        return new BufferedOutputStream(new FileOutputStream(jar_file));
    }

}

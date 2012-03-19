package backupclient.commons;

import org.json.simple.JSONObject;

import java.util.List;

abstract public class GenericUpdater implements Runnable {

    protected final Machine machine;
    private final APIHandler api_handler;

    protected GenericUpdater(Machine machine) {
        this.machine = machine;
        this.api_handler = machine.apiHandler;
    }
    
    
    protected abstract String current_version();
    protected abstract String selected_version();
    protected abstract String version_url();


    public void run() {
        if (!machine.auto_update &&
                (current_version().equals(selected_version()))) {
            return;
        }
        
        Version selected = getSelectedVersion(machine.auto_update);
        if (!selected.name.equals(current_version())) {
            machine.log.log_info("New version of updater found");
            download_version(selected);
        }
    }
    
    private Version getSelectedVersion(boolean auto) {
        List<JSONObject> json_data = api_handler.get_api_data(version_url());
        if (json_data.size() != 1) {
            machine.log.log_error("Failed getting version for updater");
            return null;
        }

        JSONObject version_data = json_data.get(0);
        return new Version(
                version_data.get("name").toString(),
                version_data.get("updater_link").toString()
        );
    }

    private void download_version(Version version) {
        
    }

    protected static class Version {
        private String name, update_link;
        private Version(String name, String update_link) {
            this.name = name;
            this.update_link = update_link;
        }

    }
}

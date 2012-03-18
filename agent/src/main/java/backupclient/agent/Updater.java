package backupclient.agent;

import org.json.simple.JSONObject;

import java.util.List;

public class Updater implements  Runnable {

    private Machine machine;
    private APIHandler api_handler;
    private Log log;

    public Updater(Machine machine) {
        this.machine = machine;
        this.api_handler = new APIHandler(machine.server_ip +"/api", machine.username, machine.password);
        this.log = new Log(api_handler, machine);
    }


    public void run() {

        APIHandler apiHandler = new APIHandler(machine.server_ip +"/api", machine.username, machine.password);

        Version selected = getSelectedVersion(machine.auto_update);
        if (! selected.name.equals(machine.current_updater_version)) {
            log.log_info("New version of updater found");
            download_version(selected);
        }
    }


    private Version getSelectedVersion(boolean auto) {
        String version = auto ? "current_updater/" : machine.selected_updater_version +"/";
        List<JSONObject> data = api_handler.get_api_data("clientversions/" + version);
        if (data.size() != 1) {
            log.log_error("Failed getting version for updater");
            return null;
        }

        JSONObject version_data = data.get(0);
        return new Version(
                version_data.get("name").toString(),
                version_data.get("updater_link").toString()
        );

    }

    private void download_version(Version version) {
        log.log_info("Getting new version of Updater: " + version.name);
    }


    private static class Version {
        private String name, update_link;
        private Version(String name, String update_link) {
            this.name = name;
            this.update_link = update_link;
        }

    }

}

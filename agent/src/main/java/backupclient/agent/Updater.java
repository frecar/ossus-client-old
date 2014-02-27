package backupclient.agent;

import backupclient.commons.GenericUpdater;
import backupclient.commons.Machine;
import backupclient.commons.Version;

import java.net.MalformedURLException;
import java.net.URL;

public class Updater extends GenericUpdater {

    private final String updater_file_name = "Updater.jar";

    public Updater(Machine machine) {
        super(machine);
    }

    @Override
    protected Version current_version() {
        return machine.get_current_updater_version();
    }

    @Override
    protected Version selected_version() {
        return machine.selected_updater_version;
    }

    @Override
    protected String version_url() {
        return "client_versions/" + ((machine.auto_update) ? "current_updater/" : selected_version().name);
    }

    @Override
    protected String out_file_name() {
        return machine.get_agent_folder()+updater_file_name;
    }

    @Override
    protected URL download_link(Version v) {
        try {
            return new URL(v.updater_link);
        } catch (MalformedURLException e) {
            machine.log_error(e.toString());
            return null;
        }
    }

    @Override
    protected void download_done(Version new_version) {
        machine.set_current_updater_version(new_version);
    }
}
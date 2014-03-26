package backupclient.updater;

import backupclient.commons.*;

import java.net.MalformedURLException;
import java.net.URL;

public class AgentUpdater extends GenericUpdater {

    private final String agent_file_name = "Agent.jar";

    public AgentUpdater(Machine machine) {
        super(machine);
    }

    @Override
    protected Version current_version() {
        return machine.get_current_agent_version();
    }

    @Override
    protected Version selected_version() {
        return machine.selected_agent_version;
    }

    @Override
    protected String version_url() {
        return "client_versions/" + ((machine.auto_update) ? "current_agent/" : selected_version().name);
    }

    @Override
    protected String out_file_name() {
        return machine.get_agent_folder() + agent_file_name;
    }

    @Override
    protected URL download_link(Version v) {
        try {
            return new URL(v.agent_link);
        } catch (MalformedURLException e) {
            machine.log_error(e.toString());
            return null;
        }
    }

    @Override
    protected void download_done(Version new_version) {
        machine.set_current_agent_version(new_version);
    }

    public static void main(String... args) {

        String settingsLocation = args.length > 0 ? args[0] : "settings.json";
        Machine machine = Machine.buildFromSettings(settingsLocation);

        while ((System.currentTimeMillis() / 1000) < machine.session) {
        }

        if (machine.isBusy()) {
            machine.log_warning("Updater: Machine busy, skipping!");
            return;
        }

        machine.setBusyUpdating(true);
        machine.log_info("Updater: Set busy!");

        try {
            new AgentUpdater(machine).run();

        } catch (Exception e) {
            machine.log_error(e.toString());
        } finally {
            machine.setBusyUpdating(false);
            machine.log_info("Updater: Set not busy!");
        }
    }
}
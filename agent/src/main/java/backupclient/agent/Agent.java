package backupclient.agent;

import backupclient.commons.CrossProcessLock;
import backupclient.commons.Machine;
import backupclient.install.Installer;

import java.io.IOException;
import java.text.ParseException;

public class Agent {

    public static void main(String[] args) throws ParseException {

        String settingsLocation = args.length > 0 ? args[0] : "settings.json";
        Machine machine = Machine.buildFromSettings(settingsLocation);

        while ((System.currentTimeMillis() / 1000) < machine.session) {
        }

        if (machine.isBusy()) {
            machine.log_warning("Agent: Machine busy, skipping!");
            return;
        }

        machine.setBusyUpdating(true);
        machine.log_info("Agent: Set busy!");

        try {

            if (machine.run_install) {
                try {
                    new Installer().runInstall();
                } catch (IOException e) {
                    e.printStackTrace();
                    machine.log_error("Error running install");

                    machine.setBusyUpdating(false);
                    machine.log_info("Agent: Set not busy!");

                }
            }

            MachineStats machinestats = new MachineStats(machine);
            machinestats.save();

            new Updater(machine).run();
            new BackupJob(machine).runBackup();

        } catch (Exception e) {
            machine.log_error(e.toString());
        } finally {
            machine.setBusyUpdating(false);
            machine.log_info("Agent: Set not busy!");
        }
    }
}
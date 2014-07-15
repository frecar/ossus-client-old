package backupclient.agent;

import backupclient.commons.Machine;
import backupclient.install.Installer;

import java.io.IOException;
import java.text.ParseException;

public class Agent {

    public static void main(String[] args) throws ParseException {

        String settingsLocation = args.length > 0 ? args[0] : "settings.json";
        Machine machine = Machine.buildFromSettings(settingsLocation);

        if (machine.isBusy()) {
            machine.log_warning("Agent: Machine busy, skipping!");
            return;
        }

        if (!machine.isBusy() && machine.changesBusyStatus(true)) {
            machine.log_info("Agent: Set busy!");

            if (!machine.isBusy()) {
                machine.log_error("Agent: Something is wrong, the status should be busy by now.. but is not?");
                return;
            }

            try {
                if (machine.run_install) {
                    try {
                        new Installer().runInstall();
                    } catch (IOException e) {

                        e.printStackTrace();
                        machine.log_error("Error running install");

                        if(machine.changesBusyStatus(false)) {
                            machine.log_info("Agent: Set not busy!");
                        }
                        else {
                            machine.log_error("Agent: Something is wrong, the install failed and not I'm not able to set busy to false.");
                            return;
                        }
                    }
                }

                MachineStats machinestats = new MachineStats(machine);
                machinestats.save();

                new Updater(machine).run();
                new BackupJob(machine).runBackup();

            } catch (Exception e) {
                machine.log_error(e.toString());
            } finally {
                if (machine.changesBusyStatus(false)) {
                    machine.log_info("Agent: Set not busy!");
                } else {
                    machine.log_error("Agent: Something is wrong! " +
                            "The status should have changed back to not busy.");
                }
            }
        }
    }
}
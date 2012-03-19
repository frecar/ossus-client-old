package backupclient.agent;

import backupclient.commons.CrossProcessLock;
import backupclient.commons.Machine;
import backupclient.commons.XMLHandler;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class Agent {

	public static void main(String[]args) throws ParseException {

        System.out.println("A new test version!");
        if (1==1) return;
        boolean update_lock = CrossProcessLock.instance.tryLock(0);
        if (!update_lock) return; // TODO log this?
        
		String settingsLocation = "settings.xml";
		try {
			settingsLocation = args[0];
		}
		catch(Exception e) {}
		
		Machine machine =  Machine.buildFromXmlSettings(new XMLHandler(settingsLocation));
        if (update_lock) new Thread(new Updater(machine)).start();
		new BackupJob(machine).runBackup();

		MachineStats machinestats = new MachineStats(machine);
		machinestats.save();
		
	}

}

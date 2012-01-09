import java.util.Arrays;
import java.util.List;

import org.hyperic.sigar.win32.Service;
import org.hyperic.sigar.win32.Win32Exception;

public class ServiceStatus {

    private static void printStatus(String name)
        throws Win32Exception {

        Service service = new Service(name);
        System.out.println(name + ": " +
                           service.getStatusString());
        service.close();
    }

    public static void run()
        throws Exception {

        List services;
        String name;

        services = Service.getServiceNames();

        for (int i=0; i<services.size(); i++) {
        	try {
        	    printStatus((String)services.get(i));      		
        	} catch(Exception e) {
        		e.printStackTrace();
        	}
        
        }
    }
}
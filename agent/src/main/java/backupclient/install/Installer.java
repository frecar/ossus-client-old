package backupclient.install;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

public class Installer {

    private static Map<String, Map<String, String>> inputFileMap = new HashMap<String, Map<String, String>>();
    private static Map<String, String> outFileMap = new HashMap<String, String>();
    static {
        inputFileMap.put("Mac OS X", new HashMap<String, String>());
        inputFileMap.get("Mac OS X").put("x86_64", "libsigar-universal64-macosx.dylib");
        outFileMap.put("Mac OS X", "/usr/lib/java/");

        inputFileMap.put("Linux", new HashMap<String, String>());
        inputFileMap.get("Linux").put("amd64", "libsigar-amd64-linux.so");
        inputFileMap.get("Linux").put("x86", "libsigar-x86-linux.so");
        outFileMap.put("Linux", "/usr/lib/");

    }

    private String inputFileName;
    private String outputFileName;

    public boolean runInstall() throws IOException {
        System.out.println("starting install");
        checkOS();
        copyFile();

        return true;
    }

    private void checkOS() {
        String os = System.getProperty("os.name");
        String arch = System.getProperty("os.arch");

        Map<String,String> archMap = inputFileMap.get(os);
        if (archMap == null) {
            throw new RuntimeException("Unknown os version " + os);
        }
        else {
            inputFileName = archMap.get(arch);
            if (inputFileName == null) {
                throw new RuntimeException("Unknown architecture: " + arch + ", running os: " + os );
            }
            outputFileName = outFileMap.get(os) + inputFileName;
        }

        inputFileName = "/sigarfiles/" + inputFileName ;
    }


    private void copyFile() throws IOException{

        if(!outputFileName.equals("") && !inputFileName.equals(""))  {

            OutputStream out = new FileOutputStream(outputFileName);
            InputStream in = Installer.class.getResource(inputFileName).openStream();
            byte[] buff = new byte[4096];
            int read;
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }

            in.close();
            out.close();

            System.out.println("Sigar file copied to " + outputFileName);

        }

    }
}

package backupclient.agent;

import backupclient.commons.Machine;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;

public class Zipper {

    static void zipDir(String zipFileName, String dir, Machine machine) throws Exception {
        Zipper.createZipFromDirectory(dir, zipFileName, machine, true);
    }

    public static boolean createZipFromDirectory(String directory, String filename, Machine machine, boolean absolute) {
        File rootDir = new File(directory);
        File saveFile = new File(filename);

        ZipArchiveOutputStream zaos;

        try {
            zaos = new ZipArchiveOutputStream(new FileOutputStream(saveFile));
        } catch (FileNotFoundException e) {
            machine.log_error(e.getMessage());
            return false;
        }
        try {
            recurseFiles(rootDir, rootDir, zaos, absolute);
        } catch (IOException e2) {
            machine.log_error(e2.getMessage());

            try {
                zaos.close();
            } catch (IOException e) {
                machine.log_error(e.getMessage());
            }
            return false;
        }
        try {
            zaos.finish();
        } catch (IOException e1) {
            machine.log_error(e1.getMessage());
            return false;
        }
        try {
            zaos.flush();
        } catch (IOException e) {
            machine.log_error(e.getMessage());
            return false;
        }
        try {
            zaos.close();
        } catch (IOException e) {
            machine.log_error(e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * * Recursive traversal to add files
     * *
     * * @param root
     * * @param file
     * * @param zaos
     * * @param absolute
     * * @throws IOException
     */
    private static void recurseFiles(File root, File file, ZipArchiveOutputStream zaos,
                                     boolean absolute) throws IOException {
        if (file.isDirectory()) {
            // recursive call
            File[] files = file.listFiles();
            for (File file2 : files) {
                recurseFiles(root, file2, zaos, absolute);
            }
        } else if ((!file.getName().endsWith(".zip")) && (!file.getName().endsWith(".ZIP"))) {
            String filename = null;
            if (absolute) {
                filename = file.getAbsolutePath().substring(root.getAbsolutePath().length());
            } else {
                filename = file.getName();
            }
            ZipArchiveEntry zae = new ZipArchiveEntry(filename);
            zae.setSize(file.length());
            zaos.putArchiveEntry(zae);
            FileInputStream fis = new FileInputStream(file);
            IOUtils.copy(fis, zaos);
            fis.close();
            zaos.closeArchiveEntry();
        }
    }

}
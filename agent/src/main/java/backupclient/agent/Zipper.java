package backupclient.agent;

import backupclient.commons.Machine;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Zipper {

    Machine machine;

    static void zipDir(String zipFileName, String dir, Machine machine) throws Exception {
        try {
            Zipper.createZipFromDirectory(dir, zipFileName, true);
        } catch (Exception e) {
            JFrame frame = new JFrame();
            JOptionPane.showMessageDialog(frame, e.getMessage() + ". Is settings.xml correct?");
        }
    }

    public static boolean createZipFromDirectory(String directory, String filename, boolean absolute) {
        File rootDir = new File(directory);
        File saveFile = new File(filename);
        // recursive call
        ZipArchiveOutputStream zaos;
        try {
            zaos = new ZipArchiveOutputStream(new FileOutputStream(saveFile));
        } catch (FileNotFoundException e) {
            return false;
        }
        try {
            recurseFiles(rootDir, rootDir, zaos, absolute);
        } catch (IOException e2) {
            try {
                zaos.close();
            } catch (IOException e) {
                // ignore
            }
            return false;
        }
        try {
            zaos.finish();
        } catch (IOException e1) {
            // ignore
        }
        try {
            zaos.flush();
        } catch (IOException e) {
            // ignore
        }
        try {
            zaos.close();
        } catch (IOException e) {
            // ignore
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
            zaos.closeArchiveEntry();
        }
    }

    /**
     * * Create a new Zip from a list of Files (only name of files will be used)
     * *
     * * @param files
     * *            list of files to add
     * * @param filename
     * *            the output filename
     * * @return True if OK
     */


    public static boolean createZipFromFiles(List<File> files, String filename) {
        File saveFile = new File(filename);
        ZipArchiveOutputStream zaos;
        try {
            zaos = new ZipArchiveOutputStream(new FileOutputStream(saveFile));
        } catch (FileNotFoundException e) {
            return false;
        }
        for (File file : files) {

            try {
                addFile(file, zaos);

            } catch (IOException e) {
                try {
                    zaos.close();
                } catch (IOException e1) {
                    // ignore

                }

                return false;

            }
        }
        try {
            zaos.finish();
        } catch (IOException e1) {
            // ignore
        }
        try {
            zaos.flush();
        } catch (IOException e) {
            // ignore
        }
        try {
            zaos.close();
        } catch (IOException e) {
            // ignore
        }
        return true;
    }

    /**
     * * Create a new Zip from an array of Files (only name of files will be used)
     * *
     * * @param files
     * *            array of files to add
     * * @param filename
     * *            the output filename
     * * @return True if OK
     *
     */

    public static boolean createZipFromFiles(File[] files, String filename) {
        File saveFile = new File(filename);
        ZipArchiveOutputStream zaos;
        try {
            zaos = new ZipArchiveOutputStream(new FileOutputStream(saveFile));

        } catch (FileNotFoundException e) {
            return false;
        }
        for (File file : files) {
            try {
                addFile(file, zaos);

            } catch (IOException e) {
                try {
                    zaos.close();

                } catch (IOException e1) {
                    // ignore

                }
                return false;

            }

        }
        try {
            zaos.finish();

        } catch (IOException e1) {
            // ignore

        }
        try {
            zaos.flush();

        } catch (IOException e) {
            // ignore

        }
        try {
            zaos.close();

        } catch (IOException e) {
            // ignore

        }
        return true;

    }


    /**
     * * Recursive traversal to add files
     * *
     * * @param file
     * * @param zaos
     * * @throws IOException
     *
     */

    private static void addFile(File file, ZipArchiveOutputStream zaos) throws IOException {
        String filename = null;
        filename = file.getName();
        ZipArchiveEntry zae = new ZipArchiveEntry(filename);
        zae.setSize(file.length());
        zaos.putArchiveEntry(zae);
        FileInputStream fis = new FileInputStream(file);
        IOUtils.copy(fis, zaos);
        zaos.closeArchiveEntry();

    }

    /**
     * * Extract all files from Tar into the specified directory
     * *
     * * @param tarFile
     * * @param directory
     * * @return the list of extracted filenames
     * * @throws IOException
     *
     */

    public static List<String> unZip(File tarFile, File directory) throws IOException {
        List<String> result = new ArrayList<String>();
        InputStream inputStream = new FileInputStream(tarFile);
        ZipArchiveInputStream in = new ZipArchiveInputStream(inputStream);
        ZipArchiveEntry entry = in.getNextZipEntry();
        while (entry != null) {
            OutputStream out = new FileOutputStream(new File(directory, entry.getName()));
            IOUtils.copy(in, out);
            out.close();
            result.add(entry.getName());
            entry = in.getNextZipEntry();

        }
        in.close();
        return result;

    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("You need to provide 3 arguments:\n" +
                    "   option filedest.tar \"source\"\n" +
                    "   where option=1 means unzip and source is a directory\n" +
                    "   option=2 means zip and source is a directory\n" +
                    "   option=3 means zip and source is a list of files comma separated");
            System.exit(1);
        }
        int option = Integer.parseInt(args[0]);
        String tarfile = args[1];
        String tarsource = args[2];
        String[] tarfiles = null;
        if (option == 3) {
            tarfiles = args[2].split(",");
            File[] files = new File[tarfiles.length];
            for (int i = 0; i < tarfiles.length; i++) {
                files[i] = new File(tarfiles[i]);

            }
            if (createZipFromFiles(files, tarfile)) {
                System.out.println("ZIP OK from multiple files");

            } else {
                System.err.println("ZIP KO from multiple files");

            }

        } else if (option == 2) {
            if (createZipFromDirectory(tarsource, tarfile, false)) {
                System.out.println("ZIP OK from directory");

            } else {
                System.err.println("ZIP KO from directory");
            }
        } else if (option == 1) {
            File tarFile = new File(tarfile);
            File directory = new File(tarsource);
            List<String> result = null;
            try {
                result = unZip(tarFile, directory);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (result == null || result.isEmpty()) {
                System.err.println("UNZIP KO from directory");
            } else {
                for (String string : result) {
                    System.out.println("File: " + string);

                }
            }
        }
    }
}
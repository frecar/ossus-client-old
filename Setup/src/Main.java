import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

class Main extends JFrame implements ActionListener {

    String agent_folder;
    String server_ip;

    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    private boolean download(String filename) {

        String url = "";

        if (filename.equals("Agent.jar")) {
            url = server_ip + "download_current_agent";
        } else {
            url = server_ip + "download_current_updater";
        }

        try {
            URL jar_url = new URL(url);
            readAndSaveJar(jar_url, filename);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }


    private void downloadFromUrl(String urlString, String filename) throws MalformedURLException, IOException {
        BufferedInputStream in = null;
        FileOutputStream fout = null;
        try {
            in = new BufferedInputStream(new URL(urlString).openStream());
            fout = new FileOutputStream(filename);

            byte data[] = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                fout.write(data, 0, count);
            }
        } finally {
            if (in != null)
                in.close();
            if (fout != null)
                fout.close();
        }
    }

    private void copyFile(String outputFileName, String inputFileName) throws IOException{

        System.out.println("Copying " + inputFileName + " to " + outputFileName);

        if(!outputFileName.equals("") && !inputFileName.equals(""))  {

            OutputStream out = new FileOutputStream(outputFileName);
            InputStream in = Main.class.getResource(inputFileName).openStream();
            byte[] buff = new byte[4096];
            int read;
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }

            in.close();
            out.close();

            System.out.println("xml file copied to " + outputFileName);

        }

    }

    private void readAndSaveJar(URL jar_url, String filename) throws IOException {
        if (jar_url == null) throw new IOException("URL argument is null");  // not really an ioexception, fix?

        BufferedOutputStream file_out = create_updater_file(filename);
        BufferedInputStream in = new BufferedInputStream(jar_url.openStream());

        int read, total = 1;
        byte[] buff = new byte[8192];   // todo: whats a good size
        while ((read = in.read(buff)) != -1) {
            total += read;
            file_out.write(buff, 0, read);
        }

        file_out.flush();
        file_out.close();
        in.close();

    }

    private BufferedOutputStream create_updater_file(String filename) throws IOException {

        File jar_file = new File(agent_folder + filename);

        if (jar_file.exists()) {
            new RandomAccessFile(jar_file, "rw").setLength(0);
        } else {
            boolean created = jar_file.createNewFile();
            if (!created) throw new IOException("Could not create file " + jar_file);
        }

        return new BufferedOutputStream(new FileOutputStream(jar_file));
    }

    public String cloneTemplate(String host, String name, String templateID, String username, String password) {

        List<JSONObject> list = new ArrayList<JSONObject>();

        String u = host + "/api/machines/" + templateID + "/clone/" + name;
        StringBuilder result = new StringBuilder();

        try {
            u += "?" + URLEncoder.encode("datetime", "UTF-8") + "=" + URLEncoder.encode(this.getDateTime(), "UTF-8");
            u += "&username=" + username + "&password=" + password;

            URL url = new URL(u);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream content = (InputStream) connection.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(content));
            String line;

            while ((line = in.readLine()) != null) {
                result.append(line);
            }

            Object obj = JSONValue.parse(result.toString());
            JSONObject machine = (JSONObject) ((JSONObject) obj).get("machine");

            return String.valueOf(machine.get("id"));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    public String downloadSettings(String host, String id, String username, String password) {

        List<JSONObject> list = new ArrayList<JSONObject>();

        String u = host + "/api/machines/" + id + "/settings/";

        StringBuilder result = new StringBuilder();

        try {

            u += "?" + URLEncoder.encode("datetime", "UTF-8") + "=" + URLEncoder.encode(this.getDateTime(), "UTF-8");
            u += "&username=" + username + "&password=" + password;

            URL url = new URL(u);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            InputStream content = (InputStream) connection.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(content));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Object obj = JSONValue.parse(result.toString());

        try {
            list.add((JSONObject) obj);
        } catch (Exception e) {
            JSONArray jsonArray = (JSONArray) obj;
            for (Object jsonObject : jsonArray) {
                list.add((JSONObject) jsonObject);
            }
        }

        agent_folder = (String) ((JSONObject) obj).get("agent_folder");
        server_ip = (String) ((JSONObject) obj).get("server_ip");

        String file_separator = System.getProperty("file.separator");


        File folder = new File(agent_folder);
        folder.mkdirs();

        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(agent_folder + file_separator + "settings.json"));
            out.write(result.toString());
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        download("Agent.jar");
        download("Updater.jar");

        try {
            copyFile(agent_folder + "start.bat", "/xml/start.bat");
            copyFile(agent_folder + "start.vbs", "/xml/start.vbs");
            //copyFile(agent_folder + "agent.xml", "/xml/agent.xml");
            //copyFile(agent_folder + "agent.xml", "/xml/agent.xml");
            //copyFile(agent_folder + "updater.xml", "/xml/updater.xml");
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        return "OK";

    }

    JButton SUBMIT;
    JPanel panel;
    JLabel label1, label2, label3, label4, label5, label6, label7;
    final JTextField text1, text2, text3, text4, text5, text6, text7;

    Main() {
        label1 = new JLabel();
        label1.setText("Local admin user:");
        text1 = new JTextField(15);

        label2 = new JLabel();
        label2.setText("Local admin password:");
        text2 = new JPasswordField(15);

        label3 = new JLabel();
        label3.setText("Template ID:");
        text3 = new JTextField(15);

        label4 = new JLabel();
        label4.setText("Server:");
        text4 = new JTextField(15);
        text4.setText("http://focus24.no");

        label5 = new JLabel();
        label5.setText("Focus24 user:");
        text5 = new JTextField(15);

        label6 = new JLabel();
        label6.setText("Focus24 password:");
        text6 = new JPasswordField(15);

        label7 = new JLabel();
        label7.setText("Machine: ");
        text7 = new JTextField(20);


        SUBMIT = new JButton("INSTALL");

        panel = new JPanel(new GridLayout(8, 1));
        panel.add(label7);
        panel.add(text7);
        panel.add(label1);
        panel.add(text1);
        panel.add(label2);
        panel.add(text2);
        panel.add(label3);
        panel.add(text3);
        panel.add(label4);
        panel.add(text4);
        panel.add(label5);
        panel.add(text5);
        panel.add(label6);
        panel.add(text6);
        panel.add(SUBMIT);

        add(panel, BorderLayout.CENTER);
        SUBMIT.addActionListener(this);
        setTitle("INSTALLATION");
    }


    public void actionPerformed(ActionEvent ae) {

        String localAdminUser = text1.getText();
        String localAdminPassword = text2.getText();

        String name = text7.getText();
        String templateID = text3.getText();
        String host = text4.getText();

        String username = text5.getText();
        String password = text6.getText();


        String id = cloneTemplate(host, name, templateID, username, password);

        System.out.println(id);

        System.out.println("C:\\WINDOWS\\system32\\schtasks.exe /create /ru " + localAdminUser + " /rp " + localAdminPassword + " " +
                "/tn \"Focus24\" /tr \"" + agent_folder + "start.vbs \" /sc minute /mo 3");

        downloadSettings(host, id, username, password);

        try {

              /*
            System.out.println("C:\\WINDOWS\\system32\\schtasks.exe /create /ru " + localAdminUser + " /rp " + localAdminPassword + " " +
                    "/tn \"Focus24Agent\" /XML " + agent_folder + "agent.xml");

          Runtime.getRuntime().exec("C:\\WINDOWS\\system32\\schtasks.exe /create /ru " + localAdminUser + " /rp " + localAdminPassword + " " +
                    "/tn \"Focus24Agent\" /XML " + agent_folder + "agent.xml");

            Runtime.getRuntime().exec("C:\\WINDOWS\\system32\\schtasks.exe /create /ru " + localAdminUser + " /rp " + localAdminPassword + " " +
                    "/tn \"Focus24Updater\" /XML " + agent_folder + "updater.xml");
             */

            /*
            Runtime.getRuntime().exec("C:\\WINDOWS\\system32\\schtasks.exe /create /ru " + localAdminUser + " /rp " + localAdminPassword + " " +
                    "/tn \"Focus24\" /XML " + agent_folder + "start.xml");
            */

            Runtime.getRuntime().exec("C:\\WINDOWS\\system32\\schtasks.exe /create /ru " + localAdminUser + " /rp " + localAdminPassword + " " +
                    "/tn \"Focus24\" /tr \"" + agent_folder + "start.vbs \" /sc minute /mo 3");


            System.exit(0);

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }
}

class MainRun {
    public static void main(String arg[]) {
        try {
            Main frame = new Main();

            frame.setTitle("Focus24");
            frame.setSize(300, 150);
            frame.setVisible(true);
            frame.setLocationRelativeTo(null);
            //frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }
}
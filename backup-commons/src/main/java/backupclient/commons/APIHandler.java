package backupclient.commons;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class APIHandler {

    private String base_url;
    private String api_user;
    private String api_token;

    public APIHandler(String url, String api_user, String api_token) {
        this.base_url = url;
        this.api_user = api_user;
        this.api_token = api_token;
    }

    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public void set_api_data(String url_path, Map<String, String> dataList, int attempts) {

        if(attempts>10) {
            return;
        }

        try {
            URL url = new URL(this.base_url + url_path);

            String data = URLEncoder.encode("datetime", "UTF-8") + "=" + URLEncoder.encode(this.getDateTime(), "UTF-8");
            data += "&api_user=" + api_user + "&api_token=" + api_token;

            for (Entry<String, String> pairs : dataList.entrySet()) {
                data += "&" + URLEncoder.encode(pairs.getKey(), "UTF-8") + "=" + URLEncoder.encode(pairs.getValue(), "UTF-8");
            }

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
            wr.write(data);
            wr.flush();

            InputStream content = (InputStream) connection.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(content));

            while (in.readLine() != null) {
            }

        } catch (Exception e) {

            try {
                Thread.sleep(4000);
                set_api_data(url_path, dataList, attempts + 1);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            e.printStackTrace();
        }
    }


    public void set_api_data(String url_path, Map<String, String> dataList) {
        set_api_data(url_path, dataList, 0);
    }

    private String download_data_from_url(String u) {

        StringBuilder result = new StringBuilder();

        try {

            u += "?" + URLEncoder.encode("datetime", "UTF-8") + "=" + URLEncoder.encode(this.getDateTime(), "UTF-8");
            u += "&api_user=" + api_user + "&api_token=" + api_token;

            URL url = new URL(u);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            InputStream content = (InputStream) connection.getInputStream();
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(content));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result.toString();

    }

    public List<JSONObject> get_api_data(String url_path) {

        String url = this.base_url + url_path;
        List<JSONObject> list = new ArrayList<JSONObject>();

        String json_content = this.download_data_from_url(url);

        Object obj = JSONValue.parse(json_content);

        try {
            list.add((JSONObject) obj);
        } catch (Exception e) {
            JSONArray jsonArray = (JSONArray) obj;
            for (Object jsonObject : jsonArray) {
                list.add((JSONObject) jsonObject);
            }
        }
        return list;
    }
}
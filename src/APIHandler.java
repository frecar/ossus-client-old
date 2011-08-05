import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class APIHandler {

    private String base_url;
    private String username;
    private String password;


    public APIHandler(String url) {
        base_url = url;
        username = "fredrik";
        password = "76ahf6234a";
    }

    public void set_api_data() {
        try {
            URL url = new URL("http://www.focustime.no/api/customers/");
            String encoding = Base64Coder.encodeString(this.username + ":" + this.password);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Basic " + encoding);
            InputStream content = (InputStream) connection.getInputStream();
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(content));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String download_data_from_url(String u) {

        String result = "";

        try {
            URL url = new URL(u);
            String encoding = Base64Coder.encodeString(this.username + ":" + this.password);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Basic " + encoding);
            InputStream content = (InputStream) connection.getInputStream();
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(content));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;

    }

    public List<JSONObject> get_api_data(String url_path) {

        String url = this.base_url + url_path;
        List<JSONObject> list = new ArrayList<JSONObject>();

        String json_content = this.download_data_from_url(url);

        Object obj = JSONValue.parse(json_content);

        try {
            JSONObject jsonObject = (JSONObject) obj;
            list.add(jsonObject);
        } catch (Exception e) {
            JSONArray jsonArray = (JSONArray) obj;
            for (Object jsonObject : jsonArray) {
                list.add((JSONObject) jsonObject);
            }
        }
        return list;
    }
}
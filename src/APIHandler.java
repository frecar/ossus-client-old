import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
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

	public void set_api_data(String url_path) {
		try {

			
			URL url = new URL(this.base_url + url_path);
			String encoding = Base64Coder.encodeString(this.username + ":" + this.password);

			String data = URLEncoder.encode("machine_id", "UTF-8") + "=" + URLEncoder.encode("2010", "UTF-8");
			data += "&" + URLEncoder.encode("text", "UTF-8") + "=" + URLEncoder.encode("hei", "UTF-8");
			data += "&" + URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode("info", "UTF-8");
			data += "&" + URLEncoder.encode("datetime", "UTF-8") + "=" + URLEncoder.encode("2011-10-10 12:12:12", "UTF-8");

			
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Authorization", "Basic " + encoding);

			OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
			wr.write(data);
			wr.flush();

			InputStream content = (InputStream) connection.getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(content));
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
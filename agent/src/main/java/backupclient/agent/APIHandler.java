package backupclient.agent;

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class APIHandler {

	private String base_url;
	private String username;
	private String password;


	public APIHandler(String url, String username, String password) {
		this.base_url = url;
		this.username = username;
		this.password = password;
	}

	private String getDateTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}

	public void set_api_data(String url_path, Map<String, String> dataList) {
		try {

			URL url = new URL(this.base_url + url_path);
			String encoding = Base64Coder.encodeString(this.username + ":" + this.password);

			String data = URLEncoder.encode("datetime", "UTF-8") + "=" + URLEncoder.encode(this.getDateTime(), "UTF-8");

			Iterator<Entry<String, String>> it = dataList.entrySet().iterator();

			while(it.hasNext()) {
				Entry<String, String> pairs = it.next();
				data += "&" + URLEncoder.encode(pairs.getKey(), "UTF-8") + "=" + URLEncoder.encode(pairs.getValue(), "UTF-8");				
			}
			
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Authorization", "Basic " + encoding);

			OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
			wr.write(data);
			wr.flush();

			InputStream content = (InputStream) connection.getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(content));
			
			while(in.readLine() != null) {}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String download_data_from_url(String u) {

		StringBuilder result = new StringBuilder();

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
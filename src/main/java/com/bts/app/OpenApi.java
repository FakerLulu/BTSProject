package com.bts.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class OpenApi {
	public static void main(String[] args) throws IOException {

		BufferedReader br = null;

		String urlstr = "http://apis.data.go.kr/1360000/VilageFcstMsgService/"
				+ "getLandFcst?serviceKey=GFUH3QC%2FdPbJrb88KS8hUbbLltKIH01daK9kWkQIKnTAPpvbxZxUlqU64pR1nyKCnFzyZt0R3VjAT9pxl5cUfQ%3D%3D"
				+ "&pageNo=1&numOfRows=10&dataType=JSON&regId=11B20603";

		URL url = new URL(urlstr);
		HttpURLConnection urlconnection = (HttpURLConnection) url.openConnection();
		urlconnection.setRequestMethod("GET");
		br = new BufferedReader(new InputStreamReader(urlconnection.getInputStream(), "UTF-8"));
		String result = "";
		String line;
		while ((line = br.readLine()) != null) {
			result = result + line + "\n";
		}
		
		try { JSONParser jsonParse = new JSONParser(); //JSONParse�� json�����͸� �־� �Ľ��� ���� JSONObject�� ��ȯ�Ѵ�.
		JSONObject responseObj = (JSONObject) jsonParse.parse(result); //JSONObject���� PersonsArray�� get�Ͽ� JSONArray�� �����Ѵ�.
		JSONObject bodyObj = (JSONObject) responseObj.get("response");
		JSONObject bodys = (JSONObject) bodyObj.get("body");
		JSONArray items = (JSONArray) bodys.get("items");
//		JSONArray personArray = (JSONArray) jsonObj.get("response/body");
		for(int i=0; i < items.size(); i++) { 
			JSONObject item = (JSONObject) items.get(i);
			System.out.println(new SimpleDateFormat("yyyy�� MM�� dd�� hh�� mm�� ��ǥ").format(new SimpleDateFormat("yyyyMMddhhmm").parse((String)item.get("announceTime"))));

		}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	

		

//				System.out.println(result + "\n");

	}

}

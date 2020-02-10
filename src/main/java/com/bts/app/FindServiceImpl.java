package com.bts.app;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bts.app.find.ApiConnectService;
import com.bts.app.find.ApiConnectServiceImpl;
import com.bts.app.find.JSONLoader;

@Service
public class FindServiceImpl implements FindService {
	@Autowired
	ApiConnectService apiService;

	public static void main(String[] args) {
		FindServiceImpl fs = new FindServiceImpl();
		;
		System.out.println(fs.findpath(126.9051651846776, 37.51619692388867, 127.04956901223464, 37.485662474272765));
	}

	@Override
	public JSONObject findpath(double sx, double sy, double ex, double ey) {
		String option = "&SX=" + sx + "&SY=" + sy + "&EX=" + ex + "&EY=" + ey;
//		JSONObject map = apiService.GetApiResponseMap("searchPubTransPathR", option);
		ApiConnectService acs = new ApiConnectServiceImpl();
		JSONObject map = acs.GetOdsayApiResponseMap("searchPubTransPathR", option);

		JSONArray pathArray = (JSONArray) ((JSONObject) map.get("result")).get("path");
		for (int i = 0; i < pathArray.length(); i++) { // path ��� ��ȸ
			JSONObject path = pathArray.getJSONObject(i);
			JSONArray subpathArr = (JSONArray) path.get("subPath");
			for (int k = 0; k < subpathArr.length(); k++) {
				JSONObject spath = subpathArr.getJSONObject(k);
				if (spath.getInt("trafficType") == 1) {// subway

					break;
				}
				if (spath.getInt("trafficType") == 2) {// bus
					JSONArray busLanes = (JSONArray) spath.get("lane");
					String StartPoint = spath.getString("startName");
					int startid = spath.getInt("startID");
					String stoption = "&stationID=" + startid;

					JSONObject stationInfo = (acs.GetOdsayApiResponseMap("busStationInfo", stoption))
							.getJSONObject("result");
					;
					int stationCityCode = stationInfo.getInt("stationCityCode");
					String stationID;
					String city = stationInfo.getString("gu");
					String dow = stationInfo.getString("do");
					if (stationCityCode == 1000) { // ����� ������
						stationID = stationInfo.getString("arsID").replace("-", "");
						JSONObject seoulinfo = acs.getSeoulRealTimeBusInfo(stationID).getJSONObject("ServiceResult"); // �����
																														// �ǽð�
																														// ȣ��
						int header = (seoulinfo.getJSONObject("msgHeader")).getInt("headerCd");
						JSONArray buslist = (seoulinfo.getJSONObject("msgBody")).getJSONArray("itemList"); // ������ ���� ���
						for (int p = 0; p < buslist.length(); p++) {
							Object rtnm = buslist.getJSONObject(p).get("rtNm");
							String busNo;
							if (rtnm instanceof String) {
								busNo = (String) rtnm;
							} else {
								busNo = String.valueOf(rtnm);
							}
							for (int m = 0; m < busLanes.length(); m++) {

								if (busNo.equals((busLanes.getJSONObject(m)).getString("busNo"))) {// ã�� ��ο��� Ÿ�� �����϶�
									String arrtime = (buslist.getJSONObject(p)).getString("arrmsg1");
									if (arrtime.equals("�� ����")) {
										busLanes.getJSONObject(m).put("arrmsg1", 0);
									} else if (arrtime.equals("��������")) {
										busLanes.getJSONObject(m).put("arrmsg1", arrtime);
									} else {
										int minute = Integer.parseInt(arrtime.split("��")[0]);
										int sec = Integer.parseInt(arrtime.split("��")[1].split("��")[0]);
										busLanes.getJSONObject(m).put("arrmsg1", minute * 60 + sec);
									}

								}
							}

						}

						System.out.println(buslist.length());
					} else {// ���� ��
						stationID = stationInfo.getString("localStationID");
						if (stationCityCode < 2000 && stationCityCode > 1000) {// ��⵵
							JSONObject seoulinfo = acs.getGGRealTimeBusInfo(stationID).getJSONObject("response");
							int header = (seoulinfo.getJSONObject("msgHeader")).getInt("headerCd");
							JSONArray buslist = (seoulinfo.getJSONObject("msgBody")).getJSONArray("busArrivalList"); // ������
																														// ����
																														// ���

						} else if (stationCityCode == 7000) {// �׻�

						} else {

						}

						stationID = stationInfo.getString("localStationID");
					}
					break;
				}
				System.out.println();
			}

		}

		return map;
	}

	@Override
	public JSONObject readCitycode() {
		JSONObject citycode = new JSONLoader().loadJSONFile("buscitycode.json");
		return citycode;

	}

}

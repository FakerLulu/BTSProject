package com.bts.app;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bts.app.find.ApiConnectService;
import com.bts.app.find.ApiConnectServiceImpl;
import com.bts.app.find.JSONLoader;
import com.bts.app.find.Point;

@Service
public class FindServiceImpl implements FindService {
	@Autowired
	ApiConnectService apiService;

	public static void main(String[] args) {
		FindServiceImpl fs = new FindServiceImpl();

//		System.out.println(fs.findpath(126.9051651846776, 37.51619692388867, 127.04956901223464, 37.485662474272765)); // ����
//		System.out.println(fs.findpath(126.808630, 37.482729, 126.786858, 37.504203)); // ��õ

		// System.out.println(fs.findpath(127.349904, 36.360802, 127.378320,
		// 36.351878));// dj
		// System.out.println(fs.findpath(126.5600341, 33.2558425, 126.5722428,
		// 33.2505057));// jeju
		fs.apiService = new ApiConnectServiceImpl();

		System.out.println(
				fs.findOutpath(126.89793002823849, 37.488232695178674, 127.34262657500408, 36.366392876840045));// jeju
//		System.out.println(fs.findpath(126.87949687399517, 35.16042131688744, 126.8110942045558, 35.1373943689147));// ��������->���ְ���

		// System.out.println(fs.findStationTimetable("�¼�", 1));
	}

	@Override
	public JSONObject findpath(double sx, double sy, double ex, double ey) {
		String option = "&SX=" + sx + "&SY=" + sy + "&EX=" + ex + "&EY=" + ey;
		JSONObject map = apiService.GetOdsayApiResponseMap("searchPubTransPathR", option);

		if (map.has("error")) {
			return map;
		}
		if (map.getJSONObject("result").getInt("searchType") == 1) {
			map.put("error", "out city");
			return map;
		}
		JSONArray pathArray = (JSONArray) (map.getJSONObject("result").get("path"));
		for (int i = 0; i < pathArray.length(); i++) { // path ��� ��ȸ
			JSONObject path = pathArray.getJSONObject(i);
			JSONArray subpathArr = (JSONArray) path.get("subPath");
			int walktime = 0;
			int pay = path.getJSONObject("info").getInt("payment");

			for (int k = 0; k < subpathArr.length(); k++) {
				JSONObject spath = subpathArr.getJSONObject(k);
				if (spath.getInt("trafficType") == 3) { // ù ���������� �ȴ½ð�
					walktime = spath.getInt("sectionTime");
					continue;
				}

				// �źд� : 1077 1~9 : 1001~1009 ��ö : 1065 ���� : 1063 ���� : 1067 ���̽ż� : 1092
				// �д� : 1075 ���� : 1071

				if (spath.getInt("trafficType") == 1) {// subway
					String startName = spath.getString("startName");
					int subcode = spath.getJSONArray("lane").getJSONObject(0).getInt("subwayCode");
					char direction = spath.getInt("wayCode") == 1 ? 'U' : 'D';// 1 = ����
					String stationCode = apiService.getSubwayStatioinCodeOpenApi(startName, subcode);
					int dailyType = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

					switch (dailyType) {
					case 1:
						dailyType = 3;
						break;
					case 7:
						dailyType = 2;
						break;
					default:
						dailyType = 1;
						break;

					}
					long time = System.currentTimeMillis() + ((walktime + 2) * 60000);// ������ �°������ �ð� 2���� ������
					int expectedTime = Integer.parseInt(new SimpleDateFormat("HHmmss").format(time));
					int currentTime = Integer
							.parseInt(new SimpleDateFormat("HHmmss").format(System.currentTimeMillis()));

					JSONObject resultBody = apiService.getSubwayTimeTableOpenApi(stationCode, dailyType, direction);
					JSONArray timetable = resultBody.getJSONObject("items").getJSONArray("item");
					for (int p = 0; p < timetable.length(); p++) {
						int arriveTime = timetable.getJSONObject(p).getInt("arrTime");

						if (expectedTime <= arriveTime) {
							int calcTime = ((arriveTime / 10000) * 3600 + ((arriveTime % 10000) / 100) * 60
									+ ((arriveTime % 10000) % 100))
									- ((currentTime / 10000) * 3600 + ((currentTime % 10000) / 100) * 60
											+ ((currentTime % 10000) % 100));
							spath.getJSONArray("lane").getJSONObject(0).put("arrmsg1", calcTime);
							spath.getJSONArray("lane").getJSONObject(0).put("subwayarrtime", arriveTime);
							break;
						}
					}
					break;
				}
				if (spath.getInt("trafficType") == 2) {// bus
					JSONArray busLanes = (JSONArray) spath.get("lane");
					String StartPoint = spath.getString("startName");
					int startid = spath.getInt("startID");
					String stoption = "&stationID=" + startid;

					JSONObject stationInfo = (apiService.GetOdsayApiResponseMap("busStationInfo", stoption))
							.getJSONObject("result");
					;
					int stationCityCode = stationInfo.getInt("stationCityCode");
					String stationID;
					String city = stationInfo.getString("gu");
					String dow = stationInfo.getString("do");
					if (stationCityCode == 1000) { // ����� ������
						JSONArray buslist = processSeoulBus(walktime, busLanes, stationInfo);
						System.out.println(buslist.length());
					} else {// ���� ��
						stationID = stationInfo.getString("localStationID");

						if (stationCityCode < 2000 && stationCityCode > 1000) {// ��⵵
							JSONObject gginfo = apiService.getGGRealTimeBusInfo(stationID).getJSONObject("response");
							int header = (gginfo.getJSONObject("comMsgHeader")).getInt("returnCode");
							if (gginfo.getJSONObject("msgHeader").getInt("resultCode") != 0) {
								continue;
							}
							JSONArray buslist = processGGBus(walktime, busLanes, gginfo);

						} else if (stationCityCode == 7000) {// �׻�
							JSONObject busaninfo = apiService.getBusanRealTimeBusInfo(stationID)
									.getJSONObject("response");
							int resultCode = (busaninfo.getJSONObject("header")).getInt("resultCode");
							processBusanBus(walktime, busLanes, busaninfo);

						} else {
							String citycode = getCityCode(city, dow);
							if (pay == 0) {
								path.getJSONObject("info").put("payment", getPayment(city, dow));
							}
							JSONObject etcInfo = apiService.getETCRealTimeBusInfo(citycode, stationID)
									.getJSONObject("response");
							int header = (etcInfo.getJSONObject("header")).getInt("resultCode");
							if (header != 0) {
								continue;
							}

							JSONArray buslist = processETCBus(walktime, busLanes, etcInfo);
							System.out.println(buslist);
						}
					}
					break;
				}
			}
		}

		return map;
	}

	private int getPayment(String city, String dow) {
		JSONObject paymentTable = readOutCityJSON("�������");
		if (dow.equals("����Ư����")) {
			return 1200;
		}

		JSONArray extendStatePayment = paymentTable.getJSONArray(dow);
		JSONObject etcpayment = null;
		boolean isFind = false;

		if (dow.equals("��û����")) {
			if (city.contains("��")) {
				return extendStatePayment.getJSONObject(0).getInt("bus");
			}
			if (city.contains("��")) {
				return extendStatePayment.getJSONObject(1).getInt("bus");
			}
		}
		for (int i = 0; i < extendStatePayment.length(); i++) {
			if (extendStatePayment.getJSONObject(i).getString("city").equals(city)) {// �����̸����� �˻�..
				return extendStatePayment.getJSONObject(i).getInt("bus");
			}
			if (extendStatePayment.getJSONObject(i).getString("city").equals("������")) {
				etcpayment = extendStatePayment.getJSONObject(i);
			}
		}

		return etcpayment.getInt("bus");
	}

	private String getCityCode(String city, String dow) {
		String citycode = "0";
		JSONArray cclist = readCitycode().getJSONArray("records");
		if (dow.contains("������") || dow.contains("Ư��")) {
			if (dow.contains("����")) {

				for (int p = 0; p < cclist.length(); p++) {
					if (cclist.getJSONObject(p).getString("cityname").equals("���ֵ�")) {
						citycode = cclist.getJSONObject(p).getString("citycode");
						break;
					}
				}

			} else if (dow.contains("����")) {
				for (int p = 0; p < cclist.length(); p++) {
					if (cclist.getJSONObject(p).getString("cityname").equals("����Ư����")) {
						citycode = cclist.getJSONObject(p).getString("citycode");
						break;
					}
				}
			} else {
				for (int p = 0; p < cclist.length(); p++) {
					if (cclist.getJSONObject(p).getString("cityname").equals(dow)) {
						citycode = cclist.getJSONObject(p).getString("citycode");
						break;
					}
				}
			}

		} else {
			for (int p = 0; p < cclist.length(); p++) {
				if (cclist.getJSONObject(p).getString("cityname").equals(city)) {
					citycode = cclist.getJSONObject(p).getString("citycode");
					break;
				}
			}
		}
		return citycode;
	}

	private JSONArray processETCBus(int walktime, JSONArray busLanes, JSONObject etcInfo) {
		if (etcInfo.getJSONObject("body").get("items") instanceof String) {
			return new JSONArray();
		}
		JSONObject items = etcInfo.getJSONObject("body").getJSONObject("items");
		JSONArray buslist;
		if (items.get("item") instanceof JSONArray) {
			buslist = etcInfo.getJSONObject("body").getJSONObject("items").getJSONArray("item");
		} else {
			buslist = new JSONArray();
			buslist.put(items.get("item"));
		}

		for (int p = 0; p < buslist.length(); p++) {
			String routeno;
			Object rn = buslist.getJSONObject(p).get("routeno");
			if (!(rn instanceof String)) {
				routeno = String.valueOf(rn);
			} else {
				routeno = (String) rn;
			}

			for (int m = 0; m < busLanes.length(); m++) {
				Object busn = busLanes.getJSONObject(m).get("busNo");
				String busbus = "";
				if (busn instanceof String) {
					if (((String) busn).contains("(")) {
						busbus = ((String) busn).split("\\(")[0];
					} else {
						busbus = (String) busn;
					}
				} else {
					busbus = String.valueOf(busn);
				}

				if (routeno.equals(busbus)) {
					Object m1 = (buslist.getJSONObject(p)).get("arrtime");

					int arrtime = 0;
					if (!(m1 instanceof String)) {
						arrtime = (int) m1;
					}

					if (arrtime >= walktime) {
						if (busLanes.getJSONObject(m).isNull("arrmsg1")) {
							busLanes.getJSONObject(m).put("arrmsg1", arrtime);

						} else {
							int tmpval = busLanes.getJSONObject(m).getInt("arrmsg1");

							if (tmpval >= 0 && tmpval > arrtime) {
								busLanes.getJSONObject(m).put("arrmsg1", arrtime);
							}
						}

					} else {

						busLanes.getJSONObject(m).put("arrmsg1", -1);
					}

				}
			}

		}
		return busLanes;
	}

	private void processBusanBus(int walktime, JSONArray busLanes, JSONObject busaninfo) {
		JSONArray buslist = busaninfo.getJSONObject("body").getJSONObject("items").getJSONArray("item");

		for (int p = 0; p < buslist.length(); p++) {
			Object line = buslist.getJSONObject(p).get("lineNo");
			String lineNo = line instanceof String ? (String) line : String.valueOf(line);

			for (int m = 0; m < busLanes.length(); m++) {
				if (lineNo.equals(busLanes.getJSONObject(m).get("busNo"))) {
					try {
						Object m1 = (buslist.getJSONObject(p)).get("min1");
						Object m2 = (buslist.getJSONObject(p)).get("min2");

						int min1 = 0;
						int min2 = 0;
						if (!(m1 instanceof String)) {
							min1 = (int) m1;
						}
						if (!(m2 instanceof String)) {
							min2 = (int) m2;
						}

						if (min1 >= walktime) {
							busLanes.getJSONObject(m).put("arrmsg1", min1 * 60);

						} else {
							if (min2 >= walktime) {
								busLanes.getJSONObject(m).put("arrmsg1", min2 * 60);

							} else {
								busLanes.getJSONObject(m).put("arrmsg1", -1);
							}

						}
					} catch (JSONException e) {
						busLanes.getJSONObject(m).put("arrmsg1", -1);
					}

				}

			}

		}
	}

	private JSONArray processGGBus(int walktime, JSONArray busLanes, JSONObject gginfo) {
		Object obj = gginfo.getJSONObject("msgBody").get("busArrivalList");
		JSONArray buslist = new JSONArray();
		if (obj instanceof JSONArray) {
			buslist = (gginfo.getJSONObject("msgBody")).getJSONArray("busArrivalList"); // bus
																						// list
		} else {
			buslist.put(obj);
		}
		for (int p = 0; p < buslist.length(); p++) {
			int routeID = buslist.getJSONObject(p).getInt("routeId");
			JSONObject busdetail = apiService.getGGBusDetail(String.valueOf(routeID));
			Object busNo = busdetail.getJSONObject("response").getJSONObject("msgBody")
					.getJSONObject("busRouteInfoItem").get("routeName");

			for (int m = 0; m < busLanes.length(); m++) {

				if (busLanes.getJSONObject(m).getString("busNo").contains(String.valueOf(busNo))) {
					Object pt1 = (buslist.getJSONObject(p)).get("predictTime1");
					Object pt2 = (buslist.getJSONObject(p)).get("predictTime2");
					int predictTime1 = 0;
					int predictTime2 = 0;
					if (!(pt1 instanceof String)) {
						predictTime1 = (int) pt1;
					}
					if (!(pt2 instanceof String)) {
						predictTime2 = (int) pt2;
					}

					if (predictTime1 >= walktime) {
						busLanes.getJSONObject(m).put("arrmsg1", predictTime1 * 60);

					} else {
						if (predictTime2 >= walktime) {
							busLanes.getJSONObject(m).put("arrmsg1", predictTime2 * 60);

						} else {
							busLanes.getJSONObject(m).put("arrmsg1", -1);
						}

					}

				}
			}

		}
		return buslist;
	}

	private JSONArray processSeoulBus(int walktime, JSONArray busLanes, JSONObject stationInfo) {
		String stationID;
		stationID = stationInfo.getString("arsID").replace("-", "");
		JSONObject seoulinfo = apiService.getSeoulRealTimeBusInfo(stationID).getJSONObject("ServiceResult"); // �����
		// �ǽð�
		// ȣ��
		int header = (seoulinfo.getJSONObject("msgHeader")).getInt("headerCd");
		Object obj = seoulinfo.getJSONObject("msgBody").get("itemList");
		if (obj instanceof JSONArray) {
			JSONArray buslist = (JSONArray) obj; // ������ ���� ���
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
						String arrtime1 = (buslist.getJSONObject(p)).getString("arrmsg1");
						String arrtime2 = (buslist.getJSONObject(p)).getString("arrmsg2");
						if (arrtime1.equals("�� ����")) {
							if (arrtime2.equals("��ߴ��") || arrtime2.equals("�� ����") || arrtime2.equals("��������")) {
								continue;
							}
							int minute = Integer.parseInt(arrtime2.split("��")[0]);
							String secpart = arrtime2.split("��")[1].split("��")[0];
							int sec = 0;
							if (secpart.contains("��")) {
								sec = Integer.parseInt(secpart.split("��")[0]);
							}
							if (walktime * 60 <= minute * 60 + sec) {// �� ���� ���� �����ð����
								busLanes.getJSONObject(m).put("arrmsg1", minute * 60 + sec);
							} else {
								busLanes.getJSONObject(m).put("arrmsg1", -1);
							}
						} else if (arrtime1.equals("��ߴ��")) {
							busLanes.getJSONObject(m).put("arrmsg1", -1);
						} else if (arrtime1.equals("��������")) {
							busLanes.getJSONObject(m).put("arrmsg1", -1);
						} else {
							int minute = Integer.parseInt(arrtime1.split("��")[0]);
							String secpart = arrtime1.split("��")[1].split("��")[0];
							int sec = 0;
							if (secpart.contains("��")) {
								sec = Integer.parseInt(secpart.split("��")[0]);
							}

							if (walktime * 60 <= minute * 60 + sec) {// �ɾ ���� ���� ���� ������ �����Ҷ�
								busLanes.getJSONObject(m).put("arrmsg1", minute * 60 + sec);
							} else {
								if (arrtime2.equals("��ߴ��") || arrtime2.equals("�� ����") || arrtime2.equals("��������")) {
									continue;
								}
								minute = Integer.parseInt(arrtime2.split("��")[0]);
								secpart = arrtime2.split("��")[1].split("��")[0];
								sec = 0;
								if (secpart.contains("��")) {
									sec = Integer.parseInt(secpart.split("��")[0]);
								}
								if (walktime * 60 <= minute * 60 + sec) {// �� ���� ���� �����ð����
									busLanes.getJSONObject(m).put("arrmsg1", minute * 60 + sec);
								} else {
									busLanes.getJSONObject(m).put("arrmsg1", -1);
								}
							}
						}

					}
				}

			}
			return buslist;
		} else {
			JSONObject buslist = (JSONObject) obj; // ������ ���� ���
			Object rtnm = buslist.get("rtNm");
			String busNo;
			if (rtnm instanceof String) {
				busNo = (String) rtnm;
			} else {
				busNo = String.valueOf(rtnm);
			}
			for (int m = 0; m < busLanes.length(); m++) {

				if (busNo.equals((busLanes.getJSONObject(m)).getString("busNo"))) {// ã�� ��ο��� Ÿ�� �����϶�
					String arrtime1 = buslist.getString("arrmsg1");
					String arrtime2 = buslist.getString("arrmsg2");
					if (arrtime1.equals("�� ����")) {
						if (arrtime2.equals("��ߴ��") || arrtime2.equals("�� ����") || arrtime2.equals("��������")) {
							continue;
						}
						int minute = Integer.parseInt(arrtime2.split("��")[0]);
						int sec = Integer.parseInt(arrtime2.split("��")[1].split("��")[0]);
						if (walktime * 60 <= minute * 60 + sec) {// �� ���� ���� �����ð����
							busLanes.getJSONObject(m).put("arrmsg1", minute * 60 + sec);
						} else {
							busLanes.getJSONObject(m).put("arrmsg1", -1);
						}
					} else if (arrtime1.equals("��ߴ��")) {
						busLanes.getJSONObject(m).put("arrmsg1", -1);
					} else if (arrtime1.equals("��������")) {
						busLanes.getJSONObject(m).put("arrmsg1", -1);
					} else {
						int minute = Integer.parseInt(arrtime1.split("��")[0]);
						String secpart = arrtime1.split("��")[1].split("��")[0];
						int sec = 0;
						if (secpart.contains("��")) {
							sec = Integer.parseInt(secpart.split("��")[0]);
						}

						if (walktime * 60 <= minute * 60 + sec) {// �ɾ ���� ���� ���� ������ �����Ҷ�
							busLanes.getJSONObject(m).put("arrmsg1", minute * 60 + sec);
						} else {
							if (arrtime2.equals("��ߴ��") || arrtime2.equals("�� ����") || arrtime2.equals("��������")) {
								continue;
							}
							minute = Integer.parseInt(arrtime2.split("��")[0]);
							sec = Integer.parseInt(arrtime2.split("��")[1].split("��")[0]);
							if (walktime * 60 <= minute * 60 + sec) {// �� ���� ���� �����ð����
								busLanes.getJSONObject(m).put("arrmsg1", minute * 60 + sec);
							} else {
								busLanes.getJSONObject(m).put("arrmsg1", -1);
							}
						}
					}

				}
			}
			return new JSONArray().put(buslist);
		}

	}

	@Override
	public JSONObject readCitycode() {
		JSONObject citycode = new JSONLoader().loadJSONFile("buscitycode.json");
		return citycode;

	}

	@Override
	public JSONObject findStationTimetable(String stationName, int subcode) {
		String stationCode = apiService.getSubwayStatioinCodeOpenApi(stationName, subcode);
		if (stationCode.equals("�������")) {
			JSONObject result = new JSONObject();
			result.put("resultCode", "fail");
			return result;
		}

		JSONObject weektableU = apiService.getSubwayTimeTableOpenApi(stationCode, 1, 'U');
		if (weektableU.get("items") instanceof String) {
			JSONObject result = new JSONObject();
			result.put("resultCode", "fail");
			return result;
		}

		JSONObject sattableU = apiService.getSubwayTimeTableOpenApi(stationCode, 2, 'U');
		JSONObject suntableU = apiService.getSubwayTimeTableOpenApi(stationCode, 3, 'U');
		JSONObject weektableD = apiService.getSubwayTimeTableOpenApi(stationCode, 1, 'D');
		JSONObject sattableD = apiService.getSubwayTimeTableOpenApi(stationCode, 2, 'D');
		JSONObject suntableD = apiService.getSubwayTimeTableOpenApi(stationCode, 3, 'D');
		JSONObject uptable = new JSONObject();
		uptable.put("weekday", weektableU);
		uptable.put("saturday", sattableU);
		uptable.put("sunday", suntableU);
		JSONObject downtable = new JSONObject();
		downtable.put("weekday", weektableD);
		downtable.put("saturday", sattableD);
		downtable.put("sunday", suntableD);
		JSONObject result = new JSONObject();
		result.put("resultCode", "success");
		result.put("up", uptable);
		result.put("down", downtable);
		return result;
	}

	@Override
	public JSONObject findOutpath(double sx, double sy, double ex, double ey) {
		String option = "&lang=0&SX=" + sx + "&SY=" + sy + "&EX=" + ex + "&EY=" + ey;
		String subOption = "&SearchType=" + 1;
		JSONObject map = apiService.GetOdsayApiResponseMap("searchPubTransPathR", option + subOption);

		if (map.has("error")) {
			return map;
		}
		Object traino = map.getJSONObject("result").getJSONObject("trainRequest").get("OBJ");
		Object expbuso = map.getJSONObject("result").getJSONObject("exBusRequest").get("OBJ");
		Object outbuso = map.getJSONObject("result").getJSONObject("outBusRequest").get("OBJ");

		JSONArray trainarr;
		JSONArray expbusarr;
		JSONArray outbusarr;
		if (traino instanceof JSONObject) {
			trainarr = new JSONArray();
			trainarr.put(traino);
		} else {
			trainarr = (JSONArray) traino;
		}
		if (expbuso instanceof JSONObject) {
			expbusarr = new JSONArray();
			expbusarr.put(expbuso);
		} else {
			expbusarr = (JSONArray) expbuso;
		}
		if (outbuso instanceof JSONObject) {
			outbusarr = new JSONArray();
			outbusarr.put(outbuso);
		} else {
			outbusarr = (JSONArray) outbuso;
		}

		HashSet<Point> trainStart = new HashSet<>();
		HashSet<Point> trainEnd = new HashSet<>();
		HashSet<Point> exStart = new HashSet<>();
		HashSet<Point> exEnd = new HashSet<>();
		HashSet<Point> outStart = new HashSet<>();
		HashSet<Point> outEnd = new HashSet<>();

		for (Object train : trainarr) {
			JSONObject station = (JSONObject) train;
			if (!station.has("startSTN")) {
				continue;
			}
			trainStart.add(new Point(station.getString("startSTN"), station.getDouble("SX"), station.getDouble("SY")));
			trainEnd.add(new Point(station.getString("endSTN"), station.getDouble("EX"), station.getDouble("EY")));
		}

		for (Object exb : expbusarr) {
			JSONObject station = (JSONObject) exb;
			if (!station.has("startSTN")) {
				continue;
			}
			exStart.add(new Point(station.getString("startSTN"), station.getDouble("SX"), station.getDouble("SY")));
			exEnd.add(new Point(station.getString("endSTN"), station.getDouble("EX"), station.getDouble("EY")));
		}

		for (Object outb : outbusarr) {
			JSONObject station = (JSONObject) outb;
			if (!station.has("startSTN")) {
				continue;
			}
			outStart.add(new Point(station.getString("startSTN"), station.getDouble("SX"), station.getDouble("SY")));
			outEnd.add(new Point(station.getString("endSTN"), station.getDouble("EX"), station.getDouble("EY")));
		}

		// inner city find start
		subOption = "&SearchType=" + 0;
		JSONArray trains = new JSONArray();
		JSONArray traine = new JSONArray();
		JSONArray exs = new JSONArray();
		JSONArray exe = new JSONArray();
		JSONArray outs = new JSONArray();
		JSONArray oute = new JSONArray();

		for (Point point : trainStart) {
			System.out.println("ts����");

			JSONObject tsmap = findpath(sx, sy, point.getX(), point.getY());
			if (tsmap.has("error")) {
				continue;
			}
			JSONObject tmp = new JSONObject();
			tmp.put("path", tsmap);
			tmp.put("name", point.getName());
			trains.put(tmp);
		}
		for (Point point : trainEnd) {
			System.out.println("te����");

			JSONObject tsmap = findpath(point.getX(), point.getY(), ex, ey);
			if (tsmap.has("error")) {
				continue;
			}
			JSONObject tmp = new JSONObject();
			tmp.put("path", tsmap);
			tmp.put("name", point.getName());
			traine.put(tmp);
		}
		for (Point point : exStart) {
			System.out.println("exs����");

			JSONObject tsmap = findpath(sx, sy, point.getX(), point.getY());
			if (tsmap.has("error")) {
				continue;
			}
			JSONObject tmp = new JSONObject();
			tmp.put("path", tsmap);
			tmp.put("name", point.getName());
			exs.put(tmp);
		}
		for (Point point : exEnd) {
			System.out.println("exe����");

			JSONObject tsmap = findpath(point.getX(), point.getY(), ex, ey);
			if (tsmap.has("error")) {
				continue;
			}
			JSONObject tmp = new JSONObject();
			tmp.put("path", tsmap);
			tmp.put("name", point.getName());
			exe.put(tmp);
		}
		for (Point point : outStart) {
			System.out.println("outs����");
			JSONObject tsmap = findpath(sx, sy, point.getX(), point.getY());
			if (tsmap.has("error")) {
				continue;
			}
			JSONObject tmp = new JSONObject();
			tmp.put("path", tsmap);
			tmp.put("name", point.getName());
			outs.put(tmp);
		}
		for (Point point : outEnd) {
			System.out.println("oute����");

			JSONObject tsmap = findpath(point.getX(), point.getY(), ex, ey);
			if (tsmap.has("error")) {
				continue;
			}
			JSONObject tmp = new JSONObject();
			tmp.put("path", tsmap);
			tmp.put("name", point.getName());
			oute.put(tmp);
		}
		JSONObject inner = new JSONObject();
		inner.put("ts", trains);
		inner.put("te", traine);
		inner.put("es", exs);
		inner.put("ee", exe);
		inner.put("os", outs);
		inner.put("oe", oute);
		map.put("innerpath", inner);

		return map;
	}

	public JSONObject readOutCityJSON(String filename) {
		JSONObject citycode = new JSONLoader().loadJSONFile(filename + ".json");
		return citycode;

	}

}

package com.bts.app.controller;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.bts.app.MemberService;
import com.bts.app.NaverLoginBO;
import com.github.scribejava.core.model.OAuth2AccessToken;

/**
 * Handles requests for the application home page.
 */
@Controller
public class NaverLoginController {

	@Autowired
	MemberService service;

	/**
	 * Simply selects the home view to render by returning its name.
	 */

	@RequestMapping(value = "/openapi", method = RequestMethod.GET)
	public void oa() {
	}

	@RequestMapping(value = "/test1", method = RequestMethod.GET)
	public void oa1() {
	}

	@RequestMapping(value = "/Overlay", method = RequestMethod.GET)
	public void oa2() {
	}

	@RequestMapping(value = "/naverlogin", method = RequestMethod.GET)
	public void oa3() {
	}

	/*
	 * @RequestMapping(value = "/callback", method = RequestMethod.GET) public void
	 * oa4() { }
	 */
	/* NaverLoginBO */
	private NaverLoginBO naverLoginBO;
	private String apiResult = null;

	@Autowired
	private void setNaverLoginBO(NaverLoginBO naverLoginBO) {
		this.naverLoginBO = naverLoginBO;
	}

	// �α��� ù ȭ�� ��û �޼ҵ�
	@RequestMapping(value = "/logindesign", method = { RequestMethod.GET, RequestMethod.POST })
	public String login(Model model, HttpSession session) {
		/* ���̹����̵�� ���� URL�� �����ϱ� ���Ͽ� naverLoginBOŬ������ getAuthorizationUrl�޼ҵ� ȣ�� */
		String naverAuthUrl = naverLoginBO.getAuthorizationUrl(session);
		// https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=sE***************&
		// redirect_uri=http%3A%2F%2F211.63.89.90%3A8090%2Flogin_project%2Fcallback&state=e68c269c-5ba9-4c31-85da-54c16c658125
		System.out.println("���̹�:" + naverAuthUrl);
		// ���̹�
		model.addAttribute("url", naverAuthUrl);
		return "logindesign";
	}

	// ���̹� �α��� ������ callbackȣ�� �޼ҵ� (�α��� ����� ���ƿ� ����� ������ �̿��ؼ� �߰� �۾��� �� �� �ִ�.)
	@RequestMapping(value = "/callback2", method = { RequestMethod.GET, RequestMethod.POST })
	public String callback(Model model, @RequestParam String code, @RequestParam String state, HttpSession session)
			throws IOException, ParseException {
		if (session.getAttribute("loginID") == null) {// �α��� �� �� ����
			OAuth2AccessToken oauthToken;
			oauthToken = naverLoginBO.getAccessToken(session, code, state);
			// 1. �α��� ����� ������ �о�´�.
			apiResult = naverLoginBO.getUserProfile(oauthToken); // String������ json������
			/**
			 * apiResult json ���� {"resultcode":"00", "message":"success",
			 * "response":{"id":"33666449","nickname":"shinn****","age":"20-29","gender":"M","email":"sh@naver.com","name":"\uc2e0\ubc94\ud638"}}
			 **/
			// 2. String������ apiResult�� json���·� �ٲ�
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(apiResult);
			JSONObject jsonObj = (JSONObject) obj;
			// 3. ������ �Ľ�
			// Top���� �ܰ� _response �Ľ�
			JSONObject response_obj = (JSONObject) jsonObj.get("response");
			String email = "naver_" + ((String) response_obj.get("email"));
			service.membercheck(session, email, (String) response_obj.get("name"));
			model.addAttribute("result", apiResult);
			return "login";
		} else {
			model.addAttribute("result", apiResult);
			return "login2";
		}

	}

	// �α׾ƿ�
	@RequestMapping(value = "/logout", method = { RequestMethod.GET, RequestMethod.POST })
	public String logout(HttpSession session) throws IOException {
		System.out.println("����� logout");
		session.invalidate();
		return "redirect:login";
	}

}
package com.bts.app.controller;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.bts.app.MemberService;
import com.bts.app.MemberVO;
import com.bts.app.NaverLoginBO;

@Controller
public class MemberController {
	@Autowired
	MemberService service;
	@Autowired
	private JavaMailSender mailSender;

	/* NaverLoginBO */
	private NaverLoginBO naverLoginBO;
	private String apiResult = null;

	@Autowired
	private void setNaverLoginBO(NaverLoginBO naverLoginBO) {
		this.naverLoginBO = naverLoginBO;
	}

	/*
	 * @RequestMapping(value = "/insertmember", method = RequestMethod.GET) public
	 * ModelAndView joinMemberService(MemberVO vo) { ModelAndView mav = new
	 * ModelAndView(); return mav; }
	 */
	// �α��� ù ȭ�� ��û �޼ҵ�
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String login(Model model, HttpSession session) {
		if (session.getAttribute("id") == null) {// �α��� �� �� ����

			/* ���̹����̵�� ���� URL�� �����ϱ� ���Ͽ� naverLoginBOŬ������ getAuthorizationUrl�޼ҵ� ȣ�� */
			String naverAuthUrl = naverLoginBO.getAuthorizationUrl(session);
			// https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=sE***************&
			// redirect_uri=http%3A%2F%2F211.63.89.90%3A8090%2Flogin_project%2Fcallback&state=e68c269c-5ba9-4c31-85da-54c16c658125
			System.out.println("���̹�:" + naverAuthUrl);
			// ���̹�
			model.addAttribute("url", naverAuthUrl);
			return "login";
		} else { // ���ǿ� ������ ���Ѿ��

			return "loginsuccess";
		}

	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public ModelAndView login(@RequestParam String id, String pw, HttpSession session) {
		
		ModelAndView mav = new ModelAndView();
		String[] inf = new String[2];
		inf[0] = id;
		inf[1] = pw;
		int result = service.login(inf);
		
		if (result == 1) { // �α��� ����
			// main.jsp�� �̵�
			mav.setViewName("loginsuccess");
			session.setAttribute("id", id);
		} else { // �α��� ����
			mav.setViewName("login");

		}
		return mav;

	}

	@RequestMapping(value = "/insertmember", method = RequestMethod.POST)
	public String joinMemberServiceresult(MemberVO vo) {

		service.joinMember(vo);
		System.out.println(vo);
		return "login";
	}

	// ȸ������â ���̵� �ߺ�üũ
	@RequestMapping(value = "/checkmember", method = RequestMethod.POST)
	@ResponseBody
	public String checkIdService(String id) {

		return "" + service.checkID(id);
	}

	// ��й�ȣ ã��
	@RequestMapping(value = "/checkpw", method = RequestMethod.GET)
	public void checkPwService() {

	}
	@RequestMapping(value = "/loginsuccess", method = RequestMethod.GET)
	public void lginsss() {
	}
	@RequestMapping(value = "/checkpw", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
	public ModelAndView checkPwServiceSuccess(@RequestParam String id, String name, HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();

		String[] list = new String[2];
		list[0] = id;
		list[1] = name;
		List<String> c_mail = service.checkMail(list);
		List<String> c_pw = service.checkPw(list);

		String email = c_mail.get(0);
		String password = c_pw.get(0);

		mav.addObject("mail", c_mail);
		mav.addObject("check", "no_id");

		// ������ ���ڵ��� ���� ����
		String charSet = "UTF-8";
		String fromName = "BTS ���";
		InternetAddress from = new InternetAddress();

		try {
			from = new InternetAddress(new String(fromName.getBytes(charSet), "8859_1") + "<witness0109@gmail.com>");
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");

			messageHelper.setFrom(from); // �����»�� �����ϰų� �ϸ� �����۵��� ����
			messageHelper.setTo(email); // �޴»�� �̸���
			messageHelper.setSubject("��й�ȣ �Դϴ�"); // ���������� ������ �����ϴ�
			messageHelper.setText(password + " �Դϴ�. "); // ���� ����

			mailSender.send(message);
		} catch (Exception e) {
			System.out.println(e);
		}

		mav.setViewName("login");
		return mav;
	}

	/*
	 * @RequestMapping(value="/checkpw" ,method = RequestMethod.POST) public
	 * ModelAndView checkPwServiceSuccess(String id) { ModelAndView mav = new
	 * ModelAndView(); String c_pw = service.checkPw(id);
	 * 
	 * mav.addObject("password",c_pw); mav.addObject("check", "no_id");
	 * mav.setViewName("checkpw"); return mav; }
	 */

	@RequestMapping("/logout")
	public String logout(HttpSession session) {
		service.logout(session);

		return "redirect:/BTS/login";

	}
	
	
	
	@RequestMapping(value="/Mypage", method=RequestMethod.GET)
		public String updatemember() {
			return "Mypage";
		}
		
	
		@RequestMapping(value="/Mypage", method=RequestMethod.POST)
			public String updatemember(String id, MemberVO vo, HttpSession session) {
				service.updatemember(vo);
				session.setAttribute("id", id);
				
				return "redirect:/";
			}
		

	


}

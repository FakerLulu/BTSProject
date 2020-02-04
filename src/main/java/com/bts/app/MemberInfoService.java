package com.bts.app;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberInfoService implements MemberService {
	@Autowired
	MemberDAO dao;

	@Override
	public int joinMember(MemberVO vo) {

		return dao.joinMember(vo);
	}

	@Override
	public int checkID(String id) {
		return dao.checkID(id);
	}
	
	@Override
	public void membercheck(HttpSession session, String email, String name) {
		if (session.getAttribute("loginID") == null) {// �α��� �� �� ����

			String id = email.split("@")[0];

			if (checkID(id) == 0) { // ������ �ȵ� ����
				MemberVO vo = new MemberVO();
				vo.setId(id);

				// response���� email, name �Ľ�
				vo.setEmail(email);
				vo.setName(name);
				int result = joinMember(vo);
				if (result == 1) {// db�۾� ����
					System.out.println("���� ���� -" + id);
				}
			}

			session.setAttribute("loginID", email); // ���� ����
		}
	}

}

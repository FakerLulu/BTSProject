package com.bts.app;

import java.util.List;

public interface SystemService {

	//��� member 
	public List<MemberVO> getAllMem();
	
	//������
	public void deleteMem(String id);
	
}

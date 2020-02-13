package com.bts.app.board;

import java.util.List;

public interface ReplyService {
	
	//��� ����
	public int commentCount();
	
	//��� ���
	public List<ReplyVO> commentList();
	
	//��� �ۼ�
	public int commentInsert(ReplyVO vo);
	
	//��� ����
	public int commentUpdate(ReplyVO vo);
	
	//��� ����
	public int commentDelete(int rno);

}

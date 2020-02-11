<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="EUC-KR">
<title>Insert title here</title>
<style type="text/css">
			li {list-style: none; float: left; padding: 6px;}
		</style>
<script src="https://code.jquery.com/jquery-3.2.1.min.js"></script>
<script>
$(document).ready(function() {

	$('#boardcss_list_add_button_table').on('click',function(){
		location.href="./boardwrite";
	});
	
	$('#pagingDiv a').click(function(e){
		e.preventDefault();
		$('#pageNum').val($(this).attr("href"));
		pagingForm.submit();
		
	});
	
	
});
</script>
</head>

<style type="text/css">

#boardcss_list_add_button_table .add_button { cursor: pointer; border: 1px solid #bebebe; position: absolute; right: 10px; top: 10px; width: 85px; padding: 6px 0 6px; text-align: center; font-weight: bold; }
#boardcss_list_add_button_table .add_button a { color: #ffffff; }
 */
 #write {text-align: right; float: right; }
 
 
/* �� ��� ��ư�� �� ����� ��ġ�� �ʰ� ������� �ƹ��͵� �ƴѰ� */
#boardcss_list_add_button_table .boardcss_list_add_button { width: 100%; overflow: hidden; height: 10px;}

/* boardcss_list ���� ����ϴ� �� ��� ���̺� ũ��*/
.boardcss_list_table { width: 100%; }

/* ȭ�鿡 �������� �� ��� ���̺� */
.list_table { width: 100%; }

/* ȭ�鿡 �������� caption */
.list_table caption { display: none; }

/* list_table ���� ���Ǵ� thead */
.list_table thead th { text-align: center; border-top: 1px solid #e5e5e5; border-bottom: 1px solid #e5e5e5; padding: 8px 0; background: #faf9fa; }

/* list_table ���� ���Ǵ� tbody */
.list_table tbody td { text-align: center;  border-bottom: 1px solid #e5e5e5; padding: 5px 0; }

#pagingDiv{margin: auto;}

</style>

<body>
<div>
<input type="button" value="����ȭ��" onclick="location.href='./BTSMap'">
</div>


<div class="boardcss_list_table">

<table class="list_table">
		<colgroup>
			<col width="15%" />
			<col width="45%" />
			<col width="20%" />
			<col width="20%" />
		</colgroup>
<thead>
	<tr>
		<th>��ȣ</th>
		<th>����</th>
		<th>�ۼ���</th>
		<th>�������</th>
	</tr>
</thead>

<c:forEach items="${boardlist}" var="list">
<tbody>
<tr>
<td>${list.seq }</td>
<td><a href="./boarddetail?seq=${list.seq}"> ${list.title }</a></td>
<td>${list.writer }</td>
<td>${list.time }</td>
</tr>
</tbody>
</c:forEach>
</table>

</div>

<div>
<input type="button" value="�۾���" onclick="location.href='./boardwrite'" id=write>
</div>

<div id="pagingDiv">
			<c:if test="${paging.prev}">
				<a href="${paging.startPage - 1 }">����</a>
			</c:if>
			<c:forEach var="num" begin="${paging.startPage}" end="${paging.endPage }">
				&nbsp;<a href="${num }">${num }</a>&nbsp;
			</c:forEach>
			<c:if test="${paging.next}">
				<a id="next" href="${paging.endPage + 1 }">����</a>
			</c:if>
	</div>
	
	<form id="pagingFrm" name="pagingForm" action="boardlist" method="get">
		<input type="hidden" id="pageNum" name="pageNum" value="${paging.cri.pageNum }">
		<input type="hidden" id="amount" name="amount" value="${paging.cri.amount }">
	</form>
</body>
</html>
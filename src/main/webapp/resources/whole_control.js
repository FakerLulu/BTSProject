

var s_id = "system";
var parent = document.getElementById('parent');
var user_son = document.getElementById('son');

if(id == s_id){
	
	
	
	var list4 = document.createElement("li");
	var node = document.createTextNode("list4");
	list4.appendChild(node);

	parent.removeChild(user_son);
	
	parent.innerHTML +=
		'<li class="sub-menu" id="son"><a href="./manageMember"><i class="fa fa-file">'+
		'</i><span>회원 관리</span><i class="arrow fa fa-angle-right pull-right"></i></a></li>';

	
	
	
	
	
}







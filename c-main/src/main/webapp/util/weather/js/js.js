function ResetDomain()
{	var ss=document.domain;
	var ii=ss.lastIndexOf('.');
	if(ii>0)
	{	if(!isNaN(ss.substr(ii+1)*1))
			return;
		ii=ss.lastIndexOf('.',ii-1);
		if(ii>0)
			document.domain	=ss.substr(ii+1);
	}
}
ResetDomain();

function getHost(){
	return top.zDomain;
}
function getSid(){
	return top.gSID;
}
function getCity(){
	return top.jsFrame.GE.city;
}
function setCity(City){
	top.jsFrame.GE.city = City;
}

function check(form){
	var City = form.area.value;
	if(City == "" || City == "请选择"){
		alert("请选择您要查询的城市！");
		return false;
	}
	return true;
}

function setup(form){
	if( !check(form) ) return;
	form.target = 'submitFrame';
	form.action = 'http://' + getHost() + '/coremail/fcg/ldapapp';
	form.sid.value = getSid();
	form.app_city.value = form.area.value;
	form.submit();
	query(form);
}

function query(form){
	if(check(form)){
		var City = form.area.value;
		setCity(City);
		alert(City);
		location.href = City + '.html';
	}
}

function setting(){
	location.href = 'wet_set.html';
}

function detail(){
	var City = getCity();
	location.href = City + '_detail.html';
}

function tomorrow(){
	var City = getCity();
	location.href = City + '_tomorrow.html';
}

function back(){
	var City = getCity();
	location.href = City + '.html';
}

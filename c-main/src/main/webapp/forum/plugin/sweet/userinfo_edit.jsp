<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.plugin.sweet.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
String userName = ParamUtil.get(request, "userName");
if (userName.equals("")) {
	out.print(StrUtil.Alert_Back("缺少用户名！"));
	return;
}
String boardcode = ParamUtil.get(request, "boardcode");

Leaf curleaf = new Leaf();
curleaf = curleaf.getLeaf(boardcode);

// 取得皮肤路径
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<LINK href="../../<%=skinPath%>/css.css" type=text/css rel=stylesheet>
<title><%=Global.AppName%> - 编辑<%=userName%>个人资料</title>
<style type="text/css">
<!--
.style1 {
	color: #525252;
	font-weight: bold;
}
.style4 {	color: #FF00FF;
	font-weight: bold;
}
.style2 {	color: #FF0000;
	font-weight: bold;
}
.style3 {color: #993399}
-->
</style>
<script>
function getRadioValue(myitem)
{
     var radioboxs = document.all.item(myitem);
     if (radioboxs!=null)
     {
	   //如果只有一个radio
	   if (radioboxs.length==null) {
		if (radioboxs.type=="radio" && radioboxs.checked)
			return radioboxs.value;
		else
			return "";
	   }
	   for (i=0; i<radioboxs.length; i++)
       {
            if (radioboxs[i].type=="radio" && radioboxs[i].checked)
            {
                 return radioboxs[i].value;
            }
       }
     }
	 return "";
}

function setRadioChecked(myitem, val)
{
     var radioboxs = document.all.item(myitem);
     if (radioboxs!=null)
     {
	   //如果只有一个radio
	   if (radioboxs.length==null) {
			if (radioboxs.type=="radio") {
            	if (radioboxs[i].value==val) {
			 	radioboxs[i].checked = true;
				return
			 	}
			}
	   }
	   for (i=0; i<radioboxs.length; i++)
       {
            if (radioboxs[i].type=="radio")
            {
                 if (radioboxs[i].value==val) {
				 	radioboxs[i].checked = true;
					return
				 }
            }
       }
     }
	 return "";
}
</script>
</head>
<body>
<div id="wrapper">
<%@ include file="../../inc/header.jsp"%>
<div id="main">
<%
SweetUserInfoDb suid = new SweetUserInfoDb();
suid = suid.getSweetUserInfoDb(userName);
String user = privilege.getUser(request);
BoardManagerDb bm = new BoardManagerDb();
bm = bm.getBoardManagerDb(boardcode, user);

// 如果用户信息不存在
if (!suid.isLoaded()) {
	// 如果是用户本人查看自己的，则重定向至录入页面
	if (boardcode.equals("")) {
		out.print(SkinUtil.makeErrMsg(request, "对不起，请从正确的入口进入！"));
		return;
	}
	// 如果浏览者是版主，则重定向至录入页面
	if (!bm.isLoaded()) {
		response.sendRedirect("userinfo_add.jsp?boardcode="+StrUtil.UrlEncode(boardcode)+"&userName="+StrUtil.UrlEncode(userName));
		return;
	}
	else {
		out.print(StrUtil.Alert("对不起，该用户的详细信息尚未填写！"));
		return;
	}
}
else {
	if (!bm.isLoaded()) {
		out.print(StrUtil.Alert_Back("对不起，您无此权限！"));
		return;
	}
}

String op = ParamUtil.get(request, "op");
if (op.equals("edit")) {
	SweetUserInfoMgr suim = new SweetUserInfoMgr();
	boolean re = false;
	try {
		re = suim.edit(request);
		if (re) {
			out.print(StrUtil.Alert("编辑成功！"));
		}
		else
			out.print(StrUtil.Alert("编辑失败！"));
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
}
%>
<table width="100%"  border="0" align="center" cellpadding="1" cellspacing="1" bgcolor="#FFD3E7">
  <tr>
    <td height="1288" valign="top" background="skin/default/images/bg.gif"><br>
     <form id=form1 name=form1 action="?op=edit" method=post>
      <table class="tableCommon80" cellSpacing="1" cellPadding="0" width="530" align="center" border="0">
          <thead>
            <tr>
              <td colSpan="3"><strong>编辑<%=userName%>个人资料&nbsp;&nbsp;&nbsp;<a href="#photo">编辑照片</a></strong></td>
            </tr>
		  </thead>
            <tr bgColor="#FBECFA">
              <td width="83" height="30"><div align="right"> 性　　别： </div></td>
              <td width="104" height="30">　
                  <input type="radio" value="男" name="gender">
          男
          <input type="radio" value="女" name="gender">
          女
          <script>
	  setRadioChecked("gender", "<%=suid.getGender()%>");
	  </script>
              </td>
              <td width="339"><%if (bm.isLoaded()) {
	  	String checked = suid.isChecked()?"checked":"";
	  %>
                  <input type="checkbox" name="isChecked" value="true" <%=checked%>>
          审核通&nbsp;&nbsp;会员等级：
          <input type="radio" name="member" value="<%=suid.MEMBER_COMMON%>">
          普通
          <input type="radio" name="member" value="<%=suid.MEMBER_SILVER%>">
          银卡
          <input type="radio" name="member" value="<%=suid.MEMBER_GOLD%>">
          金卡
          <script>
setRadioChecked("member", "<%=suid.getMember()%>");
</script>
          <%}else{
	  	if (suid.isChecked()) {
			out.print("已审核通过 审核人：" + suid.getManager());
		}
		else
			out.print("尚未审核");%>
          <input type=hidden name=member value="<%=suid.getMember()%>">
          <%}%></td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="30"><div align="right"> 年　　龄： </div></td>
              <td height="30" colspan="2">　
                  <input id="age" maxLength="2" size="2" name="age" value="<%=suid.getAge()%>">
          岁 *
          <input type=hidden name="userName" value="<%=userName%>">
          <input type=hidden name="boardcode" value="<%=boardcode%>">
              </td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="30"><div align="right"> 出生年月： </div></td>
              <td height="30" colspan="2">　<font color="#000000">
                <select class="put" id="year" size="1" name="year">
                  <option value="1940" selected>1940</option>
                  <option value="1941">1941</option>
                  <option value="1942">1942</option>
                  <option value="1943">1943</option>
                  <option value="1944">1944</option>
                  <option value="1945">1945</option>
                  <option value="1946">1946</option>
                  <option value="1947">1947</option>
                  <option value="1948">1948</option>
                  <option value="1949">1949</option>
                  <option value="1950">1950</option>
                  <option value="1951">1951</option>
                  <option value="1952">1952</option>
                  <option value="1953">1953</option>
                  <option value="1954">1954</option>
                  <option value="1955">1955</option>
                  <option value="1956">1956</option>
                  <option value="1957">1957</option>
                  <option value="1958">1958</option>
                  <option value="1959">1959</option>
                  <option value="1960">1960</option>
                  <option value="1961">1961</option>
                  <option value="1962">1962</option>
                  <option value="1963">1963</option>
                  <option value="1964">1964</option>
                  <option value="1965">1965</option>
                  <option value="1966">1966</option>
                  <option value="1967">1967</option>
                  <option value="1968">1968</option>
                  <option value="1969">1969</option>
                  <option value="1970">1970</option>
                  <option value="1971">1971</option>
                  <option value="1972">1972</option>
                  <option value="1973">1973</option>
                  <option value="1974">1974</option>
                  <option value="1975">1975</option>
                  <option value="1976">1976</option>
                  <option value="1977">1977</option>
                  <option value="1978">1978</option>
                  <option value="1979">1979</option>
                  <option value="1980">1980</option>
                  <option value="1981">1981</option>
                  <option value="1982">1982</option>
                  <option value="1983">1983</option>
                  <option value="1984">1984</option>
                  <option value="1985">1985</option>
                  <option value="1986">1986</option>
                  <option value="1987">1987</option>
                  <option value="1988">1988</option>
                  <option value="1989">1989</option>
                  <option value="1990">1990</option>
                  <option value="1991">1991</option>
                  <option value="1992">1992</option>
                  <option value="1993">1993</option>
                  <option value="1994">1994</option>
                  <option value="1995">1995</option>
                  <option value="1996">1996</option>
                  <option value="1997">1997</option>
                  <option value="1998">1998</option>
                </select>
          年
          <select class="put" id="month" size="1" name="month">
            <option value="1" selected>1</option>
            <option value="2">2</option>
            <option value="3">3</option>
            <option value="4">4</option>
            <option value="5">5</option>
            <option value="6">6</option>
            <option value="7">7</option>
            <option value="8">8</option>
            <option value="9">9</option>
            <option value="10">10</option>
            <option value="11">11</option>
            <option value="12">12</option>
          </select>
          月
          <select class="put" id="day" size="1" name="day">
            <option value="1" selected>1</option>
            <option value="2">2</option>
            <option value="3">3</option>
            <option value="4">4</option>
            <option value="5">5</option>
            <option value="6">6</option>
            <option value="7">7</option>
            <option value="8">8</option>
            <option value="9">9</option>
            <option value="10">10</option>
            <option value="11">11</option>
            <option value="12">12</option>
            <option value="13">13</option>
            <option value="14">14</option>
            <option value="15">15</option>
            <option value="16">16</option>
            <option value="17">17</option>
            <option value="18">18</option>
            <option value="19">19</option>
            <option value="20">20</option>
            <option value="21">21</option>
            <option value="22">22</option>
            <option value="23">23</option>
            <option value="24">24</option>
            <option value="25">25</option>
            <option value="26">26</option>
            <option value="27">27</option>
            <option value="28">28</option>
            <option value="29">29</option>
            <option value="30">30</option>
            <option value="31">31</option>
          </select>
          日</font>
                  <%
	  java.util.Date bday = suid.getBirthday();
	  if (bday!=null) {
	  %>
                  <script>
	  form1.year.value = "<%=1900+bday.getYear()%>";
	  form1.month.value = "<%=bday.getMonth()+1%>";
	  form1.day.value = "<%=bday.getDay()%>";
	    </script>
                  <%}%>
              </td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="30"><div align="right"> 婚姻状况： </div></td>
              <td height="30" colspan="2">　
                  <select name="marriage" id="marriage">
                    <option value="未婚" selected>未婚</option>
                    <option value="离异有孩">离异有孩</option>
                    <option value="离异无孩">离异无孩</option>
                  </select>
                  <script>
	  form1.marriage.value = "<%=suid.getMarriage()%>";
	    </script>
              </td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="30"><div align="right"> 籍　　贯： </div></td>
              <td height="30" colspan="2">　<font color="#000000">
                <select id="province" size="1" name="province">
                  <option value="" selected>选择省份</option>
                  <option value="北京市">北京市</option>
                  <option value="天津市">天津市</option>
                  <option value="河北省">河北省</option>
                  <option value="山西省">山西省</option>
                  <option value="内蒙古自治区">内蒙古自治区</option>
                  <option value="辽宁省">辽宁省</option>
                  <option value="吉林省">吉林省</option>
                  <option value="黑龙江省">黑龙江省</option>
                  <option value="上海市">上海市</option>
                  <option value="江苏省">江苏省</option>
                  <option value="浙江省">浙江省</option>
                  <option value="安徽省">安徽省</option>
                  <option value="福建省">福建省</option>
                  <option value="台湾省">台湾省</option>
                  <option value="江西省">江西省</option>
                  <option value="山东省">山东省</option>
                  <option value="河南省">河南省</option>
                  <option value="湖北省">湖北省</option>
                  <option value="湖南省">湖南省</option>
                  <option value="广东省">广东省</option>
                  <option value="香港特别行政区">香港特别行政区</option>
                  <option value="澳门特别行政区">澳门特别行政区</option>
                  <option value="广西壮族自治区">广西壮族自治区</option>
                  <option value="海南省">海南省</option>
                  <option value="重庆市">重庆市</option>
                  <option value="四川省">四川省</option>
                  <option value="贵州省">贵州省</option>
                  <option value="云南省">云南省</option>
                  <option value="西藏自治区">西藏自治区</option>
                  <option value="陕西省">陕西省</option>
                  <option value="甘肃省">甘肃省</option>
                  <option value="青海省">青海省</option>
                  <option value="宁夏回族自治区">宁夏回族自治区</option>
                  <option value="新疆维吾尔自治区">新疆维吾尔自治区</option>
                </select>
                </font>
                  <script>
	  form1.province.value = "<%=suid.getProvince()%>";
	    </script>
              </td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="30"><div align="right"> 工 作 地： </div></td>
              <td height="30" colspan="2">　
                  <input name="workAddress" size="20" value="<%=suid.getWorkAddress()%>"></td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="30"><div align="right"> 身　　高： </div></td>
              <td height="30" colspan="2">　
                  <input id="tall" maxLength="3" size="3" name="tall" value="<%=suid.getTall()%>">
          cm *</td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="30"><div align="right"> 学　　历： </div></td>
              <td height="30" colspan="2">　<font color="#000000">
                <select class="put" id="xueli" size="1" name="xueli">
                  <option value="大专" selected>大专</option>
                  <option value="本科">本科</option>
                  <option value="小学">小学</option>
                  <option value="中学">中学</option>
                  <option value="中专">中专</option>
                  <option value="高中">高中</option>
                  <option value="博士">博士</option>
                  <option value="硕士">硕士</option>
                  <option value="其他">其他</option>
                </select>
                </font>
                  <script>
	  form1.xueli.value = "<%=suid.getXueli()%>";
	    </script>
              </td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="30"><div align="right"> 职　　业： </div></td>
              <td height="30" colspan="2">　<font color="#000000">
                <select class="put" id="job" size="1" name="job">
                  <option value="管理" selected>管理</option>
                  <option value="工程">工程</option>
                  <option value="金融财务">金融财务</option>
                  <option value="技术">技术</option>
                  <option value="经济业务">经济业务</option>
                  <option value="法律">法律</option>
                  <option value="保险">保险</option>
                  <option value="教师">教师</option>
                  <option value="科研">科研</option>
                  <option value="设计">设计</option>
                  <option value="学生">学生</option>
                  <option value="行政">行政</option>
                  <option value="文体卫生">文体卫生</option>
                  <option value="服务">服务</option>
                  <option value="军队公安">军队公安</option>
                  <option value="IT">IT</option>
                  <option value="公务员">公务员</option>
                  <option value="其它">其它</option>
                </select>
                </font>
                  <script>
	  form1.job.value = "<%=suid.getJob()%>";
	    </script>
              </td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="30"><div align="right"> 月　　薪： </div></td>
              <td height="30" colspan="2">　<font color="#000000">
                <select class="put" id="salary" size="1" name="salary">
                  <option value="1000以下" selected>1000以下</option>
                  <option value="1000-2000">1000-2000</option>
                  <option value="2000-3000">2000-3000</option>
                  <option value="3000-4000">3000-4000</option>
                  <option value="4000-5000">4000-5000</option>
                  <option value="5000-6000">5000-6000</option>
                  <option value="6000以上">6000以上</option>
                </select>
                </font>
                  <script>
	  form1.salary.value = "<%=suid.getSalary()%>";
	    </script>
              </td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="30"><div align="right"> 通信地址： </div></td>
              <td height="30" colspan="2">　
                  <input id="address" size="35" name="address" value="<%=suid.getAddress()%>"></td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="30"><div align="right"> 邮　　编： </div></td>
              <td height="30" colspan="2">　
                  <input name="postCode" id="postCode" size="6" maxLength="6" value="<%=suid.getPostCode()%>"></td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="30"><div align="right"> 电　　话： </div></td>
              <td height="30" colspan="2">　
                  <input name="tel" id="tel" size="15" value="<%=suid.getTel()%>"></td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="30"><div align="right"> 电子邮件： </div></td>
              <td height="30" colspan="2">　
                  <input name="email" id="email" size="20" value="<%=suid.getEmail()%>"></td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="30"><div align="right"> OICQ： </div></td>
              <td height="30" colspan="2">　
                  <input name="OICQ" id="OICQ" size="15" value="<%=suid.getOICQ()%>"></td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="30"><div align="right"> ICQ ： </div></td>
              <td height="30" colspan="2">　
                  <input name="ICQ" id="ICQ" size="15" value="<%=suid.getICQ()%>"></td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="30"><div align="right"> MSN ： </div></td>
              <td height="30" colspan="2">　
                  <input name="MSN" id="MSN" size="20" value="<%=suid.getMSN()%>"></td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="98"><div align="right"> 自我介绍： </div></td>
              <td height="98" colspan="2">　
                  <textarea name="desc" cols="35" rows="5" id="desc"><%=suid.getDescription()%></textarea></td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="30" colSpan="3"><div align="center"> <font color="#000000"><strong>个人爱好</strong></font> </div></td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="30"><div align="right"> 喜欢的运动： </div></td>
              <td height="30" colspan="2">　
                  <input name="sport" id="sport" size="35" value="<%=suid.getSport()%>"></td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="30"><div align="right"> 喜欢的书籍： </div></td>
              <td height="30" colspan="2">　
                  <input name="book" id="book" size="35" value="<%=suid.getBook()%>"></td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="30"><div align="right"> 喜欢的音乐： </div></td>
              <td height="30" colspan="2">　
                  <input name="music" id="music" size="35" value="<%=suid.getMusic()%>"></td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="30"><div align="right"> 喜欢的名人： </div></td>
              <td height="30" colspan="2">　
                  <input name="celebrity" id="celebrity" size="35" value="<%=suid.getCelebrity()%>"></td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="30"><div align="right"> 其它爱好或特长： </div></td>
              <td height="30" colspan="2">　
                  <input name="hobby" id="hobby" size="35" value="<%=suid.getHobby()%>"></td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="30" colSpan="3"><div align="center"> <strong>交友类型</strong> </div></td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="30"><div align="right"> 交友类型： </div></td>
              <td height="30" colspan="2">　
                  <select class="put" id="frendType" size="1" name="frendType">
                    <option value="都可以" selected>都可以</option>
                    <option value="日常笔友">日常笔友</option>
                    <option value="异性交友">异性交友</option>
                    <option value="热心网友">热心网友</option>
                    <option value="共学共勉">共学共勉</option>
                  </select>
                  <script>
	  form1.frendType.value = "<%=suid.getFrendType()%>";
	    </script>
              </td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="30"><div align="right"> 年　　龄： </div></td>
              <td height="30" colspan="2">　
                  <select class="put" id="frendAge" size="1" name="frendAge">
                    <option value="都可以" selected>都可以</option>
                    <option value="16以下">16以下</option>
                    <option value="17-18">17-18</option>
                    <option value="19-20">19-20</option>
                    <option value="21-23">21-23</option>
                    <option value="24-27">24-27</option>
                    <option value="28-30">28-30</option>
                    <option value="31-35">31-35</option>
                    <option value="36-40">36-40</option>
                    <option value="41以上">41以上</option>
                  </select>
                  <script>
	  form1.frendAge.value = "<%=suid.getFrendAge()%>";
	    </script>
              </td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="30"><div align="right"> 身　　高： </div></td>
              <td height="30" colspan="2">　
                  <select class="put" id="frendTall" size="1" name="frendTall">
                    <option value="都可以" selected>都可以</option>
                    <option value="140以下">140以下</option>
                    <option value="145-150">145-150</option>
                    <option value="150-155">150-155</option>
                    <option value="155-160">155-160</option>
                    <option value="160-165">160-165</option>
                    <option value="165-170">165-170</option>
                    <option value="170-175">170-175</option>
                    <option value="175-180">175-180</option>
                    <option value="180-185">180-185</option>
                    <option value="185-190">185-190</option>
                    <option value="190以上">190以上</option>
                  </select>
                  <script>
	  form1.frendTall.value = "<%=suid.getFrendTall()%>";
	    </script>
              </td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="30"><div align="right"> 婚姻状况： </div></td>
              <td height="30" colspan="2">　
                  <select class="put" id="frendMarriage" size="1" name="frendMarriage">
                    <option value="都可以" selected>都可以</option>
                    <option value="未婚">未婚</option>
                    <option value="离异有孩">离异有孩</option>
                    <option value="离异无孩">离异无孩</option>
                  </select>
                  <script>
	  form1.frendMarriage.value = "<%=suid.getFrendMarriage()%>";
	    </script>
              </td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="30"><div align="right"> 原　　籍： </div></td>
              <td height="30" colspan="2">　<font color="#000000">
                <select id="frendProvince" size="1" name="frendProvince">
                  <option value="都可以" selected>都可以</option>
                  <option value="北京市">北京市</option>
                  <option value="天津市">天津市</option>
                  <option value="河北省">河北省</option>
                  <option value="山西省">山西省</option>
                  <option value="内蒙古自治区">内蒙古自治区</option>
                  <option value="辽宁省">辽宁省</option>
                  <option value="吉林省">吉林省</option>
                  <option value="黑龙江省">黑龙江省</option>
                  <option value="上海市">上海市</option>
                  <option value="江苏省">江苏省</option>
                  <option value="浙江省">浙江省</option>
                  <option value="安徽省">安徽省</option>
                  <option value="福建省">福建省</option>
                  <option value="台湾省">台湾省</option>
                  <option value="江西省">江西省</option>
                  <option value="山东省">山东省</option>
                  <option value="河南省">河南省</option>
                  <option value="湖北省">湖北省</option>
                  <option value="湖南省">湖南省</option>
                  <option value="广东省">广东省</option>
                  <option value="香港特别行政区">香港特别行政区</option>
                  <option value="澳门特别行政区">澳门特别行政区</option>
                  <option value="广西壮族自治区">广西壮族自治区</option>
                  <option value="海南省">海南省</option>
                  <option value="重庆市">重庆市</option>
                  <option value="四川省">四川省</option>
                  <option value="贵州省">贵州省</option>
                  <option value="云南省">云南省</option>
                  <option value="西藏自治区">西藏自治区</option>
                  <option value="陕西省">陕西省</option>
                  <option value="甘肃省">甘肃省</option>
                  <option value="青海省">青海省</option>
                  <option value="宁夏回族自治区">宁夏回族自治区</option>
                  <option value="新疆维吾尔自治区">新疆维吾尔自治区</option>
                </select>
                </font>
                  <script>
	  form1.frendProvince.value = "<%=suid.getFrendProvince()%>";
	    </script>
              </td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="30"><div align="right"> 学　　历： </div></td>
              <td height="30" colspan="2">　
                  <select class="put" id="frendXueli" size="1" name="frendXueli">
                    <option value="都可以" selected>都可以</option>
                    <option value="本科">本科</option>
                    <option value="小学">小学</option>
                    <option value="中学">中学</option>
                    <option value="中专">中专</option>
                    <option value="大专">大专</option>
                    <option value="高中">高中</option>
                    <option value="博士">博士</option>
                    <option value="硕士">硕士</option>
                    <option value="其他">其他</option>
                  </select>
                  <script>
	  form1.frendXueli.value = "<%=suid.getFrendXueli()%>";
	    </script>
              </td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="30"><div align="right"> 月　　薪： </div></td>
              <td height="30" colspan="2">　
                  <select class="put" id="frendSalary" size="1" name="frendSalary">
                    <option value="都可以" selected>都可以</option>
                    <option value="1000以下">1000以下</option>
                    <option value="1000-2000">1000-2000</option>
                    <option value="2000-3000">2000-3000</option>
                    <option value="3000-4000">3000-4000</option>
                    <option value="4000-5000">4000-5000</option>
                    <option value="5000-6000">5000-6000</option>
                    <option value="6000以上">6000以上</option>
                  </select>
                  <script>
	  form1.frendSalary.value = "<%=suid.getFrendSalary()%>";
	    </script>
              </td>
            </tr>
            <tr bgColor="#FBECFA">
              <td height="105"><div align="right"> 其他要求： </div></td>
              <td height="105" colspan="2">　
                  <textarea name="frendRequire" cols="35" rows="5" id="frendRequire"><%=suid.getFrendRequire()%></textarea></td>
            </tr>
            <%
	boolean isManager = bm.isLoaded();
	if (!suid.isChecked() || isManager) {%>
            <tr align="center" bgColor="#FBECFA">
              <td height="32" colspan="3"><input type="submit" name="Submit" value=" 提 交 ">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
          <input type="reset" name="Submit" value=" 重 置 "></td>
            </tr>
            <%}%>
          </tbody>
      </table>
      </form>
      <br>
      <form name="form2" enctype="multipart/form-data" method="post" action="userinfo_editphoto.jsp">
      <table class="tableCommon" width="530"  border="0" align="center" cellpadding="5" cellspacing="1">
          <tr>
            <td height="30" align="center" bgcolor="#FBECFA"><a name="photo"></a>上传照片
                <input type="file" name="file">
                <input type="hidden" name="op" value="photo">
                <input type="hidden" name="boardcode" value="<%=boardcode%>">
                <input type="hidden" name="userName" value="<%=userName%>">
                <input type="submit" name="Submit" value="提交"></td>
          </tr>
          <tr>
            <td height="30" align="center" bgcolor="#FBECFA">注意：如果上传为空，将删除照片</td>
          </tr>
          <tr>
            <td height="30" align="center" bgcolor="#FBECFA"><%
	String photo = StrUtil.getNullString(suid.getPhoto());
	if (!photo.equals("")) {%>
                <img src="<%=request.getContextPath()+"/"+photo%>">
                <%}%>
            </td>
          </tr>
    </table>
    </form>
    <br></td>
  </tr>
</table>
<br>
</div>
<%@ include file="../../inc/footer.jsp"%>
</div>
</body>
</html>

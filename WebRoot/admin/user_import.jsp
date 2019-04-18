<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="java.sql.*"%>
<%@ page import="java.io.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="jxl.*"%>
<%@ page import="jxl.write.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.address.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.kit.util.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.security.SecurityUtil" %>
<%@ page import="com.cloudwebsoft.framework.util.LogUtil" %>
<%@ page import="com.redmoon.oa.db.SequenceManager" %>
<%@ page import="com.redmoon.oa.account.AccountDb" %>
<%@page import="com.redmoon.oa.video.VideoMgr"%>
<%@page import="sun.misc.VM"%>
<%@page import="com.redmoon.oa.video.Config"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%!
	/**
	 * 取得部门全称
	 * 
	 * @param dd
	 * @return
	 */
	public String getFullNameOfDept(String code) {
		DeptDb dd = new DeptDb(code);
		String name = dd.getName();
		while (!dd.getParentCode().equals("-1")
				&& !dd.getParentCode().equals(DeptDb.ROOTCODE)) {
			dd = new DeptDb(dd.getParentCode());
			if (dd != null && !dd.getParentCode().equals("")) {
				name = dd.getName() + "-" + name;
			} else {
				return "";
			}
		}
		return name;
	}

	/**
	 * 取得子部门下一编号值
	 * 
	 * @param code
	 * @return
	 */
	public String getNextChildCode(String code, int type) throws SQLException {
		if (type == 1) {
			String sql = "select max(code) from department where parentcode=" + StrUtil.sqlstr(code);
			JdbcTemplate jt = new JdbcTemplate();
			ResultIterator ri = jt.executeQuery(sql);
			String childCode = "";
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				childCode = rr.getString(1);
			}
			code = code.equals("root") ? "" : code;
			return (childCode == null || childCode.equals("") || !childCode.startsWith("0")) ? (code + "0001") : (code + StrUtil.PadString(String.valueOf(StrUtil.toInt(childCode.substring(childCode.length() - 4), 0) + 1), '0', 4, true));
		} else if (type == 2) {
			return code.substring(0, code.length() - 4) + StrUtil.PadString(String.valueOf(StrUtil.toInt(code.substring(code.length() - 4), 0) + 1), '0', 4, true);
		} else {
			return code + "0001";
		}
	}
	
	/**
	 * 获取全部部门code对应部门全称的hash表,用于根据全称查询code
	 * @return
	 */
	public Object[] getAllFullName() throws SQLException {
		HashMap<String, String> map1 = new HashMap<String, String>();
		HashMap<String, String> map2 = new HashMap<String, String>();
		String sql = "select code from department order by code";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = jt.executeQuery(sql);
		while (ri.hasNext()) {
			ResultRecord rr = (ResultRecord)ri.next();
			String code = rr.getString(1);
			String name = getFullNameOfDept(code);
			String nextChild = getNextChildCode(code, 1);
			map1.put(name, code);
			map2.put(code, nextChild);
		}
		return new Object[]{map1, map2};
	}

	public String getRoleCode(String name) throws SQLException{
		String sql = "select code from user_role where description=" + StrUtil.sqlstr(name);
			
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = jt.executeQuery(sql);
		ResultRecord rr = null;
		String code = null;
		// RoleDb dd = new RoleDb();
		while (ri.hasNext()) {
			rr = (ResultRecord) ri.next();
			code = rr.getString(1);
			return code;
		}
		return null;
	}
	
	// orders从1开始
	public final boolean create(String account,String name, String realName, String pwd, String mobile,String sex,String tel,String email,String address,String IDCard,String birthday, String unitCode, String personNo, String roleName, int orders) {
		String pwdMD5 = "";
		try {
		  pwdMD5 = SecurityUtil.MD5(pwd);
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error(e.getMessage());
		}
		UserDb user = new UserDb();
		boolean re = false;
		int gender = 0;
		String sql = "insert into users (name,realName,pwd,pwdRaw,mobile,id,unit_code,regDate,email,gender,birthday,IDCard,address,phone,person_no) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		if(sex.equals("男"))
			gender = 0;
		else if(sex.equals("女"))
			gender = 1;
		else
			gender = 0;
		
		java.util.Date bir = null;
		if(birthday!=null && !birthday.equals("")){
			birthday.replaceAll("/","-");
			bir = DateUtil.parse(birthday,"yyyy-MM-dd");
		}
		JdbcTemplate jt = new JdbcTemplate();
		try {
		  int id = (int)SequenceManager.nextID(45);
		
		  re = jt.executeUpdate(sql, new Object[] { name, realName, pwdMD5, pwd, mobile, new Integer(id), unitCode, new java.util.Date(),email,gender,bir,IDCard,address,tel, personNo }) == 1;
		  
		  if (re) {
			AccountDb adb = new AccountDb();
			if (account.equals("") || (!(account.equals(""))) && (adb.isExist(account))) {
			 ;
			}else{
				adb.setName(account);
				adb.setUserName(name);
				adb.setUnitCode(unitCode);
				adb.create();
			}
			UserCache userc = new UserCache(user);
			userc.refreshCreate();
			
			user.setGender(gender);
			user.createForRelate(name, realName, pwd, mobile);
			
			FavoriteMgr fm = new FavoriteMgr();
        	fm.initQuickMenu4User(name);
			
			String roleCode = getRoleCode(roleName);
			if (roleCode!=null) {
				String insertSql =
						"insert into user_of_role (userName,roleCode) values (" +
						StrUtil.sqlstr(name) +
						", " + StrUtil.sqlstr(roleCode) +
						")";
				jt.executeUpdate(insertSql);
			}
		  }
		} catch (Exception e) {
		  LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
		return re;
	}
%>
<%
String priv = "admin.user";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String account = "";     //工号
String name = "";   	// 用户名
String pwd = "";    	// 密码
String realName = "";   // 姓名
String sex = "";
String deptName1 = "";
String deptName2 = "";
String deptName3 = "";
String deptName4 = "";
String deptName = "";
String mobile = "";     // 手机号
String tel = "";
String email = "";
String address = "";
String IDCard = "";
String birthday = "";
String personNo = "";
String [][] info = null;

String role = "";
String orders = "";

String unitCode = privilege.getUserUnitCode(request);

String op = ParamUtil.get(request, "op");
	
%>	
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>用户导入</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="../js/hopscotch/css/hopscotch.css" />
<script type="text/javascript" src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/hopscotch/hopscotch.js"></script>
<script type="text/javascript" src="../inc/common.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
</head>
<body>
<%@ include file="user_list_inc_menu_top.jsp"%>
<%
if (op.equals("import")) {
	FileUpMgr fum = new FileUpMgr();
	String excelFile = "";
	try{
		excelFile = fum.uploadExcel(application, request);
		if (excelFile.equals("")) {
			out.print(StrUtil.jAlert_Back("请上传excel文件","提示"));
			return;
		}
	}catch(ErrMsgException e){
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		return;
	}
	FileUpload fu = fum.getFileUpload();

	Workbook wb = Workbook.getWorkbook(new java.io.File(excelFile));
	Sheet sheet = wb.getSheet(0);

	int maxRow = sheet.getRows(); //excel数据行数

	int rowBegin = 2; // StrUtil.toInt(fu.getFieldValue("rowBegin"), 1)+1;       // 开始行数
	int accountCol = 0;    	// 工号所在列
	int nameCol = 1;        // 用户名所在列
	int pwdCol = 2;     	// 密码所在列
	int realNameCol = 3;	// 真实姓名所在列
	int sexCol = 4;         // 性别
	
	int deptCol1 = 5;
	int deptCol2 = 6;
	int deptCol3 = 7;
	int deptCol4 = 8;
	int deptCol = 9;		// 部门
	int roleCol = 10; 
	int orderCol = 11;
	int mobileCol = 12;      // 手机号所在列
	int telCol = 13;			// 联系电话
	int emailCol = 14;		// email
	int addCol = 15;			// 地址
	int IDCol = 16;			// 身份证
	int birCol = 17;		// 出生日期
	int personNoCol = 18;	// 用户编号

	info = new String[maxRow][19];
	int x = 0;
	for(int i=rowBegin; i<maxRow; i++) {
		Cell[] cells = sheet.getRow(i);
		if(cells.length <2 ) {
			System.out.println(getClass() + " " + i + " 行解析长度为" + cells.length + ", " + ((cells.length>0)?cells[0].getContents():""));
			continue;
		}
		Cell cAccount = sheet.getCell(accountCol,i);
		Cell cName = sheet.getCell(nameCol,i);
		Cell cPwd = sheet.getCell(pwdCol,i);
		Cell cRealName = sheet.getCell(realNameCol,i);
		Cell cSex = sheet.getCell(sexCol,i);
		Cell cDept1 = sheet.getCell(deptCol1, i);
		Cell cDept2= sheet.getCell(deptCol2, i);
		Cell cDept3= sheet.getCell(deptCol3, i);
		Cell cDept4= sheet.getCell(deptCol4, i);
		Cell cDept = sheet.getCell(deptCol,i);
		Cell cMobile = sheet.getCell(mobileCol,i);
		Cell cTel = sheet.getCell(telCol,i);
		Cell cEmail = sheet.getCell(emailCol,i);
		Cell cAddress = sheet.getCell(addCol,i);
		Cell cID = sheet.getCell(IDCol,i);
		Cell cBir = sheet.getCell(birCol,i);
		Cell cPersonNo = sheet.getCell(personNoCol, i);

		Cell cRole = sheet.getCell(roleCol, i);
		Cell cOrder = sheet.getCell(orderCol, i);
		
		account = cAccount.getContents();
		name = StrUtil.getNullStr(cName.getContents().trim().replaceAll("　",""));
		pwd = StrUtil.getNullStr(cPwd.getContents());
		realName = StrUtil.getNullStr(cRealName.getContents());
		sex = cSex.getContents();
		deptName = cDept.getContents();
		deptName1 = cDept1.getContents();
		deptName2 = cDept2.getContents();
		deptName3 = cDept3.getContents();
		deptName4 = cDept4.getContents();
		mobile = cMobile.getContents();		
		tel = cTel.getContents();
		email = cEmail.getContents();
		address = cAddress.getContents();
		IDCard = cID.getContents();
		birthday = cBir.getContents();
		personNo = cPersonNo.getContents();
		role = cRole.getContents();
		orders = cOrder.getContents();
		
		if(name.equals("") || pwd.equals("") || realName.equals("")) { // || personNo.equals("")) {
			System.out.println("name=" + name + " pwd=" + pwd + " realName=" + realName);
			break;
			// out.print(StrUtil.Alert_Back("请确认必填项是否填写完整， 登录用户名、密码、姓名、用户编号为必填项！"));
			// out.print(StrUtil.Alert_Back("请确认必填项是否填写完整， 登录用户名、密码、姓名为必填项！"));
			// return;
		}
		info[x][0] = account;
		info[x][1] = name;
		info[x][2] = pwd;
		info[x][3] = realName;
		info[x][4] = sex;
		
		info[x][5] = deptName1;
		info[x][6] = deptName2;
		info[x][7] = deptName3;
		info[x][8] = deptName4;
		info[x][9] = deptName;
		
		info[x][10] = role;
		info[x][11] = orders;
		
		info[x][12] = mobile;
		info[x][13] = tel;
		info[x][14] = email;
		info[x][15] = address;
		info[x][16] = IDCard;
		info[x][17] = birthday;
		info[x][18] = personNo;
		
		x++;
	}
	
	java.io.File file = new java.io.File(excelFile);
	file.delete();		
}
 %>
<script>
$("menu4").className="current";
</script>
<div class="spacerH"></div>
<%
	String flag = ParamUtil.get(request, "flag");//判断是否从引导页面跳转过来的
	String sql = "select name from users where isvalid=1 order by regDate desc";

	UserDb user = new UserDb();
	
	Vector v = user.list(sql);
	Iterator iterator = v.iterator();
%>
<%if (op.equals("import")) {%>
<%
	Object[] object = getAllFullName();
	HashMap<String, String> map1 = (HashMap<String, String>)object[0];        
	HashMap<String, String> map2 = (HashMap<String, String>)object[1];
	boolean isNew = true;
	Vector vNew = new Vector();
	Vector vUpdate = new Vector();
	List<String> successList = null;           //同步成功的用户集
	List<String> failureList = null;           //同步失败的用户集
	int successUser = 0;                       //同步成功数量
	int failureUser = 0;					   //同步失败数量
	StringBuilder failureSB = null;            // 用于记录同步失败的用户名
	if (info!=null) {
		for(int i=0; i<info.length; i++) {
			if(info[i][0]==null)
				continue;
			isNew = true;
			Iterator ir = v.iterator();
			while(ir.hasNext()) {
				user = (UserDb)ir.next();
				String userName = user.getName();
				if(userName.equals(info[i][1])) {
					isNew = false;
					vUpdate.addElement(i);
					break;
				}
			}
			if(isNew) vNew.addElement(i);
		}
		VideoMgr vMgr = new VideoMgr();
		if (vMgr.validate()){                                              //校验通过后，方何同步用户
			List<List<String>> list = vMgr.createUserByArr(info);         //批量同步视频会议用户
			successList = list.get(0);                       //同步成功的用户
			failureList = list.get(1);                       //同步失败的用户
			successUser = successList.size();
			failureUser = failureList.size();
			Iterator failuIterator = failureList.iterator();
			while (failuIterator!=null&&failuIterator.hasNext()){
				  	String userName = (String)failuIterator.next();
				  	if (failureSB==null){
				  		failureSB = new StringBuilder();
				  		failureSB.append(userName);
				  	} else {
				  		failureSB.append("，").append(userName);
				  	}
			}
		}
	}
%>
<div style="text-align:center">已更新用户<%=vUpdate.size()%>名</div>
<table width="80%" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1">
	<thead>
	<tr>
	  <td width="22%">用户名</td>
	  <td width="25%">真实姓名</td>
	  <td width="27%">手机</td>
	  <td width="16%">部门</td>
	  <td width="10">other</td>
	</tr>
	</thead>
<%
	String new_account = "";
	String new_name = "";
	String new_pwd = "";
	String new_realname = "";
	String new_sex = "";
	
	//String new_deptname1="";
	//String new_deptname2="";
	//String new_deptname3 = "";
	//String new_deptname4 = "";
	String new_deptname = "";
	String[] newDeptName = new String[5];
	String new_mobile = "";
	String new_tel = "";
	String new_email = "";
	String new_add = "";
	String new_idcard = "";
	String new_bir = "";
	String new_personno = "";
	String new_role = "";
	String new_orders = "";
	int gender = 0;
	DeptUserDb dud = new DeptUserDb();
	iterator = vUpdate.iterator();
	// 更新时，角色和部门及顺序不会被更新
	while(iterator.hasNext()) {
		int k = ((Integer)iterator.next()).intValue();
		// 部门级别
		int depLevel = 1;
		user = new UserDb();
		new_account = StrUtil.getNullStr(info[k][0]);
		new_name = info[k][1];
		new_pwd = info[k][2];
		new_realname = info[k][3];
		new_sex = StrUtil.getNullStr(info[k][4]);
		
		//new_deptname1 = info[k][5];
		//new_deptname2 = info[k][6];k
		//new_deptname3 = info[k][7];
		//new_deptname4 = info[k][8];
		
		//new_deptname = info[k][9];
		
		for (int i = 0; i < 5; i++) {
			if (info[k][i + 5].equals("")) {
				break;
			}
			newDeptName[i] = info[k][i + 5];
			new_deptname = info[k][i + 5];
			depLevel = i + 1;
		}
		
		new_role = info[k][10];
		new_orders = info[k][11];

		new_mobile = info[k][12];
		new_tel = info[k][13];
		new_email = info[k][14];
		new_add = info[k][15];
		new_idcard = info[k][16];
		new_bir = StrUtil.getNullStr(info[k][17]);
		new_personno = StrUtil.getNullStr(info[k][18]);
		
		if(new_sex.equals("男"))
			gender = 0;
		else if(new_sex.equals("女"))
			gender = 1;
		else
			gender = -1;
			
		java.util.Date bir = null;
		if(!new_bir.equals("")){
			new_bir.replaceAll("/","-");
			bir = DateUtil.parse(new_bir,"yyyy-MM-dd");
		}
		user = user.getUserDb(new_name);
		user.setRealName(new_realname);
		user.setGender(gender);
		user.setMobile(new_mobile);
		user.setPhone(new_tel);
		user.setAddress(new_add);
		user.setIDCard(new_idcard);
		user.setEmail(new_email);
		user.setBirthday(bir);
		user.setPersonNo(new_personno);
		user.save();
%>
	<tr>
		<td><%=new_name%></td>
	    <td><%=new_realname%></td>
	    <td><%=new_mobile%></td>
	    <td><%=new_deptname%></td>

<%
		String deptCode = "";
		String depName = "";
		String parentCode = "root";
		for (int i = 0; i < depLevel; i++) {
			depName += depName.equals("") ? newDeptName[i] : ("-" + newDeptName[i]);
			deptCode = map1.get(depName);
			if (deptCode == null || deptCode.equals("")) {
				deptCode = map2.get(parentCode);
				if (deptCode == null || deptCode.equals("")) {
					deptCode = getNextChildCode(parentCode, 3);
				}
				if (deptCode != null && !deptCode.equals("")) {
					DeptDb d = new DeptDb(parentCode);
					DeptDb childLeaf = new DeptDb();
					childLeaf.setCode(deptCode);
					childLeaf.setName(newDeptName[i]);
					childLeaf.setType(DeptDb.TYPE_DEPT);
					childLeaf.setShow(true);
					d.AddChild(childLeaf);
					map1.put(depName, deptCode);
					map2.put(parentCode, getNextChildCode(deptCode, 2));
				}
			}
			parentCode = deptCode;
		}
		if (deptCode != null && !deptCode.equals("")) {
			dud.delUser(new_name);
			dud.create(deptCode, new_name, StrUtil.toInt(new_orders, 1), "");
		}
		%>
		<td><%=deptCode%></td>
		<%
		/*
		
		AccountDb adb = new AccountDb();
		if ((!(new_account.equals(""))) && (adb.isExist(new_account))) {
		 ;
		}else{
			adb = adb.getAccountDb(new_account);
			System.out.println(new_account+"====="+(adb != null)+"======"+adb.isLoaded());
			if(adb != null && adb.isLoaded()){
				
				adb.setName(new_account);
				adb.save();
			}else{
				adb = new AccountDb();
				adb.setName(new_account);
				adb.setUserName(new_name);
				adb.setUnitCode(unitCode);
				adb.create();	
			}
		}
		*/
		%>
	</tr>
		<%
	}
%>
</table>
<br />

<div style="text-align:center">新增加用户<%=vNew.size()%>名</div>
<table width="80%" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1">
<thead>
	<tr>
	  <td width="17%">用户名</td>
	  <td width="19%">真实姓名</td>
	  <td width="21%">手机</td>
	  <td width="21%">部门</td>
	  <td width="22%">角色</td>
    </tr>
</thead>
<%
	StringBuffer sb = new StringBuffer();

	iterator = vNew.iterator();
	while(iterator.hasNext()) {
		int k = ((Integer)iterator.next()).intValue();
		int depLevel = 1;
		
		new_account = info[k][0];
		new_name = info[k][1];
		new_pwd = info[k][2];
		new_realname = info[k][3];
		new_sex = info[k][4];
		//new_deptname = info[k][5];
		
		for (int i = 0; i < 5; i++) {
			if (info[k][i + 5].equals("")) {
				break;
			}
			newDeptName[i] = info[k][i + 5];
			new_deptname = info[k][i + 5];
			depLevel = i + 1;
		}
		
		new_role = info[k][10];
		new_orders = info[k][11];
		
		new_mobile = info[k][12];
		new_tel = info[k][13];
		new_email = info[k][14];
		new_add = info[k][15];
		new_idcard = info[k][16];
		new_bir = info[k][17];
		new_personno = info[k][18];
%>
	<tr>
		<td><%=new_name%></td>
	    <td><%=new_realname%></td>
	    <td><%=new_mobile%></td>
	    <td><%=new_deptname%></td>
        <td><%=new_role%></td>
	</tr>
<%
		create(new_account,new_name, new_realname, new_pwd, new_mobile, new_sex,new_tel,new_email,new_add,new_idcard,new_bir, unitCode, new_personno, new_role, StrUtil.toInt(new_orders, 1));
		
		String deptCode = "";
		String depName = "";
		String parentCode = "root";
		for (int i = 0; i < depLevel; i++) {
			// 如果未填写一级部门，则直接跳过
			if (newDeptName[0]==null) {
				continue;
			}
			depName += depName.equals("") ? newDeptName[i] : ("-" + newDeptName[i]);
			deptCode = map1.get(depName);
			if (deptCode == null || deptCode.equals("")) {
				deptCode = map2.get(parentCode);
				if (deptCode == null || deptCode.equals("")) {
					deptCode = getNextChildCode(parentCode, 3);
				}
				if (deptCode != null && !deptCode.equals("")) {
					DeptDb d = new DeptDb(parentCode);
					DeptDb childLeaf = new DeptDb();
					childLeaf.setCode(deptCode);
					childLeaf.setName(newDeptName[i]);
					childLeaf.setType(DeptDb.TYPE_DEPT);
					childLeaf.setShow(true);
					try {
						d.AddChild(childLeaf);
					}
					catch (ErrMsgException e) {
						sb.append(e.getMessage() + " ");
					}
					map1.put(depName, deptCode);
					map2.put(parentCode, getNextChildCode(deptCode, 2));
				}
			}
			parentCode = deptCode;
		}
		if (deptCode != null && !deptCode.equals("")) {
			dud.delUser(new_name);
			dud.create(deptCode, new_name, StrUtil.toInt(new_orders, 1), "");
		}
	}
%>
</table>
<!--
<div style="text-align:center">同步视频会议用户，成功<%=successUser%>个，失败<%=failureUser%>个。</div>
-->
<%
	if (vNew.size() != 0 || vUpdate.size() != 0) {
		out.print(StrUtil.jAlert("导入完成！" + sb,"提示"));
	}
}
%>
<br />

<form action="?op=import" method="post" enctype="multipart/form-data" name="form1" id="form1">
<table width="80%" border="0" align="center" cellspacing="0" class="tabStyle_1">
	<thead>
    <tr>
      <td>导入文件</td>
    </tr>
	</thead>
    <tr>
      <td align="left">Excel中的列请按以下顺序排列：工号&nbsp;|&nbsp;用户名&nbsp;|&nbsp;密码&nbsp;|&nbsp;姓名&nbsp;|&nbsp;性别 &nbsp;|&nbsp;一级部门&nbsp;|&nbsp;二级部门&nbsp;|&nbsp;三级部门&nbsp;|&nbsp;四级部门&nbsp;|&nbsp;五级部门&nbsp;|&nbsp;角色名称&nbsp;|&nbsp;排序号&nbsp;|&nbsp;手机&nbsp;|&nbsp;联系电话&nbsp;|&nbsp;邮箱&nbsp;|&nbsp;通信地址&nbsp;|&nbsp;身份证&nbsp;|&nbsp;出生日期&nbsp;|&nbsp;用户编号</td>
    </tr>
    <tr>
      <td align="left">选择文件：
      <input title="选择附件文件" type="file" size="30" name="excel" /> <font color="red">(部门填写时需注意：若存在次级部门，则其父级部门必须填写，如：三级部门不为空，则一级、二级部门均不能为空)</font>     </td>
    </tr>
    <tr>
      <td align="center"><input class="btn" name="submit" type="submit" value="导 入" />
        &nbsp;&nbsp;<input id="btn" class="btn" name="btn" type="button" value="下载模板" onclick="downloadTemplate()" />
        &nbsp;&nbsp;</td>
        <%
        	if("introduction".equals(flag)){
        		%>
        		<script>
	        		jQuery(document).ready(function(){
				    	var tour = {
								id : "hopscotch",
								steps : [ {
									title : "提示",
									content : "此处可以下载模版，请根据模版编写Excel(红色*号为必填项)",
									target : "btn",
									placement : "bottom",
									showNextButton : false
								}]
							};
						hopscotch.startTour(tour);
					});
				</script>
        		<%
        	}
         %>
    </tr>
</table>
</form>
</body>
<script>
	function downloadTemplate(){
		//window.location.href="user_import_template.jsp";
		window.location.href="user_template.xls";
	};
</script>
</html>
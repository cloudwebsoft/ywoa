<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="java.sql.*"%>
<%@ page import="java.io.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="jxl.*"%>
<%@ page import="jxl.write.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.post.*"%>
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
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.redmoon.oa.visual.FormDAO"%>
<%@page import="com.redmoon.oa.flow.FormDb"%>
<%@page import="com.redmoon.forum.security.PassportRemoteUtil"%>
<%@page import="java.util.Date"%>
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
	public String getNextChildCode(String code) throws SQLException {
		String sql = "select MAX(code) from department d where d.parentCode = " + StrUtil.sqlstr(code);
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri;
		String maxCode = "";
		try {
			ri = jt.executeQuery(sql);
			if (ri.size()>0){
				ResultRecord rr = (ResultRecord)ri.next();
				maxCode = rr.getString(1);
			}
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e.getMessage());
		}
		
		int num = 0;
		if (DeptDb.ROOTCODE.equals(code)){
			num = StrUtil.toInt(maxCode)+1;
		} else {
			if ("".equals(maxCode)||maxCode==null){
				maxCode = "0";
			} else {
				maxCode = maxCode.substring(code.length());
			}
			num = StrUtil.toInt(maxCode)+1;
		}
		
		// 过早版本是允许跨部门移动子部门的,可能会导致当前的code已经被使用了,所以要进行检测,by 郝炜
		DeptDb dd = null;
		do {
			if (DeptDb.ROOTCODE.equals(code)){
				maxCode = StrUtil.PadString(String.valueOf(num), '0', 4, true);
			} else {
				maxCode = code + StrUtil.PadString(String.valueOf(num), '0', 4, true);
			}
			num++;
			dd = new DeptDb(maxCode);
		} while (dd != null && dd.isLoaded());
		
		return maxCode;
	}
	
	/**
	 * 获取全部部门code对应部门全称的hash表,用于根据全称查询code
	 * @return
	 */
	public HashMap<String, String> getAllFullName() throws SQLException {
		HashMap<String, String> map = new HashMap<String, String>();
		String sql = "select code from department order by code";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = jt.executeQuery(sql);
		while (ri.hasNext()) {
			ResultRecord rr = (ResultRecord)ri.next();
			String code = rr.getString(1);
			String name = getFullNameOfDept(code);
			map.put(name, code);
		}
		return map;
	}

	public String getRoleCode(String name) {
		String sql = "select code from user_role where description=" + StrUtil.sqlstr(name);
			
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		try  {
			ri = jt.executeQuery(sql);
		}
		catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
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
	public final boolean create(HttpServletRequest request, String name,String realName,String mobile,String genderStr,String person_no,String creator,String deptCode, String[] info) {
		String pwdMD5 = "";
		com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();				  
		String pwd = scfg.getInitPassword(); // 默认123
		try {
		  pwdMD5 = SecurityUtil.MD5(pwd);
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error(e.getMessage());
		}
		
        RoleDb rd = new RoleDb();
        rd = rd.getRoleDb(RoleDb.CODE_MEMBER);
        long diskSpaceAllowed = rd.getDiskQuota();
            		
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		int gender = 0;
		
		String sql = "insert into users (id,name,realName,pwd,pwdRaw,mobile,regDate,gender,unit_code,isPass,person_no,diskSpaceAllowed) values (?,?,?,?,?,?,?,?,?,?,?,?)";
		if(genderStr.equals("男"))
			gender = 0;
		else if(genderStr.equals("女"))
			gender = 1;
		else
			gender = 0;
		
		Privilege privilege = new Privilege();
		String unitCode = privilege.getUserUnitCode(request);
		
		String regDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
		boolean re = false;
		try {
		  int id = (int)SequenceManager.nextID(SequenceManager.OA_USER);
		  re = jt.executeUpdate(sql, new Object[] {id,name,realName,pwdMD5,pwd,mobile,regDate,gender,unitCode,1,person_no,diskSpaceAllowed}) == 1;
		} catch (Exception e) {
		  LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		}
		if (re){
			UserDb userDb = new UserDb();
			UserCache userc = new UserCache(userDb);
			userc.refreshCreate();
			
			userDb.setGender(gender);
			userDb.createForRelate(name, realName, pwd, mobile);
			
			FavoriteMgr fm = new FavoriteMgr();
        	fm.initQuickMenu4User(name);
        	
        	VideoMgr vmgr = new VideoMgr();   
        	userDb = userDb.getUserDb(name);                     
        	if(vmgr.validate()){                             //校验成功后，同步添加视频会议用户
        		String password = userDb.getPwdRaw();
        		String returnString = vmgr.createUser(name,password);
        		vmgr.getResultByParseXML(returnString);
        	}
        	
			String idCard = "";
			String marriaged = "";
			String email = "";
			String birthday = "";
			String QQ = "";
			String phone = "";
			String entryDate = "";
			String shortMobile = "";
			String hobbies = "";
			String address = "";
        	
			idCard = info[9];
			marriaged = info[10];
			birthday = info[11];
			email = info[12];
			QQ = info[13];
			phone = info[14];
			entryDate = info[15];
			shortMobile = info[16];
			hobbies = info[17];
			address = info[18];
		        	
			userDb.setIDCard(idCard);
			int intMarriaged = "已婚 ".equals(marriaged)?1:0;
			userDb.setMarriaged(intMarriaged);
			Date d = DateUtil.parse(birthday, "yyyy-MM-dd");
			userDb.setBirthday(d);
			userDb.setEmail(email);
			userDb.setQQ(QQ);
			userDb.setPhone(phone);
			d = DateUtil.parse(entryDate, "yyyy-MM-dd");
			userDb.setEntryDate(d);
			userDb.setMSN(shortMobile);
			userDb.setHobbies(hobbies);
			userDb.setAddress(address);		
			userDb.save();	
        	
        	// 人员基本信息表存在,则同步至人员基本信息表
        	FormDb fd = new FormDb("personbasic");
        	if (fd != null && fd.isLoaded()) {
        		FormDAO fdao = new FormDAO(fd);
        		fdao.setCreator(creator);
        		fdao.setUnitCode(userDb.getUnitCode());
        		fdao.setFieldValue("user_name", userDb.getName());
        		fdao.setFieldValue("realname", userDb.getRealName());
        		birthday = DateUtil.format(userDb.getBirthday(), "yyyy-MM-dd");
        		fdao.setFieldValue("csrq", birthday);
        		if (birthday != null && !birthday.equals("")) {
        			fdao.setFieldValue("age", String.valueOf(DateUtil.getYear(new Date()) - DateUtil.getYear(userDb.getBirthday())));
        		}
        		fdao.setFieldValue("sex", userDb.getGender() == 0 ? "男" : "女");
        		fdao.setFieldValue("idcard", userDb.getIDCard());
        		fdao.setFieldValue("mobile", userDb.getMobile());
        		fdao.setFieldValue("address", userDb.getAddress());
        		fdao.setFieldValue("dept", deptCode);
        		fdao.create();
        	}
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
String unitCode = privilege.getUserUnitCode(request);

HashMap<String, String> map = getAllFullName();
String [][] info = (String[][])request.getSession().getAttribute("info");
request.getSession().removeAttribute("info");
String name = "";
String realName = "";
String mobile = "";
String gender = "";
String personNO = "";
String dept = "";
String roleName = "";

String accountName = "";
String postName = "";
String idCard = "";
String marriaged = "";
String email = "";
String birthday = "";
String QQ = "";
String phone = "";
String entryDate = "";
String shortMobile = "";
String hobbies = "";
String address = "";

List<List<String>> successList = new ArrayList<List<String>>();
DeptUserDb dud = new DeptUserDb();
UserDb ud = new UserDb();
RoleDb rd = new RoleDb();
PostDb pd = new PostDb();
PostUserMgr pum = new PostUserMgr();
FormDb fd = new FormDb("personbasic");
FormDAO fdao = new FormDAO(fd);
String errMsg = "";        	
String myname = privilege.getUser(request);
//判断工号是否启用
	com.redmoon.oa.Config cf2 = new com.redmoon.oa.Config();
	boolean isUseAccount = cf2.getBooleanProperty("isUseAccount");

if (info!=null){
	for(int i=0;i<info.length;i++){
		List<String> list = new ArrayList<String>();
		name = info[i][0];
		realName = info[i][1];
		accountName = info[i][2];
		
		mobile = info[i][3];
		gender = info[i][4];
		personNO = info[i][5];
		dept = info[i][6];
		roleName = info[i][7];
		
		postName = info[i][8];
		idCard = info[i][9];
		marriaged = info[i][10];
		birthday = info[i][11];
		email = info[i][12];
		QQ = info[i][13];
		phone = info[i][14];
		entryDate = info[i][15];
		shortMobile = info[i][16];
		hobbies = info[i][17];
		address = info[i][18];	
		
		if ("".equals(personNO)){
			personNO = UserDb.getNextPersonNo();
		}
		String deptCode = "";
		String depName = "";
		String parentCode = "root";
		//更改为支持多部门导入
		String depts_[] = dept.split(",");
		//存放多个deptcode
		String deptCodes = "";
		for(int ii = 0; ii<depts_.length; ii ++){
			String newDeptName[] = depts_[ii].split("\\\\");
			int depLevel = newDeptName.length;
			parentCode = "root";
			deptCode = "";
			depName = "";
			for (int j = 0; j < depLevel; j++) {
				depName += depName.equals("") ? newDeptName[j] : ("-" + newDeptName[j]);
				deptCode = map.get(depName);
				if (deptCode == null || deptCode.equals("")) {
					DeptDb d = new DeptDb(parentCode);
					DeptDb childLeaf = new DeptDb();
					deptCode = getNextChildCode(parentCode);
					childLeaf.setCode(deptCode);
					childLeaf.setName(newDeptName[j]);
					childLeaf.setType(DeptDb.TYPE_DEPT);
					childLeaf.setShow(true);
					d.AddChild(childLeaf);
					map.put(depName, deptCode);
				}
				parentCode = deptCode;
			}
			if ("".equals(deptCodes)){
				deptCodes = deptCode;
			}else {
				deptCodes += "," + deptCode;
			}
		}
		//String newDeptName[] = dept.split("\\\\");

		
		boolean flag = false;
		ud = ud.getUserDb(name);
		System.out.println("import:" + name + "-" + realName);
		if (ud==null || !ud.isLoaded()) {
			flag = create(request, name,realName,mobile,gender,personNO,myname,deptCode,info[i]);
			ud = ud.getUserDb(name);
		}
		else {
			// 更新
			ud.setPersonNo(personNO);
			ud.setRealName(realName);
			int intG = 0;
			if(gender.equals("男"))
				intG = 0;
			else if(gender.equals("女"))
				intG = 1;		
			ud.setGender(intG);
			ud.setMobile(mobile);
			
			ud.setIDCard(idCard);
			int intMarriaged = "已婚 ".equals(marriaged)?0:1;
			ud.setMarriaged(intMarriaged);
			Date d = DateUtil.parse(birthday, "yyyy-MM-dd");
			ud.setBirthday(d);
			ud.setEmail(email);
			ud.setQQ(QQ);
			ud.setPhone(phone);
			d = DateUtil.parse(entryDate, "yyyy-MM-dd");
			ud.setEntryDate(d);
			ud.setMSN(shortMobile);
			ud.setHobbies(hobbies);
			ud.setAddress(address);			
						
			flag = ud.save();
			
    		// 人员基本信息表存在,则同步至人员基本信息表
        	if (fd != null && fd.isLoaded()) {
				String sqlPerson = "select id from form_table_personbasic where user_name=" + StrUtil.sqlstr(ud.getName());
        		Iterator ir = fdao.list("personbasic", sqlPerson).iterator();
        		if (ir.hasNext()) {
        			fdao = (FormDAO)ir.next();
	        		// fdao.setCreator(myname);
	        		// fdao.setUnitCode(unitCode);
	        		// fdao.setFieldValue("user_name", ud.getName());
	        		fdao.setFieldValue("realname", ud.getRealName());
	        		birthday = DateUtil.format(ud.getBirthday(), "yyyy-MM-dd");
	        		fdao.setFieldValue("csrq", birthday);
	        		if (birthday != null && !birthday.equals("")) {
	        			fdao.setFieldValue("age", String.valueOf(DateUtil.getYear(new Date()) - DateUtil.getYear(ud.getBirthday())));
	        		}
	        		fdao.setFieldValue("sex", ud.getGender() == 0 ? "男" : "女");
	        		fdao.setFieldValue("idcard", ud.getIDCard());
	        		fdao.setFieldValue("mobile", ud.getMobile());
	        		fdao.setFieldValue("address", ud.getAddress());
	        		fdao.setFieldValue("dept", deptCode);
	        		fdao.save();        			
        		}
        	}			
		}
		//处理多部门情况
		if (deptCodes != null && !"".equals(deptCodes)){
		    String temp[] = deptCodes.split(",");
			dud.delUser(name);
			for (String code: temp) {
				dud.create(code,name,"");
			}
		}
		/*if (deptCode != null && !deptCode.equals("")) {
			dud.delUser(name);
			dud.create(deptCode, name, "");
		}*/
		
		roleName = StrUtil.getNullStr(roleName);
		if (!roleName.startsWith("-")) {
			roleName = roleName.replaceAll("，", ",");
			String[] rNames = StrUtil.split(roleName, ",");
			String[] rCodes = null;
			if (rNames!=null) {
				rCodes = new String[rNames.length];
				for (int n=0; n<rNames.length; n++) {
			        String roleCode = getRoleCode(rNames[n]);
			        if (roleCode==null) {
			        	// 如果角色不存在，则创建
			        	roleCode = RandomSecquenceCreator.getId(20);
			    		rd.create(roleCode, rNames[n], 0, -1, privilege.getUserUnitCode(request), -1, false,false);
			        }
			        rCodes[n] = roleCode;
				}
			}
			ud.setRoles(rCodes);
		}
		if (isUseAccount) {
			// 工号处理
			AccountDb adb = new AccountDb();
			adb = adb.getUserAccount(name);
			if (adb != null) {
				if (adb.getName().equals(accountName)) {
					;
				} else {
					adb.clearAccountUserName(name);
					adb.setName(accountName);
					adb.setUserName(name);
					adb.setUnitCode(unitCode);
					try {
						adb.create();
					} catch (Exception e) {
						errMsg += name + "的工号" + accountName + "可能已被占用\n";
					}
				}
			} else {
				adb = new AccountDb();
				adb.setName(accountName);
				adb.setUserName(name);
				adb.setUnitCode(unitCode);
				try {
					adb.create();
				} catch (Exception e) {
					errMsg += name + "的工号" + accountName + "可能已被占用\n";
				}
			}
		}
		// 岗位处理
		postName = StrUtil.getNullStr(postName);
		if (!"-".equals(postName)) {
			int postId = pd.getIdByName(postName, unitCode);
			if (postId==-1) {
				// 如果岗位不存在则创建
				pd.create(new JdbcTemplate(), new Object[]{postName, unitCode, postName, 1});
 				postId = pd.getIdByName(postName, unitCode);				
			}
			
			// 
			PostUserDb pud = new PostUserDb();
			pud = pud.getPostUserDb(name);
			if (pud!=null) {
				// 如果原来已存在且一致，则无需再创建
				if (pud.getInt("post_id")!=postId) {
					pum.createSingle(name, postId, 0);
				}
			}
			else {
				pum.createSingle(name, postId, 0);
			}
		}

		if (flag){
			list.add(name);
			list.add(realName);
			list.add(mobile);
			list.add(gender);
			list.add(personNO);
			list.add(dept);
			list.add(roleName);
			
			list.add(postName);
			list.add(idCard);
			list.add(marriaged);
			list.add(birthday);
			list.add(email);
			list.add(QQ);
			list.add(phone);
			list.add(entryDate);
			list.add(shortMobile);
			list.add(hobbies);
			list.add(address);
			list.add(accountName);
				
			successList.add(list);
		}
		
	}
}
%>	
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>用户导入</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath() %>/js/hopscotch/css/hopscotch.css" />
<script type="text/javascript" src="<%=request.getContextPath() %>/js/jquery.js"></script>
<script type="text/javascript" src="<%=request.getContextPath() %>/js/hopscotch/hopscotch.js"></script>
<script type="text/javascript" src="<%=request.getContextPath() %>/inc/common.js"></script>
<script src="<%=request.getContextPath() %>/js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="<%=request.getContextPath() %>/js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="<%=request.getContextPath() %>/js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/skin/common/organize.css" />
</head>
<body>
<div class="oranize-number">
<!--最底层灰色条--><div class="oranize-number-linegray"></div>
<!--蓝色条1--><div class="oranize-number-lineblue1" style="display:inline;"></div>
<!--灰色条2--><div class="oranize-number-lineblue1 oranize-number-lineblue2"></div>
<!--1步--><div class="oranize-blue1">1</div>
<!--2步--><div class="oranize-blue2 oranize-roundness-blue2">2</div>
<!--3步--><div class="oranize-blue3 oranize-roundness-blue3">3</div>
<!--1步文字--><div class="oranize-txt1 ">导入Excel</div>
<!--2步文字--><div class="oranize-txt2 ">确认信息</div>
<!--3步文字--><div class="oranize-txt3 oranize-txt-sel">完成</div>
</div>
<table border="0" align="center" cellspacing="0" class="tabStyle_1" style="width:2000px;table-layout:fixed;">
	<thead>
		<tr>
			<th class="tabStyle_1_title" width="10%">帐号</th>
			<th class="tabStyle_1_title" width="10%">姓名</th>
			<th class="tabStyle_1_title" width="10%">工号</th>			
			<th class="tabStyle_1_title" width="10%">手机</th>
			<th class="tabStyle_1_title" width="10%">性别</th>
			<th class="tabStyle_1_title" width="10%">员工编号</th>
			<th class="tabStyle_1_title" width="10%">部门</th>
			<th class="tabStyle_1_title" width="10%">角色</th>
			
			<th class="tabStyle_1_title" width="10%">岗位</th>
			<th class="tabStyle_1_title" width="10%">身份证</th>
			<th class="tabStyle_1_title" width="10%">婚否</th>
			<th class="tabStyle_1_title" width="10%">出生日期 </th>
			<th class="tabStyle_1_title" width="10%">E-mail</th>
			<th class="tabStyle_1_title" width="10%">QQ</th>
			<th class="tabStyle_1_title" width="10%">电话</th>
			<th class="tabStyle_1_title" width="10%">入职时间</th>			
			<th class="tabStyle_1_title" width="10%">短号</th>
			<th class="tabStyle_1_title" width="10%">兴趣爱好</th>
			<th class="tabStyle_1_title" width="20%">地址</th>
						
			<th class="tabStyle_1_title" width="10%">提示</th>
		</tr>
	</thead>
	<tbody>
		<%
			for(int i=0;i<successList.size();i++){
				List<String> list = successList.get(i);
				name = list.get(0).trim();
				realName = list.get(1);
				mobile = list.get(2);
				gender = list.get(3);
				personNO = list.get(4);
				dept = list.get(5);
				roleName = list.get(6);
				
				postName = list.get(7);
				idCard = list.get(8);
				marriaged = list.get(9);
				birthday = list.get(10);
				email = list.get(11);
				QQ = list.get(12);
				phone = list.get(13);
				entryDate = list.get(14);
				shortMobile = list.get(15);
				hobbies = list.get(16);
				address = list.get(17);		
				
				accountName = list.get(18);			
		%>
		<tr>
			<td><%=name %></td>
			<td><%=realName %></td>
			<td><%=accountName %></td>			
			<td><%=mobile %></td>
			<td style="text-align:center"><%=gender %></td>
			<td><%=personNO %></td>
			<td><%=dept %></td>
			<td><%=roleName %></td>
			<td><%=postName %></td>
			<td><%=idCard %></td>
			<td style="text-align:center"><%=marriaged %></td>
			<td><%=birthday %></td>
			<td><%=email %></td>
			<td><%=QQ %></td>
			<td><%=phone %></td>
			<td><%=entryDate %></td>
			<td><%=shortMobile %></td>
			<td><%=hobbies %></td>
			<td><%=address %></td>			
			<td align="center"><img width="16px" src="<%=request.getContextPath()%>/skin/images/organize/icon-finish.png"/></td>
		</tr>
		<%
		}
		%>
	</tbody>
</table>
<%
com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();				  
String pwd = scfg.getInitPassword();
%>
<div style="margin-left:5%;">系统支持帐号和手机号登陆，初始密码（默认：<%=pwd%>），首次登陆系统将提醒修改密码。</div>
<table align="center">
	<tr>
	<td>
	<%
	if (!"".equals(errMsg)) {
		out.print(SkinUtil.makeErrMsg(request, errMsg) + "<BR />");
	}
	%>	
	<input type="button"  value="完成" onclick="finish()" class="org-btn" />
	</td></tr>
</table>
</body>
<script type="text/javascript">
	parent.hiddenLoading();
	function finish(){
		parent.page_refresh();
		// window.location.href="<%=request.getContextPath()%>/admin/organize/organize.jsp?type=list";
	}
</script>
</html>
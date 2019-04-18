<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="java.sql.*" %>
<%@ page import="java.io.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="jxl.*" %>
<%@ page import="jxl.write.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.address.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.kit.util.*" %>
<%@ page import="com.cloudwebsoft.framework.db.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.security.SecurityUtil" %>
<%@ page import="com.cloudwebsoft.framework.util.LogUtil" %>
<%@ page import="com.redmoon.oa.db.SequenceManager" %>
<%@ page import="com.redmoon.oa.account.AccountDb" %>
<%@page import="com.redmoon.oa.video.VideoMgr" %>
<%@page import="sun.misc.VM" %>
<%@page import="com.redmoon.oa.video.Config" %>
<%@page import="java.text.SimpleDateFormat" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%!
    /**
     * 取得部门全称
     *
     * @param code
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
                ResultRecord rr = (ResultRecord) ri.next();
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
            ResultRecord rr = (ResultRecord) ri.next();
            String code = rr.getString(1);
            String name = getFullNameOfDept(code);
            String nextChild = getNextChildCode(code, 1);
            map1.put(name, code);
            map2.put(code, nextChild);
        }
        return new Object[]{map1, map2};
    }

    public String getRoleCode(String name) throws SQLException {
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
    public final boolean create(String name, String realName, String mobile, String genderStr, String person_no) {
        String pwdMD5 = "";
        String pwd = "123";
        try {
            pwdMD5 = SecurityUtil.MD5(pwd);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri = null;
        int gender = 0;
        String sql = "insert into users (id,name,realName,pwd,pwdRaw,mobile,regDate,gender,unit_code,isPass,person_no,diskSpaceAllowed) values (?,?,?,?,?,?,?,?,?,?,?,?)";
        if (genderStr.equals("男"))
            gender = 0;
        else if (genderStr.equals("女"))
            gender = 1;
        else
            gender = 0;

        String regDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        boolean re = false;
        try {
            int id = (int) SequenceManager.nextID(SequenceManager.OA_USER);
            re = jt.executeUpdate(sql, new Object[]{id, name, realName, pwdMD5, pwd, mobile, regDate, gender, "root", 1, person_no, "314572800"}) == 1;
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
        if (re) {
            UserDb user = new UserDb();
            UserCache userc = new UserCache(user);
            userc.refreshCreate();

            user.setGender(gender);
            user.createForRelate(name, realName, pwd, mobile);

            FavoriteMgr fm = new FavoriteMgr();
            fm.initQuickMenu4User(name);
        }
        return re;
    }
%>
<%
    String priv = "admin.user";
    if (!privilege.isUserPrivValid(request, priv)) {
        out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
        return;
    }
    Object[] object = getAllFullName();
    HashMap<String, String> map1 = (HashMap<String, String>) object[0];
    HashMap<String, String> map2 = (HashMap<String, String>) object[1];
    String[][] info = (String[][]) request.getSession().getAttribute("info");
    request.getSession().removeAttribute("info");
    String name = "";
    String realName = "";
    String mobile = "";
    String gender = "";
    String personNO = "";
    String dept = "";
    List<List<String>> successList = new ArrayList<List<String>>();
    DeptUserDb dud = new DeptUserDb();
    if (info != null) {
        for (int i = 0; i < info.length; i++) {
            List<String> list = new ArrayList<String>();
            name = info[i][0];
            realName = info[i][1];
            mobile = info[i][2];
            gender = info[i][3];
            personNO = info[i][4];
            dept = info[i][5];
            if ("".equals(personNO)) {
                personNO = UserDb.getNextPersonNo();
            }
            boolean flag = create(name, realName, mobile, gender, personNO);
            String deptCode = "";
            String depName = "";
            String parentCode = "root";
            String newDeptName[] = dept.split("\\\\");
            int depLevel = newDeptName.length;
            for (int j = 0; j < depLevel; j++) {
                depName += depName.equals("") ? newDeptName[j] : ("-" + newDeptName[j]);
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
                        childLeaf.setName(newDeptName[j]);
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
                dud.delUser(name);
                dud.create(deptCode, name, "");
            }
            if (flag) {
                list.add(name);
                list.add(realName);
                list.add(mobile);
                list.add(gender);
                list.add(personNO);
                list.add(dept);
                successList.add(list);
            }
        }
    }
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>用户导入</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link type="text/css" rel="stylesheet" href="<%=request.getContextPath() %>/js/hopscotch/css/hopscotch.css"/>
    <script type="text/javascript" src="<%=request.getContextPath() %>/js/jquery.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath() %>/js/hopscotch/hopscotch.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath() %>/inc/common.js"></script>
    <script src="<%=request.getContextPath() %>/js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="<%=request.getContextPath() %>/js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="<%=request.getContextPath() %>/js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css"
          media="screen"/>
    <link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/skin/common/organize.css"/>
</head>
<body>
<div class="oranize-number">
    <!--最底层灰色条-->
    <div class="oranize-number-linegray"></div>
    <!--蓝色条1-->
    <div class="oranize-number-lineblue1" style="display:inline;"></div>
    <!--灰色条2-->
    <div class="oranize-number-lineblue1 oranize-number-lineblue2"></div>


    <!--1步-->
    <div class="oranize-blue1">1</div>
    <!--2步-->
    <div class="oranize-blue2 oranize-roundness-blue2">2</div>
    <!--3步-->
    <div class="oranize-blue3 oranize-roundness-blue3">3</div>
    <!--1步文字-->
    <div class="oranize-txt1 ">导入Excel</div>
    <!--2步文字-->
    <div class="oranize-txt2 ">确认信息</div>
    <!--3步文字-->
    <div class="oranize-txt3 oranize-txt-sel">完成</div>
</div>

<script type="text/javascript">
    parent.hiddenLoading();
    function finish() {
        parent.page_refresh();
        // window.location.href="<%=request.getContextPath()%>/admin/organize/organize.jsp?type=list";
    }
</script>
</html>
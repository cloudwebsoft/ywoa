package com.redmoon.oa.person;

import cn.js.fan.util.*;
import cn.js.fan.db.*;
import java.sql.ResultSet;
import cn.js.fan.security.SecurityUtil;
import javax.servlet.http.*;
import java.sql.SQLException;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.pvg.Privilege;
import cn.js.fan.web.Global;

public class UserService {
  boolean debug = true;

  public UserService() {
  }

  public boolean MasterLogin(HttpServletRequest request) {
    String name = StrUtil.UnicodeToGB(request.getParameter("name"));
    String pwd = request.getParameter("pwd");
    if (name == null || pwd == null) {
      return false;
    }
    HttpSession session = request.getSession(true);
    boolean isvalid = false;
    String sql =
        "select t1.name,t2.Password from sq_master as t1,Member as t2 where t1.name=" +
        StrUtil.sqlstr(name) + " and t1.name=t2.RegName";
    Conn conn = new Conn(Global.getDefaultDB());
    ResultSet rs = null;
    try {
      rs = conn.executeQuery(sql);
      if (rs != null && rs.next()) {
        if (rs.getString(2).equals(SecurityUtil.MD5(pwd))) {
          isvalid = true;
          session.setAttribute("sq_master", name);
        }
      }
    }
    catch (Exception e) {
      LogUtil.getLog(getClass()).error(e);
    }
    finally {
      try {
        if (rs != null) {
          rs.close();
          rs = null;
        }
        conn.close();
      }
      catch (SQLException e) {
      }
    }
    return isvalid;
  }

  public String getProxyPerson(HttpServletRequest request) {
    Privilege privilege = new Privilege();
    String person = privilege.getUser(request);
    String sql = "select person from member where RegName=" + StrUtil.sqlstr(person);
    Conn conn = new Conn(Global.getDefaultDB());
    ResultSet rs = null;
    try {
      rs = conn.executeQuery(sql);
      if (rs!=null && rs.next()) {
        person = rs.getString(1);
      }
    }
    catch (Exception e) {
      LogUtil.getLog(getClass()).error(e);
    }
    finally {
      if (rs!=null) {
        try { rs.close(); } catch (Exception e) {}
        rs = null;
      }
      if (conn!=null) {
        conn.close();
        conn = null;
      }
    }
    return person;
  }

  public boolean AddFriend(HttpServletRequest request) throws ErrMsgException {
    if (!CookieBean.getCookieValue(request, "oa_islogin").equals("y")) {
      throw new ErrMsgException("您尚未登录!");
    }
    String name = CookieBean.getCookieValue(request, "oa_name");
    String friend = request.getParameter("friend");
    if (friend == null) {
      throw new ErrMsgException("信息不全");
    }
    if (name.equals(friend)) {
      throw new ErrMsgException("您不能把自己加为好友!");
    }
    String sql = "select friend from sq_friend where name=" + StrUtil.sqlstr(name) +
        " and friend=" + StrUtil.sqlstr(friend);
    ResultSet rs = null;
    Conn conn = new Conn(Global.getDefaultDB());
    boolean isfriend = false;
    try {
      rs = conn.executeQuery(sql);
      if (rs != null && rs.next())
        isfriend = true;
    }
    catch (SQLException e) {
      LogUtil.getLog(getClass()).error("AddFriend error:" + e.getMessage());
    }
    finally {
      try {
        if (rs != null)
          rs.close();
      }
      catch (SQLException e) {}
      if (isfriend) {
        conn.close();
        throw new ErrMsgException(friend + "已经是您的好友!");
      }
    }

    int rowcount = 0;
    try {
        sql = "insert into sq_friend (name,friend) values (" +
              StrUtil.sqlstr(name) + "," + StrUtil.sqlstr(friend) + ")";
        rowcount = conn.executeUpdate(sql);
    }
    catch(SQLException e) {
        LogUtil.getLog(getClass()).error( e.getMessage() );
    }
    finally {
        if ( conn!=null ) {
            conn.close();
            conn = null;
        }
    }
    if (rowcount > 0)
      return true;
    else
      return false;
  }

/*
  public void setStayTime(HttpServletRequest req, HttpServletResponse res) {
    CookieBean cookiebean = new CookieBean();
    cookiebean.setCookieValue(req, res, "oa_staytime",
                              "" + System.currentTimeMillis());
    String username = cookiebean.getCookieValue(req, "oa_name");
    String upsql = "update sq_online set staytime=getDate() where name=" +
        StrUtil.sqlstr(username);
    Conn conn = null;
    try {
        conn = new Conn(Global.defaultDB);
        if (conn.executeUpdate(upsql) == 0) {
            //如果更新未成功,则说明可能是停留某页面时间过长而使得未更新staytime被刷新出论坛
            //此时需再加入此用户至在线用户
            String sql = "select name from sq_online where name=" +
                         StrUtil.sqlstr(username);
            ResultSet rs = conn.executeQuery(sql);
            boolean isexist = false;
            if (rs != null && rs.next()) {
                isexist = true;
            }
            if (rs != null)
                rs.close();
            if (isexist) //如果已存在该用户,则再次重试
                conn.executeUpdate(upsql);
            else {
                int isguest = 0;
                if (cookiebean.getCookieValue(req, "oa_pwd").equals(""))
                    isguest = 1;
                sql = "insert into sq_online (name,isguest,ip) values (" +
                      StrUtil.sqlstr(cookiebean.getCookieValue(req, "oa_name")) +
                      "," +
                      isguest + "," + StrUtil.sqlstr(req.getRemoteAddr()) + ")";
                conn.executeUpdate(sql);
            }
        }
    }
    catch( SQLException e) {
        LogUtil.getLog(getClass()).error( e.getMessage() );
    }
    finally {
        if ( conn!=null ) {
            conn.close();
            conn = null;
        }
    }
  }
*/
/*
  public long getStayTime(HttpServletRequest req) {
    CookieBean cookiebean = new CookieBean();
    String staytime = cookiebean.getCookieValue(req, "oa_staytime");
    long st = 0;
    try {
      st = Long.parseLong(staytime);
    }
    catch (java.lang.NumberFormatException e) {
      st = System.currentTimeMillis();
    }
    return st;
  }

  public void refreshStayTime(HttpServletRequest req, HttpServletResponse res) {
    long staytime = System.currentTimeMillis();
    if ( (staytime - getStayTime(req)) / 1000 * 60 > 5) {
      //大于5分钟则刷新
      setStayTime(req, res);
    }
  }
*/
  public boolean regist(HttpServletRequest req) throws
      ErrMsgException {
    boolean isvalid = true;
    String RegName = (String) req.getParameter("RegName");
    if (RegName == null || RegName.equals("")) {
      throw new ErrMsgException("用户名不能为空!");
    }
    RegName = StrUtil.UnicodeToGB(RegName);
    String Password = (String) req.getParameter("Password");
    if (Password == null || Password.equals("")) {
      throw new ErrMsgException("密码不能为空!");
    }
    String Password2 = (String) req.getParameter("Password2");
    if (Password2 != null && !Password2.equals(Password)) {
      throw new ErrMsgException("两次输入的密码不一致!");
    }
    String Question = StrUtil.UnicodeToGB(req.getParameter("Question"));
    String Answer = StrUtil.UnicodeToGB(req.getParameter("Answer"));
    if (Question == null || Answer == null)
      throw new ErrMsgException("密码问题和答案不能为空!");
    String department_id = StrUtil.getNullString(req.getParameter("department_id"));
    if (department_id.equals("000"))
      throw new ErrMsgException("请选择部门!");
    String RealName = StrUtil.UnicodeToGB(StrUtil.getNullString(req.getParameter(
        "RealName")));
    if (RealName.equals(""))
      throw new ErrMsgException("真实姓名不能为空！");
    String Career = StrUtil.UnicodeToGB(StrUtil.getNullString(req.getParameter(
        "Career")));
    String Gender = StrUtil.getNullString(req.getParameter("Gender"));
    String Job = StrUtil.UnicodeToGB(StrUtil.getNullString(req.getParameter("Job")));
    String BirthYear = StrUtil.getNullString(req.getParameter("BirthYear"));
    String BirthMonth = StrUtil.getNullString(req.getParameter("BirthMonth"));
    String BirthDay = StrUtil.getNullString(req.getParameter("BirthDay"));
    String Birthday = "";
    if (!BirthYear.equals(""))
      Birthday = BirthYear + "-" + BirthMonth + "-" + BirthDay;

    String Marriage = StrUtil.UnicodeToGB(StrUtil.getNullString(req.getParameter(
        "Marriage")));
    String Phone = StrUtil.UnicodeToGB(StrUtil.getNullString(req.getParameter(
        "Phone")));
    String Mobile = StrUtil.UnicodeToGB(StrUtil.getNullString(req.getParameter(
        "Mobile")));
    String State = StrUtil.UnicodeToGB(StrUtil.getNullString(req.getParameter(
        "State")));
    String City = StrUtil.UnicodeToGB(StrUtil.getNullString(req.getParameter("City")));
    String Address = StrUtil.UnicodeToGB(StrUtil.getNullString(req.getParameter(
        "Address")));
    String PostCode = StrUtil.getNullString(req.getParameter("PostCode"));
    String IDCard = StrUtil.getNullString(req.getParameter("IDCard"));
    String RealPic = StrUtil.getNullString(req.getParameter("RealPic"));
    String Hobbies = StrUtil.UnicodeToGB(StrUtil.getNullString(req.getParameter(
        "Hobbies")));
    String Email = StrUtil.getNullString(req.getParameter("Email"));
    String OICQ = StrUtil.getNullString(req.getParameter("OICQ"));
    String RegDate = "getDate()";

    String sql = "select * from member where RegName=" +
        StrUtil.sqlstr(RegName);
    try {
      Password = SecurityUtil.MD5(Password);
    }
    catch (Exception e) {
      LogUtil.getLog(getClass()).error(e.getMessage());
    }

    Conn conn = new Conn(Global.getDefaultDB());
    ResultSet rs = null;
    try {
      rs = conn.executeQuery(sql);
      if (rs != null && rs.next()) {
        isvalid = false;
        throw new ErrMsgException("该用户名已被注册,请选择新的用户名!");
      }
    }
    catch (SQLException e) {
      LogUtil.getLog(getClass()).error(e);
    }
    finally {
      try {
        if (rs != null)
          rs.close();
        if (!isvalid && conn != null)
          conn.close();
      }
      catch (SQLException e) {}
    }

    isvalid = false;
    sql = "insert into Member (RegName,Password,Question,Answer,RealName,Career,Gender,Job, Birthday," +
        "Marriage, Phone, Mobile, State, City, Address, PostCode, IDCard," +
        "RealPic, Hobbies, Email,OICQ,RegDate,department_id) values (" +
        StrUtil.sqlstr(RegName) + "," +
        StrUtil.sqlstr(Password) + "," + StrUtil.sqlstr(Question) + "," +
        StrUtil.sqlstr(Answer) + "," + StrUtil.sqlstr(RealName) + "," +
        StrUtil.sqlstr(Career) + "," + StrUtil.sqlstr(Gender) + "," +
        StrUtil.sqlstr(Job) + "," + StrUtil.sqlstr(Birthday) + "," +
        StrUtil.sqlstr(Marriage) + "," +
        StrUtil.sqlstr(Phone) + "," + StrUtil.sqlstr(Mobile) + "," +
        StrUtil.sqlstr(State) + "," + StrUtil.sqlstr(City) + "," +
        StrUtil.sqlstr(Address) + "," + StrUtil.sqlstr(PostCode) + "," +
        StrUtil.sqlstr(IDCard) + "," + StrUtil.sqlstr(RealPic) + "," +
        StrUtil.sqlstr(Hobbies) + "," + StrUtil.sqlstr(Email) + "," +
        StrUtil.sqlstr(OICQ) + "," + RegDate + "," + department_id + ")";
    //Debug.println(sql);
    try {
      conn.beginTrans();
      if (conn.executeUpdate(sql) > 0) {
        sql = "insert into sq_user (name,experience,credit) values (" +
            StrUtil.sqlstr(RegName) + ",500,500)";
        int rowcount = conn.executeUpdate(sql);
        if (rowcount > 0)
          isvalid = true;
        else
          isvalid = false;
      }
      if (!isvalid)
        conn.rollback();
      else
        conn.commit();
    }
    catch (SQLException e) {
      LogUtil.getLog(getClass()).error(e.getMessage());
      isvalid = false;
    }
    finally {
      if (conn != null)
        conn.close();
    }
    return isvalid;
  }

  public boolean isRegNameExist(HttpServletRequest req, HttpServletResponse res) throws
      ErrMsgException {
    String regname = req.getParameter("RegName");
    if (regname == null || regname.trim().equals(""))
      throw new ErrMsgException("用户名不能为空!");
    String sql = "select RegName from member where RegName=" +
        StrUtil.sqlstr(regname);
    Conn conn = new Conn(Global.getDefaultDB());
    ResultSet rs = null;
    boolean isexist = false;
    try {
      rs = conn.executeQuery(sql);
      if (rs != null && rs.next()) {
        isexist = true;
        throw new ErrMsgException("该用户名已被注册!");
      }
      else
        isexist = false;
    }
    catch (SQLException e) {
      LogUtil.getLog(getClass()).error("isRegNameExist error:" + e.getMessage());
    }
    finally {
      try {
        if (rs != null)
          rs.close();
      }
      catch (SQLException e) {
      }
      conn.close();
    }
    return isexist;
  }

  public int getDepartmentID(String username) {
    int department_id = -1;
    String sql = "select department_id from member where RegName=" +
        StrUtil.sqlstr(username);
    Conn conn = null;
    ResultSet rs = null;
    try {
      conn = new Conn(Global.getDefaultDB());
      rs = conn.executeQuery(sql);
      if (rs != null && rs.next()) {
        department_id = rs.getInt(1);
      }
    }
    catch (SQLException e) {
      LogUtil.getLog(getClass()).error("getDepartmentID error:" + e.getMessage());
    }
    finally {
      if (rs != null) {
        try {
          rs.close();
        }
        catch (SQLException e) {
        }
        rs = null;
      }
      conn.close();
    }
    return department_id;
  }

  public boolean isUserExist(String user) {
    if (user == null || user.trim().equals(""))
      return false;
    String sql = "select RegName from member where RegName=" +
        StrUtil.sqlstr(user);
    Conn conn = new Conn(Global.getDefaultDB());
    ResultSet rs = null;
    boolean isexist = false;
    try {
      rs = conn.executeQuery(sql);
      if (rs != null && rs.next()) {
        isexist = true;
      }
      else
        isexist = false;
    }
    catch (SQLException e) {
      LogUtil.getLog(getClass()).error("isUserExist error:" + e.getMessage());
    }
    finally {
      try {
        if (rs != null) {
          rs.close();
          rs = null;
        }
      }
      catch (SQLException e) {
      }
      conn.close();
    }
    return isexist;
  }

  public boolean edituserinfo(HttpServletRequest req) throws
      ErrMsgException {
    boolean isvalid = true;
    String RegName = (String) req.getParameter("RegName");
    if (RegName == null || RegName.equals("")) {
      throw new ErrMsgException("用户名不能为空!");
    }
    RegName = StrUtil.UnicodeToGB(RegName);

    String department_id = StrUtil.getNullString(req.getParameter("department_id"));
    String isuservalid = req.getParameter("isvalid");

    String Password = StrUtil.getNullString(req.getParameter("Password"));
    String Password2 = StrUtil.getNullString(req.getParameter("Password2"));
    if (!Password.equals(Password2)) {
      throw new ErrMsgException("两次输入的密码不一致!");
    }
    String Question = StrUtil.UnicodeToGB(req.getParameter("Question"));
    String Answer = StrUtil.UnicodeToGB(req.getParameter("Answer"));
    if (Question == null || Answer == null)
      throw new ErrMsgException("密码问题和答案不能为空!");

    String RealName = StrUtil.UnicodeToGB(StrUtil.getNullString(req.getParameter(
        "RealName")));
    if (RealName.equals(""))
      throw new ErrMsgException("真实姓名不能为空！");
    String Career = StrUtil.UnicodeToGB(StrUtil.getNullString(req.getParameter(
        "Career")));
    String Gender = StrUtil.getNullString(req.getParameter("Gender"));
    String Job = StrUtil.UnicodeToGB(StrUtil.getNullString(req.getParameter("Job")));
    String BirthYear = StrUtil.getNullString(req.getParameter("BirthYear"));
    String BirthMonth = StrUtil.getNullString(req.getParameter("BirthMonth"));
    String BirthDay = StrUtil.getNullString(req.getParameter("BirthDay"));
    String Birthday = "";
    if (!BirthYear.equals(""))
      Birthday = BirthYear + "-" + BirthMonth + "-" + BirthDay;
    //if (Birthday.trim().equals(""))
    Birthday = StrUtil.sqlstr(Birthday);

    String Marriage = StrUtil.UnicodeToGB(StrUtil.getNullString(req.getParameter(
        "Marriage")));
    String Phone = StrUtil.UnicodeToGB(StrUtil.getNullString(req.getParameter(
        "Phone")));
    String Mobile = StrUtil.UnicodeToGB(StrUtil.getNullString(req.getParameter(
        "Mobile")));
    String State = StrUtil.UnicodeToGB(StrUtil.getNullString(req.getParameter(
        "State")));
    String City = StrUtil.UnicodeToGB(StrUtil.getNullString(req.getParameter("City")));
    String Address = StrUtil.UnicodeToGB(StrUtil.getNullString(req.getParameter(
        "Address")));
    String PostCode = StrUtil.getNullString(req.getParameter("PostCode"));
    String IDCard = StrUtil.getNullString(req.getParameter("IDCard"));
    String RealPic = StrUtil.getNullString(req.getParameter("RealPic"));
    String Email = StrUtil.getNullString(req.getParameter("Email"));
    String OICQ = StrUtil.getNullString(req.getParameter("OICQ"));
    String[] HobbiesAry = req.getParameterValues("Hobbies");
    String[] privary = req.getParameterValues("priv");

    String email_name = StrUtil.getNullStr(req.getParameter("email_name"));
    String email_pwd = StrUtil.getNullStr(req.getParameter("email_pwd"));
    String isMailSeted = StrUtil.getNullStr(req.getParameter("isEmailSeted"));

    String Hobbies = "";
    if (HobbiesAry!=null)
      for (int i = 0; i < HobbiesAry.length; i++) {
        if (Hobbies.equals(""))
          Hobbies += StrUtil.UnicodeToGB(HobbiesAry[i]);
        else
          Hobbies += "|" + StrUtil.UnicodeToGB(HobbiesAry[i]);
      }

    String sql = "select * from member where RegName=" +
        StrUtil.sqlstr(RegName);
    String MD5pwd = "";
    try {
      if (!Password.equals(""))
        MD5pwd = SecurityUtil.MD5(Password);
    }
    catch (Exception e) {
      LogUtil.getLog(getClass()).error(e);
    }

    if (Password.equals(""))
      sql = "update Member set isvalid=" + isuservalid + ",department_id=" +
          department_id + ",Question=" +
          StrUtil.sqlstr(Question) + ",Answer=" + StrUtil.sqlstr(Answer) +
          ",RealName=" +
          StrUtil.sqlstr(RealName) + ",Career=" + StrUtil.sqlstr(Career) +
          ",Gender=" +
          StrUtil.sqlstr(Gender) + ",Job=" + StrUtil.sqlstr(Job) + ",Birthday=" +
          Birthday +
          ",Marriage=" + StrUtil.sqlstr(Marriage) + ",Phone=" +
          StrUtil.sqlstr(Phone) +
          ",Mobile=" + StrUtil.sqlstr(Mobile) + ",State=" + StrUtil.sqlstr(State) +
          ",City=" + StrUtil.sqlstr(City) + ",Address=" + StrUtil.sqlstr(Address) +
          ",PostCode=" + StrUtil.sqlstr(PostCode) + ",IDCard=" +
          StrUtil.sqlstr(IDCard) +
          ",RealPic=" + StrUtil.sqlstr(RealPic) + ",Hobbies=" +
          StrUtil.sqlstr(Hobbies) +
          ",Email=" + StrUtil.sqlstr(Email) + ",OICQ=" + StrUtil.sqlstr(OICQ) +
          " where RegName=" + StrUtil.sqlstr(RegName);
    else {
      try {
        Password = SecurityUtil.MD5(Password);
      }
      catch (Exception e) {}
      sql = "update Member set isvalid=" + isuservalid + ",department_id=" +
          department_id + ",Question=" +
          StrUtil.sqlstr(Question) + ",Password=" + StrUtil.sqlstr(MD5pwd) +
          ",Answer=" + StrUtil.sqlstr(Answer) + ",RealName=" +
          StrUtil.sqlstr(RealName) + ",Career=" + StrUtil.sqlstr(Career) +
          ",Gender=" +
          StrUtil.sqlstr(Gender) + ",Job=" + StrUtil.sqlstr(Job) + ",Birthday=" +
          Birthday +
          ",Marriage=" + StrUtil.sqlstr(Marriage) + ",Phone=" +
          StrUtil.sqlstr(Phone) +
          ",Mobile=" + StrUtil.sqlstr(Mobile) + ",State=" + StrUtil.sqlstr(State) +
          ",City=" + StrUtil.sqlstr(City) + ",Address=" + StrUtil.sqlstr(Address) +
          ",PostCode=" + StrUtil.sqlstr(PostCode) + ",IDCard=" +
          StrUtil.sqlstr(IDCard) +
          ",RealPic=" + StrUtil.sqlstr(RealPic) + ",Hobbies=" +
          StrUtil.sqlstr(Hobbies) +
          ",Email=" + StrUtil.sqlstr(Email) + ",OICQ=" + StrUtil.sqlstr(OICQ) +
          " where RegName=" + StrUtil.sqlstr(RegName);
    }

    int rowcount = 0;

    Conn conn = null;
    try {
        conn = new Conn(Global.getDefaultDB());
        rowcount = conn.executeUpdate(sql);

        //修改权限
        sql = "delete from user_priv where name=" + StrUtil.sqlstr(RegName);
        conn.executeUpdate(sql);
        if (privary != null) {
            int len = privary.length;
            sql = "";
            for (int k = 0; k < len; k++) {
                sql += "insert into user_priv (name,priv) values (" +
                        StrUtil.sqlstr(RegName) + "," +
                        StrUtil.sqlstr(privary[k]) + ");";
            }
            rowcount = conn.executeUpdate(sql);
        }

        //如果邮箱用户名不为空，则置邮箱
        if (!email_name.trim().equals("")) {
            String SHA_Pwd = "";
            sql = "";
            try {
                SHA_Pwd = SecurityUtil.SHA_BASE64_24(email_pwd); //james中默认密码为12345678
            } catch (Exception e) {
                LogUtil.getLog(getClass()).error("SHA:" + e.getMessage());
            }
            //如果邮箱已设
            if (isMailSeted.equals("y")) {
                //如果密码为空，则表示不修改用户密码
                if (!email_pwd.equals("")) {
                    sql = "update users set pwdRaw=" + StrUtil.sqlstr(email_pwd) +
                          ",pwdHash=" +
                          StrUtil.sqlstr(SHA_Pwd) + " where username=" +
                          StrUtil.sqlstr(email_name);
                    conn.executeUpdate(sql);
                }
            } else {
                //如果邮箱未设，且用户名不为空，则分配邮箱给用户
                if (!email_name.trim().equals("")) {
                    sql = "insert into users (username,pwdHash,pwdAlgorithm,useForwarding,useAlias,alias,pwdRaw) values (";
                    sql += StrUtil.sqlstr(email_name) + "," +
                            StrUtil.sqlstr(SHA_Pwd) + "," +
                            StrUtil.sqlstr("SHA") + ",";
                    sql += "0,0,''," + StrUtil.sqlstr(email_pwd) + ")";
                    conn.executeUpdate(sql);
                    sql = "update Member set email_name=" +
                          StrUtil.sqlstr(email_name) +
                          " where RegName=" + StrUtil.sqlstr(RegName);
                    conn.executeUpdate(sql);
                }
            }
        } else { //删除邮箱
            sql = "delete users where username=" + StrUtil.sqlstr(RegName);
            conn.executeUpdate(sql);
        }
    }
    catch( SQLException e) {
        LogUtil.getLog(getClass()).error( e.getMessage() );
    }
    finally {
        if ( conn!=null ) {
            conn.close();
            conn = null;
        }
    }
    if (rowcount > 0)
      return true;
    else
      return false;
  }

  public boolean editmyinfo(HttpServletRequest req) throws
      ErrMsgException {
    boolean isvalid = true;
    String RegName = (String) req.getParameter("RegName");
    if (RegName == null) {
      throw new ErrMsgException("用户名不能为空!");
    }
    RegName = StrUtil.UnicodeToGB(RegName);
    String Password = StrUtil.getNullString(req.getParameter("Password"));
    String Password2 = StrUtil.getNullString(req.getParameter("Password2"));
    if (!Password.equals(Password2)) {
      throw new ErrMsgException("两次输入的密码不一致!");
    }
    String Question = StrUtil.UnicodeToGB(req.getParameter("Question"));
    String Answer = StrUtil.UnicodeToGB(req.getParameter("Answer"));
    if (Question == null || Answer == null)
      throw new ErrMsgException("密码问题和答案不能为空!");

    String RealName = StrUtil.UnicodeToGB(StrUtil.getNullString(req.getParameter(
        "RealName")));
    String Career = StrUtil.UnicodeToGB(StrUtil.getNullString(req.getParameter(
        "Career")));
    String Gender = StrUtil.getNullString(req.getParameter("Gender"));
    String Job = StrUtil.UnicodeToGB(StrUtil.getNullString(req.getParameter("Job")));
    String BirthYear = StrUtil.getNullString(req.getParameter("BirthYear"));
    String BirthMonth = StrUtil.getNullString(req.getParameter("BirthMonth"));
    String BirthDay = StrUtil.getNullString(req.getParameter("BirthDay"));
    String Birthday = "";
    if (!BirthYear.equals(""))
      Birthday = BirthYear + "-" + BirthMonth + "-" + BirthDay;
    if (Birthday.trim().equals(""))
      throw new ErrMsgException("生日信息请填写完整！");

    String Marriage = StrUtil.UnicodeToGB(StrUtil.getNullString(req.getParameter(
        "Marriage")));
    String Phone = StrUtil.UnicodeToGB(StrUtil.getNullString(req.getParameter(
        "Phone")));
    String Mobile = StrUtil.UnicodeToGB(StrUtil.getNullString(req.getParameter(
        "Mobile")));
    String State = StrUtil.UnicodeToGB(StrUtil.getNullString(req.getParameter(
        "State")));
    String City = StrUtil.UnicodeToGB(StrUtil.getNullString(req.getParameter("City")));
    String Address = StrUtil.UnicodeToGB(StrUtil.getNullString(req.getParameter(
        "Address")));
    String PostCode = StrUtil.getNullString(req.getParameter("PostCode"));
    String IDCard = StrUtil.getNullString(req.getParameter("IDCard"));
    String RealPic = StrUtil.getNullString(req.getParameter("RealPic"));
    String Email = StrUtil.getNullString(req.getParameter("Email"));
    String OICQ = StrUtil.getNullString(req.getParameter("OICQ"));
    String[] HobbiesAry = req.getParameterValues("Hobbies");
    String Hobbies = "";
    if (HobbiesAry!=null)
      for (int i = 0; i < HobbiesAry.length; i++) {
        if (Hobbies.equals(""))
          Hobbies += StrUtil.UnicodeToGB(HobbiesAry[i]);
        else
          Hobbies += "|" + StrUtil.UnicodeToGB(HobbiesAry[i]);
      }

    String sql = "select * from member where RegName=" +
        StrUtil.sqlstr(RegName);
    String MD5pwd = "";
    try {
      if (!Password.equals(""))
        MD5pwd = SecurityUtil.MD5(Password);
    }
    catch (Exception e) {
      LogUtil.getLog(getClass()).error(e);
    }

    if (Password.equals(""))
      sql = "update Member set Question=" +
          StrUtil.sqlstr(Question) + ",Answer=" + StrUtil.sqlstr(Answer) +
          ",RealName=" +
          StrUtil.sqlstr(RealName) + ",Career=" + StrUtil.sqlstr(Career) +
          ",Gender=" +
          StrUtil.sqlstr(Gender) + ",Job=" + StrUtil.sqlstr(Job) + ",Birthday=" +
          StrUtil.sqlstr(Birthday) +
          ",Marriage=" + StrUtil.sqlstr(Marriage) + ",Phone=" +
          StrUtil.sqlstr(Phone) +
          ",Mobile=" + StrUtil.sqlstr(Mobile) + ",State=" + StrUtil.sqlstr(State) +
          ",City=" + StrUtil.sqlstr(City) + ",Address=" + StrUtil.sqlstr(Address) +
          ",PostCode=" + StrUtil.sqlstr(PostCode) + ",IDCard=" +
          StrUtil.sqlstr(IDCard) +
          ",RealPic=" + StrUtil.sqlstr(RealPic) + ",Hobbies=" +
          StrUtil.sqlstr(Hobbies) +
          ",Email=" + StrUtil.sqlstr(Email) + ",OICQ=" + StrUtil.sqlstr(OICQ) +
          " where RegName=" + StrUtil.sqlstr(RegName);
    else {
      try {
        Password = SecurityUtil.MD5(Password);
      }
      catch (Exception e) {}
      sql = "update Member set Question=" +
          StrUtil.sqlstr(Question) + ",Password=" + StrUtil.sqlstr(MD5pwd) +
          ",Answer=" + StrUtil.sqlstr(Answer) + ",RealName=" +
          StrUtil.sqlstr(RealName) + ",Career=" + StrUtil.sqlstr(Career) +
          ",Gender=" +
          StrUtil.sqlstr(Gender) + ",Job=" + StrUtil.sqlstr(Job) + ",Birthday=" +
          Birthday +
          ",Marriage=" + StrUtil.sqlstr(Marriage) + ",Phone=" +
          StrUtil.sqlstr(Phone) +
          ",Mobile=" + StrUtil.sqlstr(Mobile) + ",State=" + StrUtil.sqlstr(State) +
          ",City=" + StrUtil.sqlstr(City) + ",Address=" + StrUtil.sqlstr(Address) +
          ",PostCode=" + StrUtil.sqlstr(PostCode) + ",IDCard=" +
          StrUtil.sqlstr(IDCard) +
          ",RealPic=" + StrUtil.sqlstr(RealPic) + ",Hobbies=" +
          StrUtil.sqlstr(Hobbies) +
          ",Email=" + StrUtil.sqlstr(Email) + ",OICQ=" + StrUtil.sqlstr(OICQ) +
          " where RegName=" + StrUtil.sqlstr(RegName);
    }

    int rowcount = 0;
    Conn conn = null;
    try {
        conn = new Conn(Global.getDefaultDB());
        rowcount = conn.executeUpdate(sql);
    }
    catch( SQLException e) {
        LogUtil.getLog(getClass()).error( e.getMessage() );
    }
    finally {
        if ( conn!=null ) {
            conn.close();
            conn = null;
        }
    }
    if (rowcount > 0)
      return true;
    else
      return false;
  }

  public String[] getUserEmailNamePwd(String name) {
    String sql = "select username,pwdRaw from users,Member where users.username=Member.email_name and Member.RegName="+StrUtil.sqlstr(name);
    ResultSet rs = null;
    Conn conn = null;
    String[] r = new String[2];
    try {
      conn = new Conn(Global.getDefaultDB());
      rs = conn.executeQuery(sql);
      if (rs != null) {
        if (rs.next()) {
          r[0] = StrUtil.getNullStr(rs.getString(1));
          r[1] = StrUtil.getNullStr(rs.getString(2));
        }
      }
    }
    catch (SQLException e) {
    }
    finally {
      if (rs != null) {
        try {
          rs.close();
        }
        catch (SQLException e) {}
        rs = null;
      }
      if (conn != null) {
        conn.close();
        conn = null;
      }
    }
    return r;
  }

  public boolean modUserEmailPwd(String email_name, String newpwd) {
    String pwd = "";
    try {
      pwd = SecurityUtil.SHA_BASE64_24(newpwd);
    }
    catch (Exception e) {
      LogUtil.getLog(getClass()).error("modUserEmailPwd:"+e.getMessage());
    }
    String sql = "update users set pwdRaw="+StrUtil.sqlstr(newpwd)+",pwdHash="+StrUtil.sqlstr(pwd)+" where username="+StrUtil.sqlstr(email_name);
    Conn conn = null;
    boolean r = false;
    try {
        conn = new Conn(Global.getDefaultDB());

        if (conn.executeUpdate(sql) > 0)
            r = true;
        else
            r = false;
    }
    catch( Exception e ) {
        LogUtil.getLog(getClass()).error( e.getMessage() );
    }
    finally {
        if (conn != null) {
            conn.close();
            conn = null;
        }
    }
    return r;
  }
}

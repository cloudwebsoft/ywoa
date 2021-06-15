package com.redmoon.forum.person;

/**
 * Title:        风青云[商城]
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      船艇学院
 * @author 		 风青云
 * @version 1.0
 */

import java.util.*;

import javax.servlet.http.*;

import cn.js.fan.security.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.redmoon.forum.*;
import com.redmoon.oa.person.*;
import org.apache.log4j.*;

public class userservice extends UserService {
    String connname;
    boolean debug = true;
    static Logger logger = Logger.getLogger(userservice.class.getName());

    public userservice() {
        connname = Global.getDefaultDB();
    }

    public boolean MasterLogin(HttpServletRequest request) {
        String name = ParamUtil.get(request, "username");
        String pwd = ParamUtil.get(request, "pwd");
        if (name == null || pwd == null)
            return false;

        String pwdMD5 = "";
        try {
            pwdMD5 = SecurityUtil.MD5(pwd);
        } catch (Exception e) {
            logger.error("MasterLogin: " + e.getMessage());
        }

        UserDb user = new UserDb();
        user = user.getUser(name);
        if (!user.isLoaded())
            return false;
        if (!user.getPwdMd5().equals(pwdMD5))
            return false;

        MasterDb md = new MasterDb();
        md = md.getMasterDb(name);
        if (md.isLoaded()) {
            HttpSession session = request.getSession(true);
            session.setAttribute("sq_master", name);
            return true;
        } else
            return false;
    }

    @Override
    public boolean AddFriend(HttpServletRequest request) throws ErrMsgException {
        UserFriendMgr ufm = new UserFriendMgr();
        return ufm.sendAddFriendRequest(request);
    }

    /*
    public boolean AddFriend(HttpServletRequest request) throws ErrMsgException {
        CookieBean cookiebean = new CookieBean();
        if (!cookiebean.getCookieValue(request, "islogin").equals("y"))
            throw new ErrMsgException("您尚未登录!");
        String name = cookiebean.getCookieValue(request, "name");
        String friend = ParamUtil.get(request, "friend");
        if (friend == null)
            throw new ErrMsgException("信息不全");
        if (name.equals(friend))
            throw new ErrMsgException("您不能把自己加为好友!");

        UserFriendDb ufd = new UserFriendDb();
        ufd.setName(name);
        ufd.setFriend(friend);
        return ufd.create();
    }
    */

    public void setStayTime(HttpServletRequest req, HttpServletResponse res) throws
            ErrMsgException {
        HttpSession session = req.getSession(true);
        Authorization auth = (Authorization)session.getAttribute(Privilege.SESSION_CWBBS_AUTH);
        if (auth==null)
            return;
        auth.setStayTime(System.currentTimeMillis());
        session.setAttribute(Privilege.SESSION_CWBBS_AUTH, auth);

        OnlineUserDb ou = new OnlineUserDb();
        ou = ou.getOnlineUserDb(auth.getName());
        ou.setStayTime(new java.util.Date());
        // 如果用户在线
        if (ou.isLoaded()) {
            ou.save();
        } else {
            // 如果不在线，即超时被刷新掉了，则再加入在线列表
            int isguest = 0;
            if (Privilege.isGuest(req))
                isguest = 1;
            ou.setName(auth.getName());
            ou.setIp(req.getRemoteAddr());
            ou.setGuest(isguest == 1 ? true : false);
            ou.create();
            // logger.info("setStayTime: create " + username + " isguest=" + isguest);
        }
    }

    public long getStayTime(HttpServletRequest req) {
        long st = 0;
        Privilege pvg = new Privilege();
        Authorization auth = pvg.getAuthorization(req);
        if (auth!=null) {
            st = auth.getStayTime();
        }
        return st;
    }

    public void refreshStayTime(HttpServletRequest req, HttpServletResponse res) throws
            ErrMsgException {
        long staytime = System.currentTimeMillis();
        long t = (staytime - getStayTime(req)) / 300000; // (1000 * 60 * 5);
        if (t > 5) {
            // 大于5分钟则刷新
            setStayTime(req, res);
        }
    }

    public boolean isRegNameExist(HttpServletRequest req, String regName) throws
            ErrMsgException {
        if (regName == null || regName.trim().equals(""))
            throw new ErrMsgException("用户名不能为空!");
        UserDb user = new UserDb();
        user = user.getUserDbByNick(regName);
        if (user!=null && user.isLoaded())
            return true;
        else
            return false;
    }

    public boolean editmyinfo(HttpServletRequest req,
                              HttpServletResponse response) throws
            ErrMsgException {
        ParamConfig pc = new ParamConfig("form_rule.xml");
        ParamChecker pck = new ParamChecker(req);
        try {
            pck.doCheck(pc.getFormRule("user_modify"));
        } catch (CheckErrException e) {
            // 如果onError=exit，则会抛出异常
            throw new ErrMsgException(e.getMessage());
        }

        String RegName = pck.getString("RegName");
        if (RegName == null || RegName.trim().equals("")) {
            throw new ErrMsgException("用户名不能为空!");
        }

        String Password = pck.getString("Password").trim();
        String Password2 = pck.getString("Password2").trim();

        if (!Password.equals("")) {
            try {
                Password = SecurityUtil.MD5(Password);
            } catch (Exception e) {
                logger.error("editmyinfo:" + e.getMessage());
            }
        }

        String Question = pck.getString("Question");
        String Answer = pck.getString("Answer");
        String RealName = pck.getString("RealName");
        String Career = pck.getString("Career");
        String Gender = pck.getString("Gender");
        String Job = pck.getString("Job");
        String BirthYear = pck.getString("BirthYear");
        String BirthMonth = pck.getString("BirthMonth");
        String BirthDay = pck.getString("BirthDay");
        String Birthday = BirthYear + "-" + BirthMonth + "-" + BirthDay;
        int Marriage = pck.getInt("Marriage");
        String Phone = pck.getString("Phone");
        String Mobile = pck.getString("Mobile");
        String State = pck.getString("State");
        String City = pck.getString("City");
        String Address = pck.getString("Address");
        String PostCode = pck.getString("PostCode");
        String IDCard = pck.getString("IDCard");
        String RealPic = pck.getString("RealPic");
        String Hobbies = pck.getString("Hobbies");
        String Email = pck.getString("Email");
        String OICQ = pck.getString("OICQ");
        String sign = pck.getString("Content");
        boolean secret = pck.getBoolean("isSecret");
        String tzID = pck.getString("timeZone");
        TimeZone tz = TimeZone.getTimeZone(tzID);
        String home = pck.getString("home");
        String msn = pck.getString("msn");
        String locale = pck.getString("locale");

        com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
        if (sign.length() > cfg.getIntProperty("forum.sign_length")) {
            throw new ErrMsgException(SkinUtil.LoadString(req, "res.label.forum.user", "sign_limit_count") + cfg.getIntProperty("forum.sign_length"));
        }

        int provinceId = pck.getInt("province_id");
        int cityId = pck.getInt("city_id");
        int countryId = pck.getInt("country_id");

        UserDb user = new UserDb();
        user = user.getUser(RegName);

        // 如果能够改名，即操作时已改名
        String nick = ParamUtil.get(req, "nick");
        if (user.isCanRename() && !user.getNick().equals(nick)) {
            if (isRegNameExist(req, nick)) {
                throw new ErrMsgException(SkinUtil.LoadString(req, "res.label.forum.user", "user_name_exist"));
            }
            user.setNick(nick);
            user.setCanRename(false); // 只允许改一次
        }

        if (!Password.equals(""))
            user.setPwdMd5(Password);

        user.setQuestion(Question);
        user.setAnswer(Answer);
        user.setRealName(RealName);
        user.setCareer(Career);
        user.setGender(Gender);

        user.setJob(Job);
        try {
            user.setBirthday(DateUtil.parse(Birthday, "yyyy-MM-dd"));
        } catch (Exception e) {
            logger.error("editmyinfo:" + e.getMessage());
        }
        user.setMarriage(Marriage);
        user.setPhone(Phone);
        user.setMobile(Mobile);
        user.setState(State);
        user.setCity(City);
        user.setAddress(Address);
        user.setPostCode(PostCode);
        user.setIDCard(IDCard);
        user.setRealPic(RealPic);
        user.setEmail(Email);
        user.setOicq(OICQ);
        user.setHobbies(Hobbies);
        user.setSign(sign);
        if (!Password2.equals("")) {
            user.setRawPwd(Password2);
        }
        user.setSecret(secret);
        user.setTimeZone(tz);
        user.setHome(home);
        user.setMsn(msn);
        user.setLocale(locale);

        user.setProvinceId(provinceId);
        user.setCityId(cityId);
        user.setCountryId(countryId);

        boolean re = user.save();
        if (re) {
            if (!Password.equals("")) {
                com.redmoon.oa.person.UserDb ud = new com.redmoon.oa.person.UserDb();
                ud = ud.getUserDb(RegName);
                ud.setPwdMD5(user.getPwdMd5());
                ud.setPwdRaw(user.getRawPwd());
                ud.save();
            }
            // 取得用户的locale
            if (!locale.equals("")) {
                String[] ary = StrUtil.split(locale, "_");
                if (ary != null && ary.length == 2) {
                    Locale loc = new Locale(ary[0], ary[1]);
                    HttpSession session = req.getSession(true);
                    session.setAttribute(SkinUtil.SESSION_LOCALE, loc);
                }
            } else {
                HttpSession session = req.getSession(true);
                session.removeAttribute(SkinUtil.SESSION_LOCALE);
            }
        }
        return re;
    }

    public boolean ModifyPWD(HttpServletResponse response, String username,
                             String pwd, String privurl) throws ErrMsgException {
        String Password = "";
        try {
            Password = SecurityUtil.MD5(pwd);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        com.redmoon.oa.person.UserDb user = new com.redmoon.oa.person.UserDb();
        user = user.getUserDb(username);
        user.setPwdMD5(Password);
        user.setPwdRaw(pwd);
        return user.save();
    }

}

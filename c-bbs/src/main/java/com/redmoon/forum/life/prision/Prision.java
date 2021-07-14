package com.redmoon.forum.life.prision;

import cn.js.fan.db.*;
import java.util.*;
import cn.js.fan.util.*;
import javax.servlet.http.*;
import com.redmoon.forum.*;
import com.redmoon.forum.person.UserDb;
import org.apache.log4j.Logger;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;

public class Prision {
    static String connname = Global.getDefaultDB();
    public static int bailFeeDay = 0; // 逮捕天数对应于保释金的乘数
    Logger logger = Logger.getLogger(Prision.class.getName());

    public Prision() {
        if (bailFeeDay == 0) {
            Config cfg = Config.getInstance();
            bailFeeDay = cfg.getIntProperty("forum.bailFeeDay");
        }
    }

    public static int getBailDlt() {
        return bailFeeDay;
    }

    /**
     * 取得释放日期
     * @param username String
     * @return Calendar
     */
    public static Calendar getReleaseDate(String username) {
        UserDb user = new UserDb();
        user = user.getUser(username);

        java.util.Date arresttime = null;
        int arrestday = 0;
        arresttime = user.getArrestTime();
        arrestday = user.getArrestDay();
        Calendar c1 = DateUtil.add(arresttime, arrestday); //释放日期
        return c1;
    }

    public static int getDayBeginRelease(String username) {
        UserDb user = new UserDb();
        user = user.getUser(username);

        java.util.Date arresttime = null;
        int arrestday = 0;
        arresttime = user.getArrestTime();
        arrestday = user.getArrestDay();
        Calendar c1 = DateUtil.add(arresttime, arrestday); //释放日期
        Calendar c2 = Calendar.getInstance(); //当前日期
        return DateUtil.datediff(c1, c2);
    }

    public static boolean isUserArrested(String username) {
        UserDb user = new UserDb();
        user = user.getUser(username);
        int arrestday = 0;
        arrestday = user.getArrestDay();
        if (arrestday == 0)
            return false;

        java.util.Date arresttime = null;
        arresttime = user.getArrestTime();

        Calendar c1 = DateUtil.add(arresttime, arrestday); //释放日期
        Calendar c2 = Calendar.getInstance(); //当前日期
        if (DateUtil.compare(c1, c2) == 1)
            return true;
        else
            return false;
    }

    public boolean isPolice(String username) {
        UserDb user = new UserDb();
        user = user.getUser(username);
        if (user.getIsPolice() == 0)
            return false;
        else
            return true;
    }

    public boolean arrest(String police, String username, String arrestreason,
                          int arrestday) throws ResKeyException {
        UserDb user = new UserDb();
        user = user.getUser(username);
        if (!user.isLoaded()) {
            throw new ResKeyException("res.forum.life.prision.Prision", "none_user");
        }
        user.setArrestTime(Calendar.getInstance().getTime());
        user.setArrestPolice(police);
        user.setArrestReason(arrestreason);
        user.setArrestDay(arrestday);

        Calendar c1 = DateUtil.add(new java.util.Date(), arrestday); //释放日期
        user.setReleaseTime(c1.getTime());
        return user.save();
    }

    /**
     * 带有参数arresttime
     * @param police String
     * @param user String
     * @param arresttime String
     * @param arrestreason String
     * @param arrestday int
     * @return boolean
     */
    public boolean arrest(String police, String username, String arresttime,
                          String arrestreason, int arrestday) throws ResKeyException {
        UserDb user = new UserDb();
        user = user.getUser(username);
        if (!user.isLoaded()) {
            throw new ResKeyException("res.forum.life.prision.Prision", "none_user");
        }
        user.setArrestPolice(police);
        user.setArrestReason(arrestreason);
        user.setArrestDay(arrestday);

        java.util.Date d = null;
        try {
            d = DateUtil.parse(arresttime, "yyyy-MM-dd HH:mm:ss");
        } catch (Exception e) {
            logger.error("arrest:" + e.getMessage());
        }
        // System.out.println("Prision.java arrest=" + arresttime + " yyyy-MM-dd HH:mm:ss d=" + d);
        user.setArrestTime(d);
        Calendar c1 = DateUtil.add(new java.util.Date(), arrestday); //释放日期
        user.setReleaseTime(c1.getTime());
        return user.save();
    }

    public boolean release(String username) throws ResKeyException {
        UserDb user = new UserDb();
        user = user.getUser(username);
        if (!user.isLoaded()) {
            throw new ResKeyException("res.forum.life.prision.Prision", "none_user");
        }
        user.setArrestDay(0);
        return user.save();
    }

    public String[][] getPolices() {
        ConnAry fq = new ConnAry(connname);
        String sql =
                "select name from sq_user where ispolice=1 order by regdate asc";
        fq.query(sql);
        String[][] ary = fq.getResultAry();
        fq.close();
        return ary;
    }

    public boolean bail(HttpServletRequest request) throws ErrMsgException {
        Privilege privilege = new Privilege();
        if (!privilege.isUserLogin(request)) {
            throw new ErrMsgException(SkinUtil.LoadString(request, "err_not_login"));
        }

        String username = ParamUtil.get(request, "username");
        UserDb arrestuser = new UserDb();
        arrestuser = arrestuser.getUser(username);

        int bailfee = 0;
        int arday = 0;
        // 取逮捕天数
        arday = arrestuser.getArrestDay();

        bailfee = bailFeeDay * arday;
        // 取出用户的信用值
        UserDb user = arrestuser.getUser(privilege.getUser(request));
        int credit = user.getCredit();
        if (bailfee > credit) {
            String s = SkinUtil.LoadString(request, "res.forum.life.prision.Prision", "err_credit_not_enough");
            s = s.replaceFirst("\\$c", "" + credit);
            s = s.replaceFirst("\\$u", username);
            throw new ErrMsgException(s); // "您的信用值只有" + credit + "，不能保释" + username + "！");
        }

        arrestuser.setArrestDay(0);
        arrestuser.save();

        user.setCredit(user.getCredit() - bailFeeDay * arday);
        user.save();
        return true;
    }
}

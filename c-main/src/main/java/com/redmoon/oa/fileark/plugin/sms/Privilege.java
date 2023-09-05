package com.redmoon.oa.fileark.plugin.sms;

import cn.js.fan.base.IPrivilege;
import cn.js.fan.security.SecurityUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.person.UserDb;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class Privilege implements IPrivilege {
    public static final String NAME = "PLUGIN_TEACH_NAME";
    public static final String CARDID = "PLUGIN_TEACH_CARDID";

    public Privilege() {

    }

    public String getUser(HttpServletRequest request) {
      HttpSession session = request.getSession(true);
      return (String)session.getAttribute(NAME);
    }

    public static String getCardId(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        return (String) session.getAttribute(CARDID);
    }

    public boolean isUserLogin(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String name = (String)session.getAttribute(NAME);
        if (name == null)
            return false;
        else
            return true;
    }

    public boolean canSee(HttpServletRequest request, String dirCode) {
        if (!isUserLogin(request))
            return false;
        if (getUser(request).equals(UserDb.ADMIN)) // admin 享有所有权限
            return true;
        return false;
    }

    public boolean login(HttpServletRequest request) throws
            ErrMsgException {
        String cardId = ParamUtil.get(request, "cardId");
        String pwd = ParamUtil.get(request, "pwd");
        String fingerPrint = ParamUtil.get(request, "fingerPrint");
        if (cardId.equals("") || pwd.equals(""))
            throw new ErrMsgException("卡号或密码不能为空！");
        HttpSession session = request.getSession(true);
        String pwdMD5 = "";
        try {
            pwdMD5 = SecurityUtil.MD5(pwd);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error(e);
        }
        /*// 如果是指定了机器
        if (vcd.isUseFingerPrint())
            if (!fingerPrint.equals(vcd.getFingerPrint())) {
                throw new ErrMsgException("对不起，您只能在指定的机器上学习！");
            }
        // 检查卡是否有效
        Date d = new Date();
        if (DateUtil.compare(d, vcd.getBeginDate())!=1)
            throw new ErrMsgException("您的卡的开始时间为：" + vcd.getBeginDate());
        if (DateUtil.compare(d, vcd.getEndDate())==1)
            throw new ErrMsgException("您的卡的结束时间为：" + vcd.getEndDate());

        if (!vcd.getPwd().equals(pwdMD5))
            throw new ErrMsgException("密码错误！");

        session.setAttribute(NAME, vcd.getUserName());
        session.setAttribute(CARDID, cardId);
*/
        return true;

    }

    public static void logout(HttpServletRequest req) {
        HttpSession session = req.getSession(true);
        session.invalidate();
    }

    public boolean isValid(HttpServletRequest request, String priv) {
        return canSee(request, priv);
    }

}

package com.redmoon.forum.miniplugin.ad;

import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import com.redmoon.forum.plugin.base.IPluginScore;
import java.util.Iterator;
import com.redmoon.forum.plugin.ScoreUnit;
import com.redmoon.forum.plugin.ScoreMgr;
import java.util.Vector;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import com.redmoon.forum.person.UserDb;
import com.redmoon.forum.message.MessageDb;
import cn.js.fan.web.Global;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class AdUserLink {
    Logger logger = Logger.getLogger(AdUserLink.class.getName());

    public AdUserLink() {

    }

    public String ad(HttpServletRequest request) throws ErrMsgException {
        String callingPage = request.getHeader("Referer");
        if (callingPage==null)
            return "";
        // logger.info("ad:" + callingPage);
        if (callingPage.indexOf(Global.server)!=-1)
            return "";
        String userId = "-1";
        userId = ParamUtil.get(request, "userId");

        UserDb ud = new UserDb();
        ud = ud.getUser(userId);
        if (ud == null || !ud.isLoaded())
            return "";

        ScoreMgr sm = new ScoreMgr();
        Vector v = sm.getAllScore();
        Iterator ir = v.iterator();
        MessageDb md = new MessageDb();
        md.setTitle("广告链接加分成功！");

        String content = "广告来自：" + callingPage + "\r\n";
        while (ir.hasNext()) {
            ScoreUnit su = (ScoreUnit) ir.next();
            String strValue = sm.getScoreText(su.getCode(), "advertiseLink");
            int value = 0;
            if (StrUtil.isNumeric(strValue))
                value = Integer.parseInt(strValue);
            IPluginScore ips = su.getScore();
            if (ips != null) {
                ips.changeUserSum(ud.getName(), value);
                content += su.getName() + " + " + value + "\r\n";
            }
        }
        md.setContent(content);
        md.setSender(md.USER_SYSTEM);
        md.setReceiver(ud.getName());
        md.setIp(request.getRemoteAddr());
        md.create();

        return "用户" + ud.getName() + "的广告加分有效！";
    }
}

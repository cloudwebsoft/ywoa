package com.redmoon.oa.ui.desktop;

import com.cloudweb.oa.entity.OaNotice;
import com.cloudweb.oa.entity.OaNoticeReply;
import com.cloudweb.oa.service.IOaNoticeReplyService;
import com.cloudweb.oa.service.IOaNoticeService;
import com.cloudweb.oa.utils.SpringUtil;
import com.redmoon.oa.ui.DesktopMgr;
import com.redmoon.oa.person.UserDesktopSetupDb;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;

import javax.servlet.http.HttpServletRequest;

import com.redmoon.oa.ui.IDesktopUnit;

import java.util.Iterator;

import com.redmoon.oa.pvg.Privilege;

import java.util.List;

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
public class NoticeDesktopUnit implements IDesktopUnit {
    public NoticeDesktopUnit() {
    }

    @Override
    public String getPageList(HttpServletRequest request,
                              UserDesktopSetupDb uds) {
        DesktopMgr dm = new DesktopMgr();
        com.redmoon.oa.ui.DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());
        String url = du.getPageList();
        return url;
    }

    @Override
    public String display(HttpServletRequest request, UserDesktopSetupDb uds) {
        DesktopMgr dm = new DesktopMgr();
        com.redmoon.oa.ui.DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());

        Privilege privilege = new Privilege();
        String userName = privilege.getUser(request);

        String str = "";

        IOaNoticeService oaNoticeService = SpringUtil.getBean(IOaNoticeService.class);
        List<OaNotice> list = oaNoticeService.selectMyNoticeOnDesktop(userName, uds.getCount());

        IOaNoticeReplyService oaNoticeReplyService = SpringUtil.getBean(IOaNoticeReplyService.class);

        Iterator<OaNotice> ir = list.iterator();
        if (ir.hasNext()) {
            str += "<table class='article_table'>";
            while (ir.hasNext()) {
                OaNotice oaNotice = ir.next();

                String t = StrUtil.getLeft(oaNotice.getTitle(), uds.getWordCount());

                OaNoticeReply oaNoticeReply = oaNoticeReplyService.getOaNoticeReply(oaNotice.getId(), userName);
                if (oaNotice.getIsBold()==1 || (oaNoticeReply!=null && "0".equals(oaNoticeReply.getIsReaded()))) {
                    t = "<b>" + t + "</b>";
                }
                if (!"".equals(oaNotice.getColor())) {
                    t = "<font color='" + oaNotice.getColor() + "'>" + t + "</font>";
                }

                str += "<tr><td class='article_content'><a title='" + oaNotice.getTitle() + "' href='" + du.getPageShow() + oaNotice.getId() + "&isShow=" + oaNotice.getIsShow() + "'>" + t + "</a></td><td class='article_time'>[" + DateUtil.format(oaNotice.getBeginDate(), "yyyy-MM-dd") + "]</td></tr>";
            }
            str += "</table>";
        } else {
            str = "<div class='no_content'><img title='暂无通知'  src='images/desktop/no_content.jpg'></div>";
        }

        return str;
    }

}

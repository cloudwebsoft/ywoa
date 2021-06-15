package com.redmoon.forum.robot;

import cn.js.fan.module.cms.robot.RobotInfo;
import cn.js.fan.module.cms.robot.RobotUtil;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.NumberUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.blog.UserConfigDb;
import com.redmoon.forum.Attachment;
import com.redmoon.forum.Leaf;
import com.redmoon.forum.MsgCache;
import com.redmoon.forum.MsgDb;
import com.redmoon.forum.person.UserDb;
import com.redmoon.forum.person.UserMgr;
import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class Roboter
{
    RobotUtil ru = new RobotUtil();
    Vector result;
    String lastError = "";

    public Roboter() {
        this.result = new Vector();
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getLastError() {
        return this.lastError;
    }

    public boolean gatherDocument(HttpServletRequest request, String docUrl, RobotDb rd)
            throws ErrMsgException
    {
        String docSrc = this.ru.gather(docUrl, rd.getString("charset"));

        String title = "";
        Vector v = this.ru.getSegmentRegex(docSrc, rd.getString("doc_title_rule"), "[subject]", true, true);

        if (v.size() > 0)
            title = (String)v.elementAt(0);
        if (title.trim().equals("")) {
            this.lastError = (title + " 标题为空");
            LogUtil.getLog(getClass()).error("gatherDocument:title=" + title);
            return false;
        }

        LogUtil.getLog(getClass()).info("gatherDocument:title=" + title);

        if (this.ru.filterData(title, rd.getString("doc_title_filter"))) {
            this.lastError = (title + " 标题被过滤");
            return false;
        }

        if (!this.ru.hasKey(title, rd.getString("doc_title_key"))) {
            this.lastError = (title + " 标题不含有关键字");
            return false;
        }

        title = this.ru.replace(title, rd.getString("doc_title_replace_before"), rd.getString("doc_title_replace_after"));

        if (rd.getInt("doc_title_repeat_allow") == 0) {
            Date d = DateUtil.addDate(new Date(), -3);
            MsgDb md = new MsgDb();
            if (md.isMsgWithTitleExist(title, d)) {
                this.lastError = (title + " 标题已存在");
                return false;
            }
        }

        String content = "";
        v = this.ru.getSegmentRegex(docSrc, rd.getString("doc_content_rule"), "[message]", true, true);

        if (v.size() > 0) {
            content = (String)v.elementAt(0);
        }

        if (this.ru.filterData(content, rd.getString("doc_content_filter"))) {
            this.lastError = (title + " 内容被过滤");
            return false;
        }

        content = this.ru.replace(content, rd.getString("doc_content_replace_before"), rd.getString("doc_content_replace_after"));

        Calendar cal = Calendar.getInstance();
        String year = "" + cal.get(1);
        String month = "" + (cal.get(2) + 1);
        String relativePath = "forum/upfile/" + year + "/" + month;
        String fullPath = Global.realPath + relativePath;
        File f = new File(fullPath);
        if (!f.isDirectory()) {
            f.mkdirs();
        }

        boolean isDownloadImg = rd.getString("doc_save_img").equals("1");
        Vector v2 = new Vector();
        content = RobotUtil.fixImageLinkAndDownload(docUrl, content, rd.getString("charset"), rd.getString("doc_img_flash_prefix"), isDownloadImg, relativePath, v2);
        boolean isDlownloadFlash = rd.getString("doc_save_flash").equals("1");

        Vector v3 = new Vector();
        content = RobotUtil.fixFlashLinkAndDownload(docUrl, content, rd.getString("charset"), rd.getString("doc_img_flash_prefix"), isDlownloadFlash, relativePath, v3);

        String nicks = rd.getString("topic_user_name");
        nicks = nicks.replaceAll("，", ",");
        String[] users = StrUtil.split(nicks, ",");

        UserMgr um = new UserMgr();
        String userName = "";
        if (users.length == 1) {
            userName = um.getUserDbByNick(users[0]).getName();
        } else {
            int k = NumberUtil.random(0, users.length);
            UserDb ud = um.getUserDbByNick(users[k]);
            if (ud == null) {
                throw new ErrMsgException("用户：" + users[k] + " 不存在！");
            }
            userName = ud.getName();
        }
        int expression = rd.getInt("expression");

        if (expression == -1) {
            expression = NumberUtil.random(25, 45);
        }

        Leaf lf = new Leaf();
        lf = lf.getLeaf(rd.getString("dir_code"));
        if (lf == null)
        {
            this.lastError = (title + " 编码为" + rd.getString("dir_code") + "的版块不存在！");
            return false;
        }

        MsgDb md = new MsgDb();
        md.setBoardcode(rd.getString("dir_code"));
        md.setTitle(title);
        md.setContent(content);
        md.setIp("127.0.0.1");
        md.setType(0);
        md.setBlogId(UserConfigDb.NO_BLOG);

        md.setCheckStatus(rd.getInt("examine"));
        md.setName(userName);
        md.setExpression(expression);
        md.create();

        if (md.getCheckStatus() == 1) {
            md.setBoardNewAddId(md.getId());

            md.setBoardStatistic(true, md.getId());
        }

        this.result.addElement("<a href=" + docUrl + ">" + title + "</a>");

        int orders = 0;
        int fileNameIndex = relativePath.length() + 1;

        if (isDownloadImg)
        {
            Iterator ir2 = v2.iterator();
            while (ir2.hasNext()) {
                String filePath = (String)ir2.next();
                Attachment att = new Attachment();
                String fileName = filePath.substring(fileNameIndex);
                att.setDiskName(fileName);
                att.setMsgId(md.getId());
                att.setName(userName);
                att.setVisualPath(relativePath);
                att.setOrders(orders);
                att.setUploadDate(new Date());
                File file = new File(Global.realPath + filePath);
                att.setSize(file.length());
                att.create();
                orders++;
            }
            if (v2.size() > 0) {
                MsgCache mc = new MsgCache();
                mc.refreshUpdate(md.getId());
            }
        }

        if (isDlownloadFlash)
        {
            Iterator ir3 = v3.iterator();
            while (ir3.hasNext()) {
                String filePath = (String)ir3.next();
                Attachment att = new Attachment();
                String fileName = filePath.substring(fileNameIndex);
                att.setDiskName(fileName);
                att.setMsgId(md.getId());
                att.setName(userName);
                att.setVisualPath(relativePath);
                att.setOrders(orders);
                att.setUploadDate(new Date());
                File file = new File(Global.realPath + filePath);
                att.setSize(file.length());
                att.create();
                orders++;
            }
            if (v3.size() > 0) {
                MsgCache mc = new MsgCache();
                mc.refreshUpdate(md.getId());
            }
        }
        return true;
    }

    public static String[] getListPageUrls(RobotDb rd)
    {
        String list_url_link = rd.getString("list_url_link");
        String list_url_type = rd.getString("list_url_type");
        String[] listUrlAry = null;
        if (list_url_type.equals("0")) {
            listUrlAry = StrUtil.split(list_url_link, "\n");
        } else {
            int list_page_begin = rd.getInt("list_page_begin");
            int list_page_end = rd.getInt("list_page_end");
            listUrlAry = new String[list_page_end - list_page_begin + 1];
            int k = 0;
            for (int i = list_page_begin; i <= list_page_end; i++) {
                listUrlAry[k] = list_url_link.replaceFirst("\\[page\\]", "" + i);
                k++;
            }
        }
        return listUrlAry;
    }

    public static Vector getDocUrlsOfList(String listPageUrl, RobotDb rd)
            throws ErrMsgException
    {
        RobotUtil ru = new RobotUtil();
        String pageSrc = ru.gather(listPageUrl, rd.getString("charset"));

        Vector v = ru.getSegmentRegex(pageSrc, rd.getString("list_field_rule"), "[list]", true, true);

        boolean isValid = true;
        if (v.size() == 0) {
            isValid = false;
        }

        String list_field_src = "";
        if (isValid) {
            list_field_src = (String)v.elementAt(0);
            list_field_src = list_field_src.trim();
            if (list_field_src.equals(""))
                isValid = false;
        }
        if (!isValid) {
            LogUtil.getLog(Roboter.class).error(listPageUrl + " 未找到列表区域！pageSrc=" + pageSrc);

            throw new ErrMsgException("<a target=_blank href='" + listPageUrl + "'>" + listPageUrl + "</a> 未找到列表区域！");
        }

        Vector v_list_doc_url_rule = ru.getSegmentRegex(list_field_src, rd.getString("list_doc_url_rule"), "[url]", false, false);

        if (v_list_doc_url_rule.size() == 0) {
            LogUtil.getLog(Roboter.class).error(listPageUrl + " 未取得列表页中贴子的链接，请检查列表区域的获取是否正确，或者获取贴子的规则是否正确");
            LogUtil.getLog(Roboter.class).error("列表区域为：" + list_field_src);

            throw new ErrMsgException("<a target=_blank href='" + listPageUrl + "'>" + listPageUrl + "</a>未取得列表页中贴子的链接，请检查列表区域的获取是否正确，或者获取贴子的规则是否正确");
        }

        return v_list_doc_url_rule;
    }

    public void gatherList(HttpServletRequest request, RobotDb rd)
            throws ErrMsgException
    {
        String[] listUrlAry = getListPageUrls(rd);
        int listUrlAryLen = listUrlAry.length;

        int maxCount = rd.getInt("gather_count");
        int count = 0;
        for (int i = 0; i < listUrlAryLen; i++)
        {
            Vector v_list_doc_url_rule = getDocUrlsOfList(listUrlAry[i], rd);
            int size = v_list_doc_url_rule.size();
            for (int j = 0; j < size; j++) {
                String linkStr = (String)v_list_doc_url_rule.elementAt(j);
                try
                {
                    LogUtil.getLog(getClass()).info("gatherList:doc url=" + rd.getString("list_doc_url_prefix") + RobotUtil.UrlDecode(linkStr, rd.getString("charset")));
                }
                catch (Exception e) {
                }
                if (!gatherDocument(request, rd.getString("list_doc_url_prefix") + linkStr, rd)) {
                    continue;
                }
                count++;
                if (count >= maxCount)
                    return;
            }
        }
    }

    public Vector getResult()
    {
        return this.result;
    }

    public int gatherOneByOne(HttpServletRequest request, int robotId)
            throws ErrMsgException
    {
        RobotDb rd = new RobotDb();
        rd = (RobotDb)rd.getQObjectDb(new Integer(robotId));

        String nicks = rd.getString("topic_user_name");
        nicks = nicks.replaceAll("，", ",");
        UserMgr um = new UserMgr();
        String[] ary = StrUtil.split(nicks, ",");
        if (ary == null) {
            throw new ErrMsgException("用户不能为空！");
        }

        int len = ary.length;
        for (int i = 0; i < len; i++) {
            UserDb ud = um.getUserDbByNick(ary[i]);
            if (ud == null) {
                throw new ErrMsgException("用户" + ary[i] + "不存在！");
            }

        }

        int ret = 0;

        int maxCount = rd.getInt("gather_count");

        HttpSession session = request.getSession();
        RobotInfo gi = (RobotInfo)session.getAttribute(RobotInfo.SESSION_VAR_GATHER_INFO);
        if (gi == null)
        {
            String[] listPageUrls = getListPageUrls(rd);
            gi = new RobotInfo(listPageUrls);
            gi.docPageUrls = getDocUrlsOfList(gi.listPageUrls[gi.curListPageUrlsIndex], rd);

            gi.curDocPageUrlsIndex = 0;
        }

        String docUrl = (String)gi.docPageUrls.elementAt(gi.curDocPageUrlsIndex);

        if (gatherDocument(request, rd.getString("list_doc_url_prefix") + docUrl, rd))
        {
            gi.count += 1;
        }
        else {
            ret = -1;

            this.result.addElement("<a href=" + rd.getString("list_doc_url_prefix") + docUrl + ">" + rd.getString("list_doc_url_prefix") + docUrl + "</a> 采集失败！ " + this.lastError);
        }

        gi.curDocPageUrlsIndex += 1;

        if (gi.curDocPageUrlsIndex >= gi.docPageUrls.size())
        {
            gi.curListPageUrlsIndex += 1;

            if (gi.curListPageUrlsIndex >= gi.listPageUrls.length) {
                ret = 1;
                this.result.addElement("所有列表页均已处理完毕，采集结束，共采集了" + gi.curListPageUrlsIndex + "页");
            }
            else
            {
                gi.curDocPageUrlsIndex = 0;

                gi.docPageUrls = getDocUrlsOfList(gi.listPageUrls[gi.curListPageUrlsIndex], rd);
            }

        }

        if (gi.count >= maxCount) {
            ret = 2;
            this.result.addElement("采集已达到最大数目：" + gi.count + "，采集结束");
        }
        if ((ret == 1) || (ret == 2))
            session.removeAttribute(RobotInfo.SESSION_VAR_GATHER_INFO);
        else
            session.setAttribute(RobotInfo.SESSION_VAR_GATHER_INFO, gi);
        return ret;
    }

    private void jbInit()
            throws Exception
    {
    }
}
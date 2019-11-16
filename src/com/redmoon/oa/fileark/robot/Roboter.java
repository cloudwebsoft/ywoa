package com.redmoon.oa.fileark.robot;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.util.LogUtil;
import com.cloudwebsoft.framework.util.Queue;
import com.redmoon.oa.Config;
import com.redmoon.oa.fileark.DocContent;
import com.redmoon.oa.fileark.DocImage;
import com.redmoon.oa.fileark.Document;
import com.redmoon.oa.fileark.Leaf;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class Roboter {
    RobotUtil ru = new RobotUtil();
    Vector result;
    private String lastError = "";

    public Roboter() {
        this.result = new Vector();
    }

    public Document gatherDocAndCreate(String docSrc, RobotDb rd)
            throws ErrMsgException {
        String title = "";
        String author = "";
        String from = "";
        Vector v = this.ru.getSegmentRegex(docSrc, rd.getString("doc_title_rule"), "[subject]", true, true);

        if (v.size() > 0) {
            title = (String) v.elementAt(0);
        }
        if (title.trim().equals(""))
            LogUtil.getLog(getClass()).error("gatherDocAndCreate:title=" + title);
        else {
            LogUtil.getLog(getClass()).info("gatherDocAndCreate:title=" + title);
        }

        if (this.ru.filterData(title, rd.getString("doc_title_filter"))) {
            this.lastError = (title + " 标题被过滤");
            return null;
        }

        if (!this.ru.hasKey(title, rd.getString("doc_title_key"))) {
            this.lastError = (title + " 标题不含有关键字");
            return null;
        }

        title = this.ru.replace(title, rd.getString("doc_title_replace_before"), rd.getString("doc_title_replace_after"));

        if (rd.getInt("doc_title_repeat_allow") == 0) {
            Date d = DateUtil.addDate(new Date(), -3);
            Document doc = new Document();
            if (doc.isDocWithTitleExist(title, d)) {
                this.lastError = (title + " 标题重复");
                return null;
            }

        }

        String doc_author_rule = StrUtil.getNullString(rd.getString("doc_author_rule"));
        if (doc_author_rule.startsWith("#")) {
            author = doc_author_rule.substring(1);
        } else {
            v = this.ru.getSegmentRegex(docSrc, rd.getString("doc_author_rule"), "[author]", true, true);

            if (v.size() > 0) {
                author = (String) v.elementAt(0);
            }
        }

        String doc_date = StrUtil.getNullStr(rd.getString("doc_date"));
        Date createDate = null;
        LogUtil.getLog(getClass()).info("doc_date=" + doc_date);
        if (!doc_date.equals("")) {
            v = this.ru.getSegmentRegex(docSrc, doc_date, "[date]", true, true);

            LogUtil.getLog(getClass()).info("v.size()=" + v.size());

            if (v.size() > 0) {
                String dateStr = (String) v.elementAt(0);
                String doc_date_format = StrUtil.getNullStr(rd.getString("doc_date_format"));
                if (doc_date_format.equals(""))
                    doc_date_format = "yyyy-MM-dd HH:mm:ss";
                LogUtil.getLog(getClass()).info("dateStr=" + dateStr);
                LogUtil.getLog(getClass()).info("doc_date_format=" + doc_date_format);

                createDate = DateUtil.parse(dateStr, doc_date_format);

                LogUtil.getLog(getClass()).info("createDate=" + DateUtil.format(createDate, "yyyy-MM-dd HH:mm"));
            }

        }

        String doc_source_rule = StrUtil.getNullString(rd.getString("doc_source_rule"));
        if (doc_source_rule.startsWith("#")) {
            from = doc_source_rule.substring(1);
        } else {
            v = this.ru.getSegmentRegex(docSrc, doc_source_rule, "[from]", true, true);

            if (v.size() > 0) {
                from = (String) v.elementAt(0);
            }
        }

        Document doc = new Document();
        doc.setCreateDate(createDate);

        doc.create(rd.getString("dir_code"), title, "", 0, "", "", "admin", -1, author);

        doc = doc.getDocument(doc.getId());
        try {
            doc.UpdateExamine(rd.getInt("examine"));
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error("gatherDocument:" + e.getMessage());
        }
        return doc;
    }

    public boolean gatherDocument(HttpServletRequest request, String docUrl, RobotDb rd, String relativePath)
            throws ErrMsgException {
        String docSrc = this.ru.gather(docUrl, rd.getString("charset"));

        Leaf lf = new Leaf();
        lf = lf.getLeaf(rd.getString("dir_code"));
        if (lf == null) {
            this.lastError = (" 编码为" + rd.getString("dir_code") + "的目录不存在，请修改存入目录！");

            throw new ErrMsgException(this.lastError);
        }

        int curPageNum = 1;

        Document doc = gatherDocAndCreate(docSrc, rd);
        if (doc == null) {
            return false;
        }

        boolean re = gatherDocContentOfPage(docSrc, rd, doc, curPageNum, relativePath);
        if (!re) {
            return false;
        }

        this.result.addElement("<a target=_blank href=" + docUrl + ">" + doc.getTitle() + "</a>");

        HashMap hm = new HashMap();
        hm.put(docUrl, "tmp");

        Vector v = gatherDocPageLinks(docSrc, rd);
        Queue q = new Queue();
        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            String docurl2 = (String) ir.next();

            if (!hm.containsKey(docurl2)) {
                q.enq(docurl2);
            }
        }
        while (!q.isEmpty()) {
            String link = (String) q.deq();
            if (hm.containsKey(link)) {
                continue;
            }
            String docPageSrc = this.ru.gather(link, rd.getString("charset"));

            boolean ret = gatherDocContentOfPage(docPageSrc, rd, doc, curPageNum + 1, relativePath);
            if (!ret)
                return false;
            curPageNum++;

            hm.put(link, "tmp");

            v = gatherDocPageLinks(docSrc, rd);
            ir = v.iterator();
            while (ir.hasNext()) {
                String url2 = (String) ir.next();

                if (!hm.containsKey(url2)) {
                    q.enq(url2);
                }

            }

        }

        return true;
    }

    public boolean gatherDocContentOfPage(String docSrc, RobotDb rd, Document doc, int pageNum, String relativePath) {
        String content = "";
        Vector v = this.ru.getSegmentRegex(docSrc, rd.getString("doc_content_rule"), "[message]", true, true);

        if (v.size() > 0) {
            content = (String) v.elementAt(0);
        }

        if (this.ru.filterData(content, rd.getString("doc_content_filter"))) {
            try {
                doc.del();
            } catch (ErrMsgException e) {
                LogUtil.getLog(getClass()).error("gatherDocContentOfPage:" + e.getMessage());
            }
            this.lastError = (doc.getTitle() + " 内容被过滤");
            return false;
        }

        content = this.ru.replace(content, rd.getString("doc_content_replace_before"), rd.getString("doc_content_replace_after"));

        boolean isDownloadImg = rd.getString("doc_save_img").equals("1");
        Vector v2 = new Vector();
        content = RobotUtil.fixImageLinkAndDownload(content, rd.getString("charset"), rd.getString("doc_img_flash_prefix"), isDownloadImg, relativePath, v2);

        boolean isDownloadFlash = rd.getString("doc_save_flash").equals("1");
        Vector v3 = new Vector();
        content = RobotUtil.fixFlashLinkAndDownload(content, rd.getString("charset"), rd.getString("doc_img_flash_prefix"), isDownloadFlash, relativePath, v3);

        DocContent dc = doc.getDocContent(pageNum);
        if (!dc.isLoaded()) {
            dc = new DocContent();

            dc.create(doc.getId(), content, pageNum);
        } else {
            dc.setContent(content);
            dc.save();
        }
        try {
            doc.UpdatePageCount(pageNum);
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error("gatherDocContent" + e.getMessage());
        }

        doc = doc.getDocument(doc.getId());
        try {
            doc.UpdateExamine(rd.getInt("examine"));
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error("gatherDocument:" + e.getMessage());
        }

        if (isDownloadImg) {
            Iterator ir2 = v2.iterator();
            DocImage di = new DocImage();
            while (ir2.hasNext()) {
                String filePath = (String) ir2.next();
                di.create(filePath, "" + doc.getId(), DocImage.KIND_DOCUMENT, pageNum);
            }

        }

        if (isDownloadFlash) {
            Iterator ir3 = v3.iterator();
            DocImage di = new DocImage();
            while (ir3.hasNext()) {
                String filePath = (String) ir3.next();
                di.create(filePath, "" + doc.getId(), DocImage.KIND_DOCUMENT, pageNum);
            }
        }

        return true;
    }

    public Vector gatherDocPageLinks(String docSrc, RobotDb rd) {
        Vector v = new Vector();
        Vector pageArea = this.ru.getSegmentRegex(docSrc, rd.getString("doc_page_rule"), "[pagearea]", true, true);

        if (pageArea.size() > 0) {
            Vector pageUrlV = this.ru.getSegmentRegex((String) pageArea.elementAt(0), rd.getString("doc_page_url_rule"), "[page]", false, true);

            Iterator ir = pageUrlV.iterator();

            while (ir.hasNext()) {
                String pageLink = (String) ir.next();
                pageLink = rd.getString("doc_page_url_prefix") + pageLink;

                v.addElement(pageLink);
            }
        }
        return v;
    }

    public static String getRelativePath() {
        Calendar cal = Calendar.getInstance();
        String year = "" + cal.get(1);
        String month = "" + (cal.get(2) + 1);
        Config cfg = new Config();
        String path = cfg.get("file_folder") + "/" + year + "/" + month;
        String fullPath = Global.realPath + path;
        File f = new File(fullPath);
        if (!f.isDirectory()) {
            f.mkdirs();
        }
        return path;
    }

    public Vector getResult() {
        return this.result;
    }

    public String getLastError() {
        return this.lastError;
    }

    public static String[] getListPageUrls(RobotDb rd) {
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
            throws ErrMsgException {
        RobotUtil ru = new RobotUtil();
        String pageSrc = ru.gather(listPageUrl, rd.getString("charset"));

        Vector v = ru.getSegmentRegex(pageSrc, rd.getString("list_field_rule"), "[list]", true, true);

        if (v.size() == 0) {
            throw new ErrMsgException(listPageUrl.trim() + " 未找到列表区域！");
        }

        String list_field_rule = (String) v.elementAt(0);

        Vector v_list_doc_url_rule = ru.getSegmentRegex(list_field_rule, rd.getString("list_doc_url_rule"), "[url]", false, false);

        return v_list_doc_url_rule;
    }

    public void gatherList(HttpServletRequest request, RobotDb rd)
            throws ErrMsgException {
        String[] listUrlAry = getListPageUrls(rd);
        int listUrlAryLen = listUrlAry.length;

        String relativePath = getRelativePath();
        int maxCount = rd.getInt("gather_count");
        int count = 0;
        for (int i = 0; i < listUrlAryLen; i++) {
            Vector v_list_doc_url_rule = getDocUrlsOfList(listUrlAry[i], rd);
            int size = v_list_doc_url_rule.size();
            for (int j = 0; j < size; j++) {
                String linkStr = (String) v_list_doc_url_rule.elementAt(j);

                if (!gatherDocument(request, rd.getString("list_doc_url_prefix") + linkStr, rd, relativePath)) {
                    continue;
                }
                count++;
                if (count >= maxCount)
                    return;
            }
        }
    }

    public int gatherOneByOne(HttpServletRequest request, int robotId)
            throws ErrMsgException {
        int ret = 0;

        RobotDb rd = new RobotDb();
        rd = (RobotDb) rd.getQObjectDb(new Integer(robotId));

        int maxCount = rd.getInt("gather_count");

        HttpSession session = request.getSession();
        RobotInfo gi = (RobotInfo) session.getAttribute(RobotInfo.SESSION_VAR_GATHER_INFO);
        if (gi == null) {
            String[] listPageUrls = getListPageUrls(rd);
            gi = new RobotInfo(listPageUrls);

            gi.docPageUrls = getDocUrlsOfList(gi.listPageUrls[gi.curListPageUrlsIndex], rd);

            gi.curDocPageUrlsIndex = 0;
        }

        if (gi.docPageUrls.size() == 0) {
            ret = 0;

            this.result.addElement("getDocUrlsOfList获取列表页中文章链接地址的数量为0！" + this.lastError);
            return ret;
        }

        String docUrl = (String) gi.docPageUrls.elementAt(gi.curDocPageUrlsIndex);

        if (gatherDocument(request, rd.getString("list_doc_url_prefix") + docUrl, rd, getRelativePath())) {
            gi.count += 1;
        } else {
            ret = -1;

            this.result.addElement("<a href=" + rd.getString("list_doc_url_prefix") + docUrl + ">" + rd.getString("list_doc_url_prefix") + docUrl + "</a> 采集失败！ " + this.lastError);
        }

        gi.curDocPageUrlsIndex += 1;

        if (gi.curDocPageUrlsIndex >= gi.docPageUrls.size()) {
            gi.curListPageUrlsIndex += 1;

            if (gi.curListPageUrlsIndex >= gi.listPageUrls.length) {
                ret = 1;
            } else {
                gi.curDocPageUrlsIndex = 0;

                gi.docPageUrls = getDocUrlsOfList(gi.listPageUrls[gi.curListPageUrlsIndex], rd);
            }

        }

        if (gi.count >= maxCount) {
            ret = 2;
        }
        if ((ret == 1) || (ret == 2))
            session.removeAttribute(RobotInfo.SESSION_VAR_GATHER_INFO);
        else
            session.setAttribute(RobotInfo.SESSION_VAR_GATHER_INFO, gi);
        return ret;
    }
}

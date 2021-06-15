package cn.js.fan.module.cms.ui.desktop;

import cn.js.fan.module.cms.ui.IDesktopUnit;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.module.cms.ui.DesktopItemDb;
import cn.js.fan.module.cms.ui.DesktopMgr;
import cn.js.fan.module.cms.ui.DesktopUnit;
import cn.js.fan.util.StrUtil;
import cn.js.fan.module.cms.*;
import java.util.Iterator;
import cn.js.fan.util.DateUtil;
import java.util.HashMap;
import cn.js.fan.module.cms.SubjectListDb;
import cn.js.fan.module.cms.SubjectDb;
import cn.js.fan.db.ListResult;
import cn.js.fan.module.cms.DocumentMgr;
import cn.js.fan.util.ErrMsgException;
import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.module.cms.template.ListSubjectPagniator;

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
public class SubjectListDesktopUnit implements IDesktopUnit  {
    public SubjectListDesktopUnit() {
    }

    public String getPageList(HttpServletRequest request, DesktopItemDb di) {
        // 静态页面
        Config cfg = new Config();
        boolean html_doc = cfg.getBooleanProperty("cms.html_doc");
        if (html_doc) {
            String dir_code = di.getModuleItem();
            SubjectDb lf = new SubjectDb();
            lf = lf.getSubjectDb(dir_code);
            if (lf == null)
                return "";

            String sql = SQLBuilder.getSubjectDocListSql(dir_code);
            SubjectListDb doc = new SubjectListDb();
            int total = doc.getDocCount(sql);
            int pageSize = cfg.getIntProperty("cms.listPageSize");
            ListSubjectPagniator paginator = new ListSubjectPagniator(request,
                    total, pageSize);

            int pageNo = paginator.pageNum2No(1);

            // System.out.println(getClass() + " getPageList: pageNo=" + pageNo);

            return lf.getListHtmlNameByPageNo(pageNo);
        } else {
            DesktopMgr dm = new DesktopMgr();
            DesktopUnit du = dm.getDesktopUnit(di.getModuleCode());
            String url = du.getPageList() + di.getModuleItem();
            return url;
        }
    }

     public String display(HttpServletRequest request, DesktopItemDb di) {
         String dir_code = di.getModuleItem();
         SubjectDb sd = new SubjectDb();
         sd = sd.getSubjectDb(dir_code);
         if (sd==null)
             return "";

         SubjectListDb sld = new SubjectListDb();
         int count = di.getCount();

         String sql = "select d.id from cws_cms_subject_doc s,document d where s.doc_id=d.id and d.examine=" + Document.EXAMINE_PASS + " and s.code=" + StrUtil.sqlstr(dir_code) + " order by d.doc_level desc, d.id desc";

         DocBlockIterator dbi = sld.getDocuments(sql, dir_code, 0, count);

         String str = "<ul>";
         DesktopMgr dm = new DesktopMgr();
         DesktopUnit du = dm.getDesktopUnit(di.getModuleCode());

         String url = du.getPageShow();

         HashMap props = di.getProps();
         boolean isDateShow = false;
         String dateFormat = "";
         String dt = (String)props.get("date");
         if (dt!=null) {
             isDateShow = dt.equals("true") || dt.equals("yes");
             dateFormat = (String)props.get("dateFormat");
             if (dateFormat==null) {
                 dateFormat = "yy-MM-dd";
             }
         }

         cn.js.fan.module.cms.Config cfg = new cn.js.fan.module.cms.
                                           Config();
         boolean isHtml = cfg.getBooleanProperty("cms.html_doc");

         while (dbi.hasNext()) {
             Document doc = (Document) dbi.next();

             if (!isHtml)
                 url = url + "?id=" + doc.getId();
             else
                 url = sld.getDocHtmlName(dir_code, doc, 1);

             if (DateUtil.compare(new java.util.Date(), doc.getExpireDate()) == 2) {
                 str += "<li><a href='"+ url + "'>";
                 if (doc.isBold())
                     str += "<B>";
                 if (!doc.getColor().equals("")) {
                     str += "<font color='" + doc.getColor() + "'>";
                 }
                 str += StrUtil.toHtml(StrUtil.getLeft(doc.getTitle(), di.getTitleLen()));
                 if (!doc.getColor().equals(""))
                     str += "</font>";
                 if (doc.isBold())
                     str += "</B>";
                 if (isDateShow) {
                     str += " " + DateUtil.format(doc.getModifiedDate(), dateFormat);
                 }
                 str += "</a></li>";
             } else {
                 str += "<li><a href='" + url + "'>" +
                         StrUtil.toHtml(StrUtil.getLeft(doc.getTitle(), di.getTitleLen()));
                 if (isDateShow) {
                     str += " " + DateUtil.format(doc.getModifiedDate(), dateFormat);
                 }
                 str += "</a></li>";
             }
         }
         str += "</ul>";
        return str;
    }
}

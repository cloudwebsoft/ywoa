package cn.js.fan.module.cms.site;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.cloudwebsoft.framework.util.LogUtil;

import cn.js.fan.module.cms.Document;
import cn.js.fan.module.cms.DocumentMgr;
import cn.js.fan.module.nav.Navigation;
import cn.js.fan.module.nav.NavigationMgr;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;

public class SiteTag extends BodyTagSupport {
    String code = "";
    String showPage = "";

    public SiteTag() {
    }

    public void setCode(String d) {
        this.code = d;
    }
    
    public void setShowPage(String showPage) {
    	this.showPage = showPage;
    }

    /**
     * put your documentation comment here
     * @return
     */
    public int doEndTag() {
        try {
        	SiteDb sd = new SiteDb();
        	sd = sd.getSiteDb(code);
        	
            if (sd != null) {
                BodyContent bc = getBodyContent();
                String body = bc.getString();

                HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();                
                // SiteTemplateImpl sti = new SiteTemplateImpl();
                // String nav = sti.renderNav(request, sd);
                
                NavigationMgr nmr = new NavigationMgr();
                String type = sd.getString("code");

                if (body.indexOf("$nav")!=-1) {
	                StringBuffer buf = new StringBuffer();
	                buf.append("<ul>");
	
	                Iterator ir = nmr.getAllNav(type).iterator();
	                while (ir.hasNext()) {
	                    buf.append("<li>");
	
	                    Navigation nav = (Navigation) ir.next();
	                    String color = StrUtil.getNullString(nav.getColor());
	                    String name = "";
	                    if (color.equals(""))
	                        name = nav.getName();
	                    else
	                        name = "<font color='" + color + "'>" + nav.getName() +
	                               "</font>";
	                    String target = nav.getTarget();
	                    buf.append("<a target='" + target + "' href='" +
	                               nav.getLink() + "'>" + name + "</a>");
	                    buf.append("</li>");
	                }
	                if (sd.getInt("is_guestbook_open")==1)
	                    buf.append("<li><a href='" + request.getContextPath() + "/site_guestbook.jsp?siteCode=" + sd.getString("code") + "'>留言簿</a></li>");
	                buf.append("</ul>");                
	                
	                body = body.replaceAll("\\$nav", buf.toString());
                }
                body = body.replaceAll("\\$focus", renderFocus(request, sd));
                body = body.replaceAll("\\$recommand", renderRecommand(request, sd));                

                pageContext.getOut().print(body);
            } else {
                pageContext.getOut().print("Site not found!");
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        return EVAL_PAGE;
    }

    
    /**
     * 显示推荐文章
     * @param request
     * @param sd
     * @return
     */
    public String renderRecommand(HttpServletRequest request, SiteDb sd) {
        StringBuffer buf = new StringBuffer();
        // boolean isHtml = cfg.getBooleanProperty("cms.html_doc");
        buf.append("<ul>");
		DocumentMgr dm = new DocumentMgr();
		Document doc = null;
		String[] ids = StrUtil.split(StrUtil.getNullStr(sd.getString("doc_recommand")), ",");
		int len = 0;
		if (ids!=null)
			len = ids.length;
		for (int k=0; k<len; k++) {
			doc = dm.getDocument(StrUtil.toInt(ids[k]));

            String t = doc.getTitle();

            boolean isDateValid = DateUtil.compare(new java.util.Date(),
                    doc.getExpireDate()) == 2;
            if (isDateValid) {
                if (doc.isBold())
                    t = "<B>" + t + "</B>";
                if (!doc.getColor().equals("")) {
                    t = "<font color=" + doc.getColor() + ">" + t + "</font>";
                }
            }

            if (isDateValid && doc.getIsNew() == 1) {
                t += "<img border=0 src='" + request.getContextPath() + "/images/i_new.gif' width='18' height='7'>";
            }

            buf.append("<li>");
            if (doc.getType()==Document.TYPE_LINK) {
                buf.append("<a href='" + doc.getSource() + "' title='" + StrUtil.toHtml(doc.getTitle()) + "' target='_blank'>" + t + "</a>");
            }
            else {
            	String myPage = "site_doc.jsp";
            	if (showPage!=null && !showPage.equals("")) {
            		myPage = showPage;
            	}
                buf.append("<a title='" + StrUtil.toHtml(doc.getTitle()) + "' href='" + myPage + "?siteCode=" +
                           sd.getString("code") +
                           "&docId=" + doc.getId() + "'>" +
                           t + "</a>");
            }

            // 时间
            buf.append("<span>" + DateUtil.format(doc.getCreateDate(), "[yy-MM-dd HH:mm]") + "</span>");


            buf.append("</li>");
        }
        buf.append("</ul>");

        return buf.toString();
    }    

    /**
     * 显示焦点文章
     * @param request
     * @param sd
     * @return
     */
    public String renderFocus(HttpServletRequest request, SiteDb sd) {
         StringBuffer buf = new StringBuffer();
        // boolean isHtml = cfg.getBooleanProperty("cms.html_doc");
        buf.append("<ul>");
		DocumentMgr dm = new DocumentMgr();
		Document doc = null;
		String[] ids = StrUtil.split(StrUtil.getNullStr(sd.getString("doc_focus")), ",");
		int len = 0;
		if (ids!=null)
			len = ids.length;
		for (int k=0; k<len; k++) {
			doc = dm.getDocument(StrUtil.toInt(ids[k]));

            String t = doc.getTitle();

            boolean isDateValid = DateUtil.compare(new java.util.Date(),
                    doc.getExpireDate()) == 2;
            if (isDateValid) {
                if (doc.isBold())
                    t = "<B>" + t + "</B>";
                if (!doc.getColor().equals("")) {
                    t = "<font color=" + doc.getColor() + ">" + t + "</font>";
                }
            }

            if (isDateValid && doc.getIsNew() == 1) {
                t += "<img border=0 src='" + request.getContextPath() + "/images/i_new.gif' width='18' height='7'>";
            }

            buf.append("<li>");
            if (doc.getType()==Document.TYPE_LINK) {
                buf.append("<a href='" + doc.getSource() + "' title='" + StrUtil.toHtml(doc.getTitle()) + "' target='_blank'>" + t + "</a>");
            }
            else {
            	String myPage = "site_doc.jsp";
            	if (showPage!=null && !showPage.equals("")) {
            		myPage = showPage;
            	}            	
                buf.append("<a title='" + StrUtil.toHtml(doc.getTitle()) + "' href='" + myPage + "?siteCode=" +
                           sd.getString("code") +
                           "&docId=" + doc.getId() + "'>" +
                           t + "</a>");
            }

            // 时间
            buf.append("<span>" + DateUtil.format(doc.getCreateDate(), "[yy-MM-dd HH:mm]") + "</span>");

            buf.append("</li>");
        }
        buf.append("</ul>");

        return buf.toString();
    }    
    
}
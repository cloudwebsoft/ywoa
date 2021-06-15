package cn.js.fan.module.cms.template;

import java.util.*;

import cn.js.fan.module.nav.*;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.template.*;
import javax.servlet.http.HttpServletRequest;
import com.cloudwebsoft.framework.util.BeanUtil;
import cn.js.fan.module.cms.Leaf;
import cn.js.fan.module.cms.Document;
import cn.js.fan.module.cms.Directory;
import cn.js.fan.module.cms.Config;
import cn.js.fan.web.Global;

/**
 * <p>Title: 目录树上节点的模板</p>
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
public class LeafTemplateImpl extends VarPart {
    public LeafTemplateImpl() {

    }

    public String toString(HttpServletRequest request, List params) {
        if (field.equals("parentName")) {
            if (keyValue.equalsIgnoreCase("auto")) {
                String kValue = "";
                int id = ParamUtil.getInt(request, "id", -1);
                if (id == -1) {
                    id = StrUtil.toInt((String) request.getAttribute("id"), -1); // 生成静态页面时
                }

                if (id == -1) {
                    kValue = parseKeyValueFromRequest(request);
                } else {
                    Document doc = new Document();
                    doc = doc.getDocument(id);
                    kValue = doc.getDirCode();
                }

                if (kValue.equals(""))
                    kValue = parseKeyValueFromRequest(request);
                Directory dir = new Directory();
                Leaf lf = dir.getLeaf(kValue);
                if (lf == null)
                    throw new IllegalArgumentException(
                            "filed=parentName kValue=" +
                            kValue + " params=" + params);
                lf = dir.getLeaf(lf.getParentCode());
                return lf.getName();
            }
            else {
                String kValue = parseKeyValueFromRequest(request);
                Directory dir = new Directory();
                Leaf lf = dir.getLeaf(kValue);
                if (lf == null)
                    throw new IllegalArgumentException(
                            "filed=parentName kValue=" +
                            kValue + " params=" + params);
                lf = dir.getLeaf(lf.getParentCode());
                return lf.getName();
            }
        } else if (field.equals("nav")) {
            String kValue = "";
            if (keyValue.equals("auto")) {
                int id = ParamUtil.getInt(request, "id", -1);
                if (id == -1) {
                    id = StrUtil.toInt((String) request.getAttribute("id"), -1); // 生成静态页面时
                }

                if (id == -1) {
                    kValue = parseKeyValueFromRequest(request);
                } else {
                    Document doc = new Document();
                    doc = doc.getDocument(id);
                    kValue = doc.getDirCode();
                }
            } else
                kValue = parseKeyValueFromRequest(request);

            if (kValue.equals("")) {
                throw new IllegalArgumentException("filed=nav kValue=" +
                        kValue + " params=" + params + " keyValue=" + keyValue);
            }

            Directory dir = new Directory();
            Leaf lf = dir.getLeaf(kValue);

            String navstr = "";
            String parentcode = lf.getCode();

            Leaf plf = new Leaf();
            Config cfg = new Config();
            boolean isHtml = cfg.getBooleanProperty("cms.html_doc");
            while (!parentcode.equals("root")) {
                plf = plf.getLeaf(parentcode);
                if (plf == null || !plf.isLoaded())
                    break;
                if (plf.getType() == Leaf.TYPE_LIST) {
                    if (isHtml) {
                        navstr = "&nbsp;>>&nbsp;<a href='" +
                                 request.getContextPath() + "/" +
                                 plf.getListHtmlNameByPageNum(request, 1) +
                                 "'>" +
                                 plf.getName() + "</a>" + navstr;
                    } else {
                        navstr = "&nbsp;>>&nbsp;<a href='" +
                                 request.getContextPath() +
                                 "/doc_list_view.jsp?dirCode=" +
                                 StrUtil.UrlEncode(plf.getCode()) + "'>" +
                                 plf.getName() + "</a>" + navstr;
                    }
                } else if (plf.getType() == Leaf.TYPE_NONE) {
                    navstr = "&nbsp;>>&nbsp;" + plf.getName() + "" + navstr;
                } else if (plf.getType() == Leaf.TYPE_COLUMN ||
                           plf.getType() == Leaf.TYPE_SUB_SITE) {
                    if (isHtml) {
                        navstr = "&nbsp;>>&nbsp;<a href='" +
                                 request.getContextPath() + "/" +
                                 plf.getListHtmlPath() + "/index." +
                                 cfg.getProperty("cms.html_ext") + "'>" +
                                 plf.getName() + "</a>" + navstr;
                    } else {
                        navstr = "&nbsp;>>&nbsp;<a href='" +
                                 request.getContextPath() +
                                 "/doc_column_view.jsp?dirCode=" +
                                 StrUtil.UrlEncode(plf.getCode()) + "'>" +
                                 plf.getName() + "</a>" + navstr;
                    }
                } else
                    navstr = "&nbsp;>>&nbsp;" + plf.getName() + navstr;

                parentcode = plf.getParentCode();
            }
            return navstr;
        } else if (field.equalsIgnoreCase("logo")) {
            String kValue = parseKeyValueFromRequest(request);

            Leaf lf = new Leaf();
            lf = lf.getLeaf(kValue);
            if (lf != null && !lf.getLogo().equals("")) {
                cn.js.fan.module.cms.Config cfg = new cn.js.fan.module.cms.
                                                  Config();
                boolean isHtml = cfg.getBooleanProperty("cms.html_doc");
                StringBuffer buf = new StringBuffer();
                // System.out.println(getClass() + " lf.getListHtmlNameByPageNum(request, 1)=" + lf.getListHtmlNameByPageNum(request, 1));
                // @task 文章型节点的处理
                buf.append("<a href='");
                if (isHtml) {
                    if (Global.virtualPath.equals(""))
                        buf.append(
                                "/" +
                                lf.
                                getListHtmlNameByPageNum(request,
                                1));
                    else
                        buf.append(
                                "/" + Global.virtualPath + "/" +
                                lf.getListHtmlNameByPageNum(
                                        request, 1));
                } else {
                    buf.append(request.getContextPath() +
                               "/doc_list_view.jsp?dirCode=" +
                               StrUtil.UrlEncode(lf.getCode()));
                }
                buf.append("'>");
                buf.append("<img border=0 src='" +
                           request.getContextPath() + "/" +
                           lf.getLogo() + "'/>");
                buf.append("</a>");
                return buf.toString();
            }
            else
                return "";
        } else {
            String kValue = parseKeyValueFromRequest(request);

            Leaf lf = new Leaf();
            lf = lf.getLeaf(kValue);
            if (lf != null) {
                BeanUtil bu = new BeanUtil();
                Object obj = bu.getProperty(lf, field);
                return format(obj, props);
            } else
                return "";
        }
    }


}

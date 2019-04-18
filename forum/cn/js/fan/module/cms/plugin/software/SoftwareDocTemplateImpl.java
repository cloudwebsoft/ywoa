package cn.js.fan.module.cms.plugin.software;

import java.util.*;

import cn.js.fan.module.cms.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.template.*;
import com.cloudwebsoft.framework.util.*;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 * $doc.dirCode(code).summary 提取文章的摘要
 * $doc.id(id).title 提取文章的标题
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class SoftwareDocTemplateImpl extends VarPart {
    public SoftwareDocTemplateImpl() {

    }

    public String write(HttpServletRequest request, SoftwareDocumentDb doc) {
        BeanUtil bu = new BeanUtil();
        Object obj = null;
        if (field.equalsIgnoreCase("content")) {
            String pageNum = (String) ParamUtil.get(request, "CPages");
            if (!StrUtil.isNumeric(pageNum)) {
                pageNum = (String) request.getAttribute("CPages");
                if (pageNum == null)
                    pageNum = "1";
            }
            int pNum = Integer.parseInt(pageNum);
            Document dc = new Document();
            dc = dc.getDocument(doc.getDocId());
            if (pNum>dc.getPageCount())
                pNum = 1;
            obj = dc.getContent(pNum);
            return format(obj, props);
        }
        else if (field.equalsIgnoreCase("smallImg")) {
            if (!doc.getSmallImg().equals("")) {
                String rootPath = "";
                if (!Global.virtualPath.equals(""))
                    rootPath = "/" + Global.virtualPath;

                return "<img src='" + rootPath + "/" + doc.getSmallImg() + "'>";
            }
            else
                return "";
        }
        else if (field.equalsIgnoreCase("softUrl")) {
            String rootPath = "";
            if (!Global.virtualPath.equals(""))
                rootPath = "/" + Global.virtualPath;

            String[] ary = doc.getUrlAry();
            String str = "";
            int len = ary.length;
            for (int i = 0; i < len; i++) {
                str += "<a target=_blank href='" + rootPath + "/cms/plugin/software/download.jsp?softId=" + doc.getDocId() + "&urlId=" + i + "'>下载地址" + (i + 1) + "</a>&nbsp;&nbsp;&nbsp;&nbsp;";
            }

            return str;
        }
        else if (field.equalsIgnoreCase("fileIcon")) {
            String rootPath = "";
            if (!Global.virtualPath.equals(""))
                rootPath = "/" + Global.virtualPath;

            LogUtil.getLog(getClass()).info("write:id=" + doc.getDocId() + " doc=" + doc);

            return rootPath + "/images/fileicon/" + doc.getFileType().substring(1) + ".gif";
        }
        else {
            obj = bu.getProperty(doc, field);
            return format(obj, props);
        }
    }

    public String toString(HttpServletRequest request, List param) {
        if (keyName==null)
            throw new IllegalArgumentException("缺少属性值！");
        if (keyName.equalsIgnoreCase("id")) {
            // LogUtil.getLog(getClass()).info("toString:keyValue=" + keyValue);
            String kValue = parseKeyValueFromRequest(request);
            // LogUtil.getLog(getClass()).info("toString:kValue1=" + kValue);

            if (kValue.equals("")) {
                kValue = (String) request.getAttribute("id"); // 当添加修改文章自动生成静态页面时
            }

            // LogUtil.getLog(getClass()).info("toString:kValue2=" + kValue);

            if (kValue==null)
                kValue = keyValue;

            int id = Integer.parseInt(kValue);
            SoftwareDocumentDb doc = new SoftwareDocumentDb();
            doc = doc.getSoftwareDocumentDb(id);
            // LogUtil.getLog(getClass()).info("toString:id=" + id + " doc=" + doc + " isLoaded=" + doc.isLoaded());
            if (doc.isLoaded())
                return write(request, doc);
            else
                return ""; // "id=" + id + " is not found.";
        } else if (keyName.equalsIgnoreCase("dirCode")) {
            Leaf lf = new Leaf();
            lf = lf.getLeaf(keyValue);
            int id = lf.getDocID();
            SoftwareDocumentDb doc = new SoftwareDocumentDb();
            doc = doc.getSoftwareDocumentDb(id);
            return write(request, doc);
        } else
            return "Key " + keyName + " is invalid!";
    }

}

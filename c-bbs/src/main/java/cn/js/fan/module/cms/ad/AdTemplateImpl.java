package cn.js.fan.module.cms.ad;

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
public class AdTemplateImpl extends VarPart {
    public AdTemplateImpl() {

    }

    public String toString(HttpServletRequest request, List param) {
        if (keyName == null)
            throw new IllegalArgumentException("缺少属性值！");
        if (keyName.equalsIgnoreCase("id")) {
            // LogUtil.getLog(getClass()).info("toString:keyValue=" + keyValue);
            String kValue = null;
            if (request!=null) {
                kValue = parseKeyValueFromRequest(request);

                if (kValue.equals("")) {
                    kValue = (String) request.getAttribute("id"); // 当添加修改文章自动生成静态页面时
                }
            }
            if (kValue==null)
                kValue = keyValue;

            int id = StrUtil.toInt(kValue, -1);
            if (id!=-1) {
                Document doc = new Document();
                doc = doc.getDocument(id);
                dirCode = doc.getDirCode();
            }
            else
                dirCode = Leaf.ROOTCODE;
            return "<script src='" + Global.getRootPath() + "/js.jsp?var=ad&dirCode=" + dirCode + "&type=" + field + "'></script>";

            // LogUtil.getLog(getClass()).info("toString:id=" + id + " doc=" + doc);
            /*
            AdRender ad = new AdRender(dirCode, field);
            return ad.render(request);
            */
        }
        else if (keyName.equalsIgnoreCase("dirCode")) {
            String kValue = null;
            if (request != null) {
                kValue = parseKeyValueFromRequest(request);
            }
            if (kValue == null) {
                kValue = keyValue;
                Leaf lf = new Leaf();
                lf = lf.getLeaf(kValue);
                if (lf == null)
                    dirCode = Leaf.ROOTCODE;
                else
                    dirCode = kValue;
            } else
                dirCode = kValue;

            return "<script src='" + Global.getRootPath() + "/js.jsp?var=ad&dirCode=" + dirCode + "&type=" + field + "'></script>";
            /*
            AdRender ad = new AdRender(dirCode, field);
            return ad.render(request);
            */
        }
        else
            return "Key " + keyName + " is invalid!";
    }

    String dirCode;

}

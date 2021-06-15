package cn.js.fan.module.cms.template;

import com.cloudwebsoft.framework.template.VarPart;
import cn.js.fan.util.ParamUtil;
import com.cloudwebsoft.framework.util.BeanUtil;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import cn.js.fan.util.StrUtil;

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
public class RequestTemplateImpl extends VarPart {
    public RequestTemplateImpl() {
    }

    public String toString(HttpServletRequest request, List params) {
        if (field.startsWith("@")) {
            BeanUtil bu = new BeanUtil();
            return (String)bu.getProperty(request, field.substring(1));
        }
        else {
            String v = ParamUtil.get(request, field);
            if (!v.equals(""))
                return v;
            else {
                v = StrUtil.getNullStr((String)request.getAttribute(field));
                return v;
            }
        }
    }
}

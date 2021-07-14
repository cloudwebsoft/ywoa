package cn.js.fan.module.cms.template;

import com.cloudwebsoft.framework.template.VarPart;
import cn.js.fan.web.Global;
import javax.servlet.http.HttpServletRequest;
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
public class GlobalTemplateImpl extends VarPart {
    public GlobalTemplateImpl() {
    }

    public String toString(HttpServletRequest request, List params) {
        return getProperty(Global.getInstance());
    }
}

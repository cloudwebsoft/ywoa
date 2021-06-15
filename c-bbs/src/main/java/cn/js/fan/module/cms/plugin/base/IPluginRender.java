package cn.js.fan.module.cms.plugin.base;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.module.cms.DocContent;

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
public interface IPluginRender {
    public String RenderContent(HttpServletRequest request, DocContent dc);
}

package cn.js.fan.module.cms.plugin.software;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.module.cms.DocContent;
import cn.js.fan.module.cms.plugin.base.IPluginRender;

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
public class SoftwareRender implements IPluginRender {
    public SoftwareRender() {
    }

    /**
     * RenderContent
     *
     * @param request HttpServletRequest
     * @param dc DocContent
     * @return String
     * @todo Implement this cn.js.fan.module.cms.plugin.base.IPluginRender
     *   method
     */
    public String RenderContent(HttpServletRequest request, DocContent dc) {
        String content = dc.getContent();
        return content;
    }
}

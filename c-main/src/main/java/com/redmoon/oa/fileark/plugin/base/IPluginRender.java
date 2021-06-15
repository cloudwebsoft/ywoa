package com.redmoon.oa.fileark.plugin.base;

import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.fileark.DocContent;

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

package com.cloudwebsoft.framework.base;

import javax.servlet.http.HttpServletRequest;
import com.cloudwebsoft.framework.web.DomainUnit;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public interface IDomainDispatcher {
    public String getUrl(HttpServletRequest request, String domainField, DomainUnit du);
}

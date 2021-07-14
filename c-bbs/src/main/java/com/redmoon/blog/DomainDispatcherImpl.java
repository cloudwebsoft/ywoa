package com.redmoon.blog;

import com.cloudwebsoft.framework.base.IDomainDispatcher;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.web.DomainUnit;

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
public class DomainDispatcherImpl implements IDomainDispatcher {
    public DomainDispatcherImpl() {
    }

    public String getUrl(HttpServletRequest request, String domainField, DomainUnit du) {
        if (StrUtil.isNumeric(domainField)) {
            return StrUtil.format(du.getUrl(), new String[] {domainField});
        }
        UserConfigDb ucd = new UserConfigDb();
        long blogId = ucd.getBlogIdByDomain(domainField);
        return StrUtil.format(du.getUrl(), new String[] {""+blogId});
    }

}

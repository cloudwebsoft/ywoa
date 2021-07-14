package com.redmoon.oa.ui;

import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.person.UserDesktopSetupDb;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public interface IDesktopUnit {
    public String display(HttpServletRequest request, UserDesktopSetupDb uds);
    public String getPageList(HttpServletRequest request, UserDesktopSetupDb uds);
}

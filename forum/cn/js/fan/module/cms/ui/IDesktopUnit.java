package cn.js.fan.module.cms.ui;

import javax.servlet.http.HttpServletRequest;

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
    public String display(HttpServletRequest request, DesktopItemDb di);
    public String getPageList(HttpServletRequest request, DesktopItemDb di);
}

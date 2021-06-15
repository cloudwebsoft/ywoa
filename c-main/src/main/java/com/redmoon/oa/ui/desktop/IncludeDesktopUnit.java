package com.redmoon.oa.ui.desktop;

import javax.servlet.http.HttpServletRequest;

import com.redmoon.oa.person.UserDesktopSetupDb;
import com.redmoon.oa.ui.DesktopMgr;
import com.redmoon.oa.ui.IDesktopUnit;

public class IncludeDesktopUnit implements IDesktopUnit {
    public IncludeDesktopUnit() {
    }

    @Override
    public String getPageList(HttpServletRequest request,
                              UserDesktopSetupDb uds) {
        DesktopMgr dm = new DesktopMgr();
        com.redmoon.oa.ui.DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());
        String url = du.getPageList();
        return url;
    }

    @Override
    public String display(HttpServletRequest request, UserDesktopSetupDb uds) {
        DesktopMgr dm = new DesktopMgr();
        String parentId = (String) request.getAttribute("parentId");
        if (parentId != null) {
            com.redmoon.oa.ui.DesktopUnit du = dm.getDesktopUnit(uds.getModuleCode());
        /*
        String path = "http://" + request.getServerName() + ":" +
                      request.getServerPort() + request.getContextPath() +
                      "/" + du.getPageShow() + "?id=" + uds.getId();
        */

            //String path = Global.getFullRootPath() + "/" + du.getPageShow() + "?id=" + uds.getId();

            // return "<script>$(function() { loadDesktopUnit('" + request.getContextPath() + "/" + du.getPageShow() + "?id=" + uds.getId() + "','" + parentId + "','drag_" + uds.getId() + "'); })</script>";
            return "<script>loadDesktopUnit('" + request.getContextPath() + "/" + du.getPageShow() + "?id=" + uds.getId() + "','" + parentId + "','drag_" + uds.getId() + "')</script>";
        } else {
            return "";
        }
        //return NetUtil.gather(request, "utf-8", path);
    }
}

package cn.js.fan.module.cms.template;

import cn.js.fan.module.cms.ui.DesktopItemDb;
import com.cloudwebsoft.framework.template.VarPart;
import cn.js.fan.module.cms.ui.DesktopMgr;
import cn.js.fan.module.cms.ui.DesktopUnit;
import cn.js.fan.module.cms.ui.IDesktopUnit;
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
public class DesktopTemplateImpl extends VarPart {
    public DesktopTemplateImpl() {
    }

    public String toString(HttpServletRequest request, List params) {
        if (getKeyValue()==null)
            throw new IllegalArgumentException(name + "'s getKeyValue() is wanted");
        String[] ary = getKeyValue().split(",");
        if (ary==null || ary.length!=2) {
            throw new IllegalArgumentException("Desktop param " + getKeyValue() + " is invalid!");
        }
        DesktopItemDb di = new DesktopItemDb();
        di = di.getDesktopItemDb(ary[0].trim(), ary[1].trim());
        if (di==null) {
            return "Desktop item is not found at position " + getKeyValue();
        }
        else {
            if (field.equals("title")) {
                return di.getTitle();
            }
            else if (field.equals("content")) {
                DesktopMgr dm = new DesktopMgr();
                DesktopUnit du = dm.getDesktopUnit(di.getModuleCode());
                if (du==null) {
                    throw new IllegalArgumentException("Desktop item moduleCode=" + di.getModuleCode() + " is not found in config XML file");
                }
                IDesktopUnit idu = du.getIDesktopUnit();
                if (idu==null) {
                    throw new IllegalArgumentException("Desktop item moduleCode=" + di.getModuleCode() + " className=" + du.getClassName() + " is not an valid interface of IDesktopUnit");
                }
                return idu.display(request, di);
            }
            else if (field.equals("pageList")) {
                DesktopMgr dm = new DesktopMgr();
                DesktopUnit du = dm.getDesktopUnit(di.getModuleCode());
                IDesktopUnit idu = du.getIDesktopUnit();
                return idu.getPageList(request, di);
            }
            else
                return "";
        }
    }
}

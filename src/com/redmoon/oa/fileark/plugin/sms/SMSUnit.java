package com.redmoon.oa.fileark.plugin.sms;

import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.fileark.plugin.base.IPluginUnit;
import com.redmoon.oa.fileark.plugin.base.IPluginDocumentAction;
import com.redmoon.oa.fileark.plugin.base.IPluginUI;
import com.redmoon.oa.fileark.Leaf;


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
public class SMSUnit implements IPluginUnit{
    public static final String code = "sms";
    public SMSUnit() {
    }

    public IPluginDocumentAction getDocumentAction() {
        return new SMSDocumentAction();
    }

    public IPluginUI getUI(HttpServletRequest request) {
        return new SMSUI(request);
    }

    public boolean isPluginDir(String dirCode) {
        Leaf lf = new Leaf();
        lf = lf.getLeaf(dirCode);
        if (lf.getPluginCode().equals(code))
            return true;
        else
            return false;
    }
}

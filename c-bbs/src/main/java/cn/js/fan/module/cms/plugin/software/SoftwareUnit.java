package cn.js.fan.module.cms.plugin.software;

import cn.js.fan.module.cms.Leaf;
import cn.js.fan.module.cms.plugin.base.IPluginDocumentAction;
import cn.js.fan.module.cms.plugin.base.IPluginUnit;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.module.cms.plugin.base.IPluginUI;
import cn.js.fan.module.cms.plugin.base.IPluginDocument;

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
public class SoftwareUnit implements IPluginUnit{
    public static final String code = "software";
    public SoftwareUnit() {
    }

    public IPluginDocumentAction getDocumentAction() {
        return new SoftwareDocumentAction();
    }

    public IPluginUI getUI(HttpServletRequest request) {
        return new SoftwareUI(request);
    }

    public boolean isPluginDir(String dirCode) {
        Leaf lf = new Leaf();
        lf = lf.getLeaf(dirCode);
        if (lf.getPluginCode().equals(code))
            return true;
        else
            return false;
    }

    public IPluginDocument getDocument(int docId) {
        SoftwareDocumentDb idd = new SoftwareDocumentDb();
        idd = idd.getSoftwareDocumentDb(docId);
        return idd;
    }

}

package cn.js.fan.module.cms.plugin.wiki;

import com.redmoon.oa.fileark.Leaf;
import com.redmoon.oa.fileark.plugin.base.IPluginDocumentAction;
import com.redmoon.oa.fileark.plugin.base.IPluginUnit;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.fileark.plugin.base.IPluginUI;
import com.redmoon.oa.fileark.plugin.base.IPluginDocument;

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
public class WikiUnit implements com.redmoon.oa.fileark.plugin.base.IPluginUnit{
    public static final String code = "wiki";
    public WikiUnit() {
    }

    public IPluginDocumentAction getDocumentAction() {
        return new WikiDocumentAction();
    }

    public IPluginUI getUI(HttpServletRequest request) {
        return new WikiUI(request);
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
        WikiDocumentDb idd = new WikiDocumentDb();
        idd = idd.getWikiDocumentDb(docId);
        return idd;
    }

}

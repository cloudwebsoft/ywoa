package cn.js.fan.module.cms.plugin.software;

import javax.servlet.http.HttpServletRequest;

import java.util.Vector;
import java.util.Iterator;
import cn.js.fan.module.cms.plugin.base.IPluginViewAddDocument;
import cn.js.fan.module.cms.plugin.base.UIAddDocument;
import cn.js.fan.module.cms.Leaf;
import cn.js.fan.module.cms.Document;
import cn.js.fan.module.cms.plugin.base.IPluginViewEditDocument;

public class SoftwareViewEditDocument implements IPluginViewEditDocument {
    public final String FORM_EDIT = "FORM_EDIT";

    String dirCode;
    HttpServletRequest request;
    Document doc;

    public SoftwareViewEditDocument(HttpServletRequest request, Document doc) {
        this.request = request;
        this.dirCode = doc.getDirCode();
        this.doc = doc;
        init();
    }

    public void init() {
        formElement = SoftwareSkin.LoadString(request, FORM_EDIT);
    }

    public String render(int position) {
        String str = "";
        switch (position) {
        case UIAddDocument.POS_TITLE:
            str += SoftwareSkin.LoadString(request, "addDocumentTitle");
            break;
        case UIAddDocument.POS_FORM_ELEMENT:
            str = getFormElement();
            break;
        default:
            break;
        }
        return str;
    }

    public void setFormElement(String formElement) {
        this.formElement = formElement;
    }

    public String getFormElement() {
        return formElement;
    }

    private String formElement;
}

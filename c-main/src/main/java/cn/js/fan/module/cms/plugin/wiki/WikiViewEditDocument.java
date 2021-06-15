package cn.js.fan.module.cms.plugin.wiki;

import javax.servlet.http.HttpServletRequest;

import com.redmoon.oa.fileark.Document;
import com.redmoon.oa.fileark.plugin.base.IPluginViewEditDocument;
import com.redmoon.oa.fileark.plugin.base.UIAddDocument;

public class WikiViewEditDocument implements IPluginViewEditDocument {
    public final String FORM_EDIT = "FORM_EDIT";

    String dirCode;
    HttpServletRequest request;
    Document doc;

    public WikiViewEditDocument(HttpServletRequest request, Document doc) {
        this.request = request;
        this.dirCode = doc.getDirCode();
        this.doc = doc;
        init();
    }

    public void init() {
        formElement = WikiSkin.LoadString(request, FORM_EDIT);
    }

    public String render(int position) {
        String str = "";
        switch (position) {
        case UIAddDocument.POS_TITLE:
            str += WikiSkin.LoadString(request, "addDocumentTitle");
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

package com.redmoon.oa.fileark.plugin.sms;

import javax.servlet.http.HttpServletRequest;

import java.util.Vector;
import java.util.Iterator;
import com.redmoon.oa.fileark.plugin.base.IPluginViewAddDocument;
import com.redmoon.oa.fileark.plugin.base.UIAddDocument;
import com.redmoon.oa.fileark.Leaf;
import com.redmoon.oa.fileark.Document;
import com.redmoon.oa.fileark.plugin.base.IPluginViewEditDocument;

public class SMSViewEditDocument implements IPluginViewEditDocument {
    public final String FORM_EDIT = "FORM_EDIT";

    String dirCode;
    HttpServletRequest request;
    Document doc;

    public SMSViewEditDocument(HttpServletRequest request, Document doc) {
        this.request = request;
        this.dirCode = doc.getDirCode();
        this.doc = doc;
        init();
    }

    public void init() {
        formElement = SMSSkin.LoadString(request, FORM_EDIT);
    }

    public String render(int position) {
        String str = "";
        switch (position) {
        case UIAddDocument.POS_TITLE:
            str += SMSSkin.LoadString(request, "addDocumentTitle");
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

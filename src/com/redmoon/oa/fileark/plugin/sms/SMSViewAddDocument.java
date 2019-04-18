package com.redmoon.oa.fileark.plugin.sms;

import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.fileark.plugin.base.IPluginViewAddDocument;
import com.redmoon.oa.fileark.plugin.base.UIAddDocument;

public class SMSViewAddDocument implements IPluginViewAddDocument {
    public final String FORM_ADD = "FORM_ADD";

    String dirCode;
    HttpServletRequest request;

    public SMSViewAddDocument(HttpServletRequest request, String dirCode) {
        this.request = request;
        this.dirCode = dirCode;
        init();
    }

    public void init() {
        formElement = SMSSkin.LoadString(request, FORM_ADD);
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

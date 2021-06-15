package cn.js.fan.module.cms.plugin.img;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.module.cms.plugin.base.IPluginViewAddDocument;
import cn.js.fan.module.cms.plugin.base.UIAddDocument;

public class ImgViewAddDocument implements IPluginViewAddDocument {
    public final String FORM_ADD = "FORM_ADD";

    String dirCode;
    HttpServletRequest request;

    public ImgViewAddDocument(HttpServletRequest request, String dirCode) {
        this.request = request;
        this.dirCode = dirCode;
        init();
    }

    public void init() {
        formElement = ImgSkin.LoadString(request, FORM_ADD);
    }

    public String render(int position) {
        String str = "";
        switch (position) {
        case UIAddDocument.POS_TITLE:
            str += ImgSkin.LoadString(request, "addDocumentTitle");
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

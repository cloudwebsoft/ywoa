package com.redmoon.forum.plugin.present;

import javax.servlet.http.HttpServletRequest;

import com.redmoon.forum.plugin.base.IPluginViewAddMsg;
import com.redmoon.forum.plugin.base.UIAddMsg;
import com.redmoon.forum.plugin.BoardDb;

public class PresentViewAddMsg implements IPluginViewAddMsg {
    public final String FORM_ADD = "FORM_ADD";

    String boardCode;
    HttpServletRequest request;

    public PresentViewAddMsg(HttpServletRequest request, String boardCode) {
        this.request = request;
        this.boardCode = boardCode;
        init();
    }

    public void init() {
        formElement = PresentSkin.LoadString(request, FORM_ADD);
    }

    public String render(int position) {
        String str = "";
        switch (position) {
        case UIAddMsg.POS_TITLE:
            str += PresentSkin.LoadString(request, "addMsgTitle");
            break;
        case UIAddMsg.POS_FORM_ELEMENT:
            str = getFormElement();
            break;
        default:
            break;
        }
        return str;
    }

    public boolean IsPluginBoard() {
        BoardDb sb = new BoardDb();
        return sb.isPluginBoard(PresentUnit.code, boardCode);
    }

    public void setFormElement(String formElement) {
        this.formElement = formElement;
    }

    public String getFormElement() {
        return formElement;
    }

    private String formElement;
}

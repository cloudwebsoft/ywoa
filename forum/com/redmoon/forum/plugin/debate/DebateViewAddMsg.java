package com.redmoon.forum.plugin.debate;

import javax.servlet.http.HttpServletRequest;

import com.redmoon.forum.plugin.base.IPluginViewAddMsg;
import com.redmoon.forum.plugin.base.UIAddMsg;

public class DebateViewAddMsg implements IPluginViewAddMsg {
    public final String FORM_ADD = "FORM_ADD";

    String boardCode;
    HttpServletRequest request;

    public DebateViewAddMsg(HttpServletRequest request, String boardCode) {
        this.request = request;
        this.boardCode = boardCode;
        init();
    }

    public void init() {
        formElement = DebateSkin.LoadString(request, FORM_ADD);
    }

    public String render(int position) {
        String str = "";
        switch (position) {
        case UIAddMsg.POS_TITLE:
            str += DebateSkin.LoadString(request, "addMsgTitle");
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
        DebateUnit au = new DebateUnit();
        return au.isPluginBoard(boardCode);
    }

    public void setFormElement(String formElement) {
        this.formElement = formElement;
    }

    public String getFormElement() {
        return formElement;
    }

    private String formElement;
}

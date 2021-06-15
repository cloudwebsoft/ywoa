package com.redmoon.forum.plugin.dig;

import javax.servlet.http.*;

import com.redmoon.forum.plugin.base.*;
import org.apache.log4j.*;


public class DigViewAddReply implements IPluginViewAddReply {
    HttpServletRequest request;
    long msgRootId;

    Logger logger = Logger.getLogger(this.getClass().getName());

    public DigViewAddReply(HttpServletRequest request, String boardCode, long msgRootId) {
        this.request = request;
        this.boardCode = boardCode;
        this.msgRootId = msgRootId;
        init();
    }

    public String render(int position) {
        String str = "";
        switch (position) {
        case UIAddReply.POS_FORM_NOTE:
            str = getFormNote();
            break;
        case UIAddReply.POS_FORM_ELEMENT:
            str = getFormElement();
            break;
        default:
        }
        return str;
    }

    public void setBoardCode(String boardCode) {
        this.boardCode = boardCode;
    }

    public void setFormNote(String formNote) {
        this.formNote = formNote;
    }

    public void setFormElement(String formElement) {
        this.formElement = formElement;
    }

    public String getBoardCode() {
        return boardCode;
    }

    public boolean IsPluginBoard() {
        DigUnit au = new DigUnit();
        return au.isPluginBoard(boardCode);
    }

    public String getFormElement() {
        return formElement;
    }

    public void init() {
        formElement = "";
    }

    public String getFormNote() {
        return formNote;
    }

    private String boardCode;
    private String formNote = "";
    private String formElement = "";
}

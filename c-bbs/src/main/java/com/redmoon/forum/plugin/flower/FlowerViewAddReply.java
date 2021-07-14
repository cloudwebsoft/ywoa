package com.redmoon.forum.plugin.flower;

import javax.servlet.http.HttpServletRequest;

import com.redmoon.forum.plugin.base.IPluginViewAddReply;
import com.redmoon.forum.plugin.base.UIAddReply;
import org.apache.log4j.Logger;
import com.redmoon.forum.plugin.BoardDb;

public class FlowerViewAddReply implements IPluginViewAddReply {
    HttpServletRequest request;
    long msgRootId;

    Logger logger = Logger.getLogger(this.getClass().getName());

    public FlowerViewAddReply(HttpServletRequest request, String boardCode, long msgRootId) {
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
        BoardDb sb = new BoardDb();
        return sb.isPluginBoard(FlowerUnit.code, boardCode);
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

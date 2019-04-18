package com.redmoon.oa.project.forum;

import javax.servlet.http.HttpServletRequest;

import com.redmoon.forum.plugin.base.IPluginViewEditMsg;
import com.redmoon.forum.plugin.base.UIEditMsg;
import com.redmoon.forum.MsgDb;

public class ProjectViewEditMsg implements IPluginViewEditMsg {
    HttpServletRequest request;
    public final String FORM_EDIT = "FORM_ADD";

    public ProjectViewEditMsg(HttpServletRequest request, String boardCode, long msgId) {
        this.request = request;
        this.boardCode = boardCode;
        this.msgId = msgId;
        init();
    }

    public void init() {
        formElement = "";
        formNote = ProjectSkin.LoadString(request, "LABEL_EDIT_MSG");
        // 脚本
        // formNote += "<script src='plugin/" + ProjectSkin.code + "/script.js'></script>\n";

        MsgDb md = new MsgDb();
        md = md.getMsgDb(msgId);
        // 如果该贴不是根贴
        if (md.getReplyid()==-1) {



        }
    }

    /**
     * IsPluginBoard
     *
     * @return boolean
     * @todo Implement this com.redmoon.forum.plugin.base.IPluginViewShowMsg
     *   method
     */
    public boolean IsPluginBoard() {
        ProjectUnit au = new ProjectUnit();
        return au.isPluginBoard(boardCode);
    }

    /**
     * render
     *
     * @param position int
     * @return String
     * @todo Implement this com.redmoon.forum.plugin.base.IPluginView method
     */
    public String render(int position) {
        String str = "";
        switch (position) {
        case UIEditMsg.POS_FORM_NOTE:
            str = getFormNote();
            break;
        case UIEditMsg.POS_FORM_ELEMENT:
            str = getFormElement();
            break;
        default:
        }
        return str;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public void setBoardCode(String boardCode) {
        this.boardCode = boardCode;
    }

    public void setFormElement(String formElement) {
        this.formElement = formElement;
    }

    public void setFormNote(String formNote) {
        this.formNote = formNote;
    }

    public long getMsgId() {
        return msgId;
    }

    public String getBoardCode() {
        return boardCode;
    }

    public String getFormElement() {
        return formElement;
    }

    public String getFormNote() {
        return formNote;
    }

    private long msgId;
    private String boardCode;
    private String formElement;
    private String formNote;
}

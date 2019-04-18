package com.redmoon.forum.plugin.sweet;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.db.PrimaryKey;
import com.redmoon.forum.Privilege;
import cn.js.fan.util.ParamUtil;
import com.redmoon.forum.plugin.base.IPluginViewEditMsg;
import com.redmoon.forum.plugin.base.UIAddReply;
import com.redmoon.forum.plugin.base.UIEditMsg;
import com.redmoon.forum.MsgDb;

public class SweetViewEditMsg implements IPluginViewEditMsg {
    HttpServletRequest request;

    public SweetViewEditMsg(HttpServletRequest request, String boardCode, long msgId) {
        this.request = request;
        this.boardCode = boardCode;
        this.msgId = msgId;
        init();
    }

    public void init() {
        formElement = "";
        formNote = SweetSkin.LoadString(request, "LABEL_EDIT_MSG");

        SweetMsgDb sw = new SweetMsgDb();
        sw = sw.getSweetMsgDb(msgId);
        MsgDb md = new MsgDb();
        md = md.getMsgDb(msgId);
        // 如果该贴不是根贴，则写secretLevel修改的表单，根贴默认为公共可见，不可更改
        if (md.getReplyid()!=-1)
            formElement += SweetViewAddReply.getFormSecretLevel(request, sw.getSecretLevel());
    }

    /**
     * IsPluginBoard
     *
     * @return boolean
     * @todo Implement this com.redmoon.forum.plugin.base.IPluginViewShowMsg
     *   method
     */
    public boolean IsPluginBoard() {
        SweetUnit sut = new SweetUnit();
        return sut.isPluginBoard(boardCode);
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

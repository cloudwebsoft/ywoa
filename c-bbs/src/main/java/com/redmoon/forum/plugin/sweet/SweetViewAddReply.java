package com.redmoon.forum.plugin.sweet;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.db.PrimaryKey;
import com.redmoon.forum.Privilege;
import com.redmoon.forum.plugin.base.IPluginViewAddReply;
import com.redmoon.forum.plugin.base.UIAddReply;
import org.apache.log4j.Logger;
import cn.js.fan.util.ParamUtil;

public class SweetViewAddReply implements IPluginViewAddReply {
    HttpServletRequest request;
    long msgRootId;

    public static final String FORM_ACCEPT_APPLY = "sweet_AcceptApply";
    public static final String FORM_DECLINE_APPLY = "sweet_DeclineApply";

    public static final String FORM_APPLY_MARRY = "sweet_ApplyMarry";
    public static final String FORM_ACCEPT_APPLY_MARRY = "sweet_AcceptApplyMarry";
    public static final String FORM_DECLINE_APPLY_MARRY = "sweet_DeclineApplyMarry";

    Logger logger = Logger.getLogger(this.getClass().getName());

    public SweetViewAddReply(HttpServletRequest request, String boardCode, long msgRootId) {
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
        SweetUnit su = new SweetUnit();
        return su.isPluginBoard(boardCode);
    }

    public String getFormElement() {
        return formElement;
    }

    public void init() {
        formElement = "";

        SweetDb sd = new SweetDb();
        sd = (SweetDb)sd.getObjectDb(new Long(msgRootId));
        String name = Privilege.getUser(request);

        SweetUserDb su = new SweetUserDb();
        PrimaryKey pk = su.getPrimaryKey();
        pk.setKeyValue("msgRootId", new Long(msgRootId));
        pk.setKeyValue("name", name);
        su = (SweetUserDb) su.getObjectDb(pk.getKeys());

        // logger.info("name=" + name + "SweetDb.name=" + sd.getName());
        if (name.equals(sd.getName())) {
            formNote = SweetSkin.LoadString(request, "addReplyNoteOwner");
        }
        else {
            String str = "";
            // 如果在sweet用户表中已记录
            if (su.isLoaded()) {
                switch (su.getType()) {
                case SweetUserDb.TYPE_APPLIER: // 如果正在申请中，则可继续发申请贴
                    str = SweetSkin.LoadString(request, "noteApplier");
                    formElement = "<input type=hidden name=sweetAction value='apply'><input type=checkbox name=tempSweetAction value='apply' disabled checked>" + SweetSkin.LoadString(request, "addReplyLableApply");
                    break;
                case SweetUserDb.TYPE_PERSUATER:
                    str = SweetSkin.LoadString(request, "addReplyLableUserPersuate");
                    break;
                case SweetUserDb.TYPE_SPOUSE:
                    str = SweetSkin.LoadString(request, "addReplyLableUserSpouse");
                    break;
                default:
                }
            } else { // 如果在用户表中未记录，则说明为申请贴
                str = SweetSkin.LoadString(request, "addReplyNoteApply");
                formElement = "<input type=hidden name=sweetAction value='" + SweetMsgAction.ACTION_APPLY + "'><input type=checkbox name=tempSweetAction value='apply' disabled checked>" + SweetSkin.LoadString(request, "addReplyLableApply");
            }
            formNote = str;

        }
        // 根据pluginForm置入相应的表单元素
        String pluginForm = ParamUtil.get(request, "pluginForm");
        // logger.info("pluginForm" + pluginForm);
        if (pluginForm.equals(this.FORM_ACCEPT_APPLY)) { // 接受申请，成为追求者
            formElement +=
                    "<input type=hidden name='sweetAction' value='" + SweetMsgAction.ACTION_ACCEPT_APPLY + "'>" +
                    "<input type=checkbox name=tempSweetAction disabled checked>" +
                    SweetSkin.LoadString(request, "BUTTON_ACCEPT");
            formElement += getFormSecretLevel(request, SweetMsgDb.SECRET_LEVEL_MSG_USER_REPLIED);
        }
        else if (pluginForm.equals(this.FORM_DECLINE_APPLY)) { // 拒绝申请成为追求者
            formElement += "<input type=hidden name='sweetAction' value='" + SweetMsgAction.ACTION_DECLINE_APPLY + "'><input type=checkbox name=tempSweetAction disabled checked>" +
                    SweetSkin.LoadString(request, "BUTTON_DECLINE");
            formElement += getFormSecretLevel(request, SweetMsgDb.SECRET_LEVEL_MSG_USER_REPLIED);
        }
        else if (pluginForm.equals(this.FORM_APPLY_MARRY)) { // 申请结婚表单
            // 如果回复者不是楼主
            if (!sd.getName().equals(name)) {
                    // 如果在sweet用户表中已记录
                    if (su.getType()==su.TYPE_SPOUSE)
                        formElement += SweetSkin.LoadString(request, "addReplyLableAlreadyMarry");
                    else if (su.getType() == su.TYPE_PERSUATER) {
                        formElement +=
                                "<input type=hidden name='sweetAction' value='" +
                                SweetMsgAction.ACTION_APPLY_MARRY +
                                "'><input type=checkbox name=tempSweetAction disabled checked>" +
                                SweetSkin.LoadString(request,
                                                     "addReplyLableApplyMarry");
                    }
                    else
                        formElement += "&nbsp;" + SweetSkin.LoadString(request, "addReplyLableShouldApplyPersuate");    // 警告，您必须先成为追求者
                }
            // 仅楼主可见
            formElement += getFormSecretLevel(request, SweetMsgDb.SECRET_LEVEL_MSG_OWNER);
        }
        else if (pluginForm.equals(this.FORM_ACCEPT_APPLY_MARRY)) { // 接受求婚
            formElement +=
                    "<input type=hidden name='sweetAction' value='" + SweetMsgAction.ACTION_ACCEPT_MARRY + "'>" +
                    "<input type=checkbox name=tempSweetAction disabled checked>" +
                    SweetSkin.LoadString(request, "BUTTON_ACCEPT");
            formElement += getFormSecretLevel(request, SweetMsgDb.SECRET_LEVEL_MSG_USER_REPLIED);
        }
        else if (pluginForm.equals(this.FORM_DECLINE_APPLY_MARRY)) { // 拒绝申请成为追求者
            formElement += "<input type=hidden name='sweetAction' value='" + SweetMsgAction.ACTION_DECLINE_MARRY + "'><input type=checkbox name=tempSweetAction disabled checked>" +
                    SweetSkin.LoadString(request, "BUTTON_DECLINE");
            formElement += getFormSecretLevel(request, SweetMsgDb.SECRET_LEVEL_MSG_USER_REPLIED);
        }
        else
            formElement += getFormSecretLevel(request, SweetMsgDb.SECRET_LEVEL_FORUM_PUBLIC);

    }

    public String getFormNote() {
        return formNote;
    }

    /**
     * 取得对应于贴子秘级的表单元素
     * @param defaultOpt in 默认为SECRET_LEVEL_FORUM_PUBLIC
     * @return String
     */
    public static String getFormSecretLevel(HttpServletRequest request, int defaultLevel) {
        String str = "";
        if (defaultLevel==SweetMsgDb.SECRET_LEVEL_FORUM_PUBLIC)
            str += "<input type=radio checked name=secretLevel value=" + SweetMsgDb.SECRET_LEVEL_FORUM_PUBLIC + ">" + SweetMsgDb.getSecretLevelDesc(request, SweetMsgDb.SECRET_LEVEL_FORUM_PUBLIC);
        else
            str += "<input type=radio name=secretLevel value=" + SweetMsgDb.SECRET_LEVEL_FORUM_PUBLIC + ">" + SweetMsgDb.getSecretLevelDesc(request, SweetMsgDb.SECRET_LEVEL_FORUM_PUBLIC);
        if (defaultLevel==SweetMsgDb.SECRET_LEVEL_MSG_USER)
            str += "<input type=radio checked name=secretLevel value=" + SweetMsgDb.SECRET_LEVEL_MSG_USER + ">" + SweetMsgDb.getSecretLevelDesc(request, SweetMsgDb.SECRET_LEVEL_MSG_USER);
        else
            str += "<input type=radio name=secretLevel value=" + SweetMsgDb.SECRET_LEVEL_MSG_USER + ">" + SweetMsgDb.getSecretLevelDesc(request, SweetMsgDb.SECRET_LEVEL_MSG_USER);
        if (defaultLevel==SweetMsgDb.SECRET_LEVEL_MSG_USER_REPLIED)
            str += "<input type=radio checked name=secretLevel value=" + SweetMsgDb.SECRET_LEVEL_MSG_USER_REPLIED + ">" + SweetMsgDb.getSecretLevelDesc(request, SweetMsgDb.SECRET_LEVEL_MSG_USER_REPLIED);
        else
            str += "<input type=radio name=secretLevel value=" + SweetMsgDb.SECRET_LEVEL_MSG_USER_REPLIED + ">" + SweetMsgDb.getSecretLevelDesc(request, SweetMsgDb.SECRET_LEVEL_MSG_USER_REPLIED);
        if (defaultLevel==SweetMsgDb.SECRET_LEVEL_MSG_OWNER)
            str += "<input type=radio checked name=secretLevel value=" + SweetMsgDb.SECRET_LEVEL_MSG_OWNER + ">" + SweetMsgDb.getSecretLevelDesc(request, SweetMsgDb.SECRET_LEVEL_MSG_OWNER);
        else
            str += "<input type=radio name=secretLevel value=" + SweetMsgDb.SECRET_LEVEL_MSG_OWNER + ">" + SweetMsgDb.getSecretLevelDesc(request, SweetMsgDb.SECRET_LEVEL_MSG_OWNER);
        return str;
    }

    private String boardCode;
    private String formNote;
    private String formElement = "";
}

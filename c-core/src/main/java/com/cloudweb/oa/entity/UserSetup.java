package com.cloudweb.oa.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author fgf
 * @since 2020-02-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UserSetup extends Model<UserSetup> {

    private static final long serialVersionUID = 1L;

    @TableId("USER_NAME")
    private String userName;

    @TableField("MESSAGE_TO_DEPT")
    private String messageToDept;

    @TableField("MESSAGE_TO_USERGROUP")
    private String messageToUsergroup;

    @TableField("MESSAGE_TO_USERROLE")
    private String messageToUserrole;

    @TableField("MESSAGE_TO_MAX_USER")
    private Integer messageToMaxUser;

    @TableField("MESSAGE_USER_MAX_COUNT")
    private Integer messageUserMaxCount;

    @TableField("IS_MSG_WIN_POPUP")
    private Boolean isMsgWinPopup;

    @TableField("IS_CHAT_SOUND_PLAY")
    private Boolean isChatSoundPlay;

    @TableField("IS_CHAT_ICON_SHOW")
    private Boolean isChatIconShow;

    private Integer isMessageSoundPlay;

    private String skinCode;

    private String weatherCode;

    private String clockCode;

    private String calendarCode;

    private Integer isWebedit;

    private Long msgSpaceAllowed;

    private Long msgSpaceUsed;

    private Integer uiMode;

    private String wallpaper;

    private Integer isShowSidebar;

    private String mydesktopProp;

    private LocalDateTime lastMsgNotifyTime;

    private String emailName;

    private String emailPwd;

    private String isMsgChat;

    private String local;

    /**
     * 1代表"绑定"，0代表"解绑"
     */
    private Integer isBindMobile;

    private String keyId;

    /**
     * 1：android  2 ：ios   0 :没有使用过手机端app
     */
    private Integer client;

    /**
     * 手机令牌
     */
    private String token;

    private String myleaders;

    private Integer menuMode;

    private LocalDateTime agreeDate;

    private String cid;

    @Override
    protected Serializable pkVal() {
        return this.userName;
    }

}

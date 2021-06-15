package com.redmoon.weixin.enums;

public class Enum {
    /**
     * 消息类型
     */
    public enum emMsgType {
        ;
        public final static String  emEvent = "event";//事件
    }

    public enum  emEvent{
        ;
        public final static String emChangeContact = "change_contact";//修改联系人

    }

    public enum emChangeType{
        ;
        public final static String emCreateUser = "create_user";//创建用户
        public final static String emUpdateUser = "update_user";//更新用户
        public final static String emDeleteUser = "delete_user";//删除用户
        public final static String emCreateParty = "create_party";//创建部门
        public final static String emUpdateParty ="update_party";//更新部门
        public final static String  emDeleteParty = "delete_party";//删除部门事件
    }

    public enum emWXChangeKey{
        ;
        public final static String emMsgType = "MsgType";//创建用户
        public final static String emEvent = "Event";//更新用户
        public final static String emChangeType = "ChangeType";//删除用户
    }



}

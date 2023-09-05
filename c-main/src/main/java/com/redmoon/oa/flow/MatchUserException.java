package com.redmoon.oa.flow;

import cn.js.fan.util.ErrMsgException;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class MatchUserException extends Exception {
    public static final int TYPE_MULTIDEPT = 0;

    int type = TYPE_MULTIDEPT;

    public MatchUserException() {
    }

    public MatchUserException(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public String getTypeDesc() {
        if (type==TYPE_MULTIDEPT) {
            return "用户目前处于多个部门中，不支持关联到组织机构的自动匹配人员方式！";
        }
        else {
            return "";
        }
    }

    @Override
    public String getMessage() {
        return "请先选择部门";
    }

}

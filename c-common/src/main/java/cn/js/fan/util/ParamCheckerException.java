package cn.js.fan.util;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ParamCheckerException extends ErrMsgException {
    public static int TYPE_PARAM_NOT_SET_IN_FORM_RULE = 0;
    public int type = 0;

    public ParamCheckerException(String msg, int type) {
        super(msg);
        this.type = type;
    }

    public int getType() {
        return type;
    }
}

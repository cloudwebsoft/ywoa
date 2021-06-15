package cn.js.fan.util;

import java.util.Vector;
import java.util.Iterator;

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
public class CheckErrException extends Exception {
    Vector msgs;

    public CheckErrException(Vector msgs) {
        this.msgs = msgs;
    }

    public String getMessage() {
        return getMessage(false);
    }

    public String getMessage(boolean isHtml) {
        String str = "";
        Iterator ir = msgs.iterator();
        while (ir.hasNext()) {
            if (str.equals(""))
                str = (String) ir.next();
            else
                str += "\\r" + (String) ir.next();
        }
        if (isHtml)
            str = StrUtil.toHtml(str);
        return str;
    }
}

package com.cloudwebsoft.framework.template;

import com.cloudwebsoft.framework.template.plugin.PluginMgr;
import java.util.List;


/**
 * <p>Title: 模板解析器中的辅助标签</p>
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
public class Token {
    public static final int NONE = 0;
    public static final int VAR = 1; // 变量
    public static final int BEGIN = 2;
    public static final int FIELD = 3;
    public static final int END = 4;

    public static final int hasVar = 10;

    public Token() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public String name;
    public int type = NONE;
    public List posPairs = null;

}

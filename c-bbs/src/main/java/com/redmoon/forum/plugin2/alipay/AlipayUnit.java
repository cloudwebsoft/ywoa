package com.redmoon.forum.plugin2.alipay;

import com.redmoon.forum.plugin2.base.IPlugin2Unit;
import com.redmoon.forum.plugin2.base.IRender;
import com.redmoon.forum.plugin.base.IPluginMsgAction;

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
public class AlipayUnit implements IPlugin2Unit {
    public IRender getRender() {
        return new AlipayRender();
    }

    public IPluginMsgAction getMsgAction() {
        return new AlipayMsgAction();
    }
}

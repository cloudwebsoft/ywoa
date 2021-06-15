package com.redmoon.oa.fileark.plugin.base;

import cn.js.fan.util.ErrMsgException;
import com.redmoon.oa.flow.WorkflowActionDb;

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
public interface IPluginWorkflowOperater {
    public void OnStatusChange(int status, WorkflowActionDb lastAction);
}

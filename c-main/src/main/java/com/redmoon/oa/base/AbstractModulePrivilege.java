package com.redmoon.oa.base;

import com.redmoon.oa.pvg.Privilege;

/**
 * <p>Title: 各个模块中权限类需继承于此纯虚基类</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public abstract class AbstractModulePrivilege extends Privilege {
    public String CODE;

    public AbstractModulePrivilege() {
    }
}

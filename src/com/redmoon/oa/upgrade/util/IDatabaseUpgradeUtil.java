package com.redmoon.oa.upgrade.util;

import java.io.File;

public interface IDatabaseUpgradeUtil {

	public abstract void upgrade(File script);

}
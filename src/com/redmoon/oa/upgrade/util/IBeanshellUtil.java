package com.redmoon.oa.upgrade.util;

import java.io.File;

public interface IBeanshellUtil {

	public abstract Object get(String name);

	public abstract void set(String name, Object value);

	public abstract void execute(File source);

}
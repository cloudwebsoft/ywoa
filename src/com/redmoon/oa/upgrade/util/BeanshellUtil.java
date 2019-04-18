package com.redmoon.oa.upgrade.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.redmoon.oa.upgrade.service.UpgradeException;

import bsh.EvalError;
import bsh.Interpreter;

public class BeanshellUtil implements IBeanshellUtil {
	private Interpreter interpreter = new Interpreter();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.redmoon.oa.upgrade.util.IBeanshellUtil#get(java.lang.String)
	 */
	public Object get(String name) {
		try {
			return this.interpreter.get(name);
		} catch (EvalError e) {
			throw new UpgradeException("Execute beanshell script failed.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.redmoon.oa.upgrade.util.IBeanshellUtil#set(java.lang.String,
	 * java.lang.Object)
	 */
	public void set(String name, Object value) {
		try {
			this.interpreter.set(name, value);
		} catch (EvalError e) {
			throw new UpgradeException("Execute beanshell script failed.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.redmoon.oa.upgrade.util.IBeanshellUtil#execute(java.io.File)
	 */
	public void execute(File source) {
		if (!source.exists()) {
			return;
		}
		try {
			interpreter.source(source.getAbsolutePath());
			// interpreter.run();
		} catch (FileNotFoundException e) {
			throw new UpgradeException("Execute beanshell script failed.", e);
		} catch (IOException e) {
			throw new UpgradeException("Execute beanshell script failed.", e);
		} catch (EvalError e) {
			throw new UpgradeException("Execute beanshell script failed.", e);
		}
	}
}

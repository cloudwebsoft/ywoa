package com.redmoon.oa.shell;

public class ClassUnit {
	Class cls;
	boolean explicit;
	
	public ClassUnit(Class cls, boolean explicit) {
		this.cls = cls;
		this.explicit = explicit;
	}
	
	public Class getCls() {
		return cls;
	}
	
	public String getClassName() {
		return cls.getName();
	}
	
	public boolean isExplicit() {
		return explicit;
	}
}

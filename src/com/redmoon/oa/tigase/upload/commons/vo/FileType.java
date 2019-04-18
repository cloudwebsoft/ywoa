package com.redmoon.oa.tigase.upload.commons.vo;

public enum FileType {
	Image("image"), Audio("audio"), Video("video"), Other("other");

	private String baseName;

	private FileType(String baseName) {
		this.baseName = baseName;
	}

	public String getBaseName() {
		return baseName;
	}
}

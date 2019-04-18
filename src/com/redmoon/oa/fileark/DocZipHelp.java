package com.redmoon.oa.fileark;

/**
 * @Description: 
 * @author: 
 * @Date: 2016-9-30下午11:19:57
 */
public class DocZipHelp {
	public static final String TYPE_DIR = "1";
	public static final String TYPE_DOC = "2";
	
	private String dirCode = "";
	private String docId = "";
	private String path = "";
	private String type = "";
	private String attPath = "";
	private String filename = "";
	
	
	public String getAttPath() {
		return attPath;
	}
	public void setAttPath(String attPath) {
		this.attPath = attPath;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getDirCode() {
		return dirCode;
	}
	public void setDirCode(String dirCode) {
		this.dirCode = dirCode;
	}
	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	
}

package cn.js.fan.module.cms.plugin.wiki;

import java.util.Vector;

public class WikiDoc {
	WikiDocLeaf root = new WikiDocLeaf("root");
	
	String content = "";


	public WikiDocLeaf getRoot() {
		return root;
	}

	public void setRoot(WikiDocLeaf root) {
		this.root = root;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}

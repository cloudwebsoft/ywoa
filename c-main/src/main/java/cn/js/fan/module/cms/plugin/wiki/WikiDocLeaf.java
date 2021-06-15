package cn.js.fan.module.cms.plugin.wiki;

import java.util.Iterator;
import java.util.Vector;

public class WikiDocLeaf {
	/**
	 * 章节
	 */
	int chapter = 1;
	
	public int getChapter() {
		return chapter;
	}

	public void setChapter(int chapter) {
		this.chapter = chapter;
	}

	private String title;
	
	/**
	 * 层级
	 */
	private int layer = 0;
	
	public int getLayer() {
		return layer;
	}

	public void setLayer(int layer) {
		this.layer = layer;
	}

	public WikiDocLeaf(String code) {
		this.code = code;
	}
	
	public Vector getChildren() {
		return children;
	}

	public void setChildren(Vector children) {
		this.children = children;
	}

	private Vector children = new Vector();
	
	public String getTitle() {
		return title;
	}
	
	public WikiDocLeaf getChild(String code) {
		Iterator ir = children.iterator();
		while (ir.hasNext()) {
			WikiDocLeaf leaf = (WikiDocLeaf)ir.next();
			if (leaf.getCode().equals(code)) {
				return leaf;
			}
		}
		return null;
	}
	
	public void addChild(WikiDocLeaf leaf) {
		children.addElement(leaf);
		leaf.setParentCode(code);
		leaf.setLayer(layer + 1);
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCode() {
		return code;
	}

	/**
	 * 
	 * @param code
	 */
	public void setCode(String code) {
		this.code = code;
	}

	public String getParentCode() {
		return parentCode;
	}

	public void setParentCode(String parentCode) {
		this.parentCode = parentCode;
	}

	/**
	 * 编码
	 */
	private String code;
	
	private String parentCode;
	
	private int no;

	public int getNo() {
		return no;
	}

	public void setNo(int no) {
		this.no = no;
	}
}

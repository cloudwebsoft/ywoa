package com.redmoon.weixin.util;

import com.cloudwebsoft.framework.util.Cn2Spell;
import com.redmoon.oa.sys.DebugUtil;

/**
 * Java汉字转换为拼音
 *
 */
public class CharacterParser {
	private String resource;
	private static CharacterParser characterParser = new CharacterParser();

	public static CharacterParser getInstance() {
		return characterParser;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getSelling(String chs) {
		chs = chs.replaceAll("　", ""); // 全角空格转半角，否则全角空格会导致出错
		return Cn2Spell.converterToSpell(chs.trim());
	}

	public String getSpelling() {
		return this.getSelling(this.getResource());
	}

	public static void main(String[] args) {
	}
}
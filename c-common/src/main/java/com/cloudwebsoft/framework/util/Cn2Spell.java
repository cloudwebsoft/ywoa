package com.cloudwebsoft.framework.util;

import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * <p>Title: 汉字转换位汉语拼音首字母，英文字符不变</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class Cn2Spell {

    /**
     * 汉字转换位汉语拼音首字母，英文字符不变，使转换首字母
     * @param chines 汉字
     * @return 拼音
     */
    public static String converterToFirstSpell(String chines) {
        String pinyinName = "";
        char[] nameChar = chines.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < nameChar.length; i++) {
            if (nameChar[i] > 128) {
                try {
                	String[] ary = PinyinHelper.toHanyuPinyinStringArray(nameChar[i], defaultFormat);
                	// 如果中文字符串中含有符号，ary会为null
                    if (ary != null && ary.length > 0) {
                		pinyinName += ary[0].charAt(0);
                	}
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    LogUtil.getLog(Cn2Spell.class).error(e);
                }
            } else {
                pinyinName += nameChar[i];
            }
        }
        return pinyinName;
    }

    /**
     * 汉字转换位汉语拼音，英文字符不变
     * @param chines 汉字
     * @return 拼音
     */
    public static String converterToSpell(String chines) {
        String pinyinName = "";
        char[] nameChar = chines.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < nameChar.length; i++) {
            if (nameChar[i] > 128) {
                try {
                    String[] ary = PinyinHelper.toHanyuPinyinStringArray(nameChar[i], defaultFormat);
                    // 如果中文字符串中含有符号，ary会为null
                    if (ary != null && ary.length > 0) {
                        pinyinName += ary[0];
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    LogUtil.getLog(Cn2Spell.class).error(e);
                }
            } else {
                pinyinName += nameChar[i];
            }
        }
        return pinyinName;
    }

    public static void main(String[] args) {

    }
}

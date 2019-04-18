package com.redmoon.oa.android;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
	public static void main(String[] args){
		
		String pat = "<INPUT( style=([^>]+?)){0,1} title=文章地址 .*?value=宏控件： 超链接控件 .*?name=wzdz(.*?)>";
        Pattern p = Pattern.compile(pat,
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		String content = "aaa<INPUT title=文章地址 value=宏控件：超链接控件 name=wzdz cannull=\"1\" kind=\"macro\" macrotype=\"macro_hyperlink_ctl\" macrodefaultvalue=\"\">bbbb";
        Matcher m = p.matcher(content);
        // System.out.println(getClass() + " macroFormField.getName()=" +  macroFormField.getName() + " htmlCtl=" + htmlCtl);

        // LogUtil.getLog(getClass()).info("doReplaceMacroCtlWithHTMLCtl: htmlCtl=" + htmlCtl);
        // LogUtil.getLog(getClass()).info("doReplaceMacroCtlWithHTMLCtl: content=" + content);

        content = m.replaceFirst("test");
        
        System.out.println(content);
		
		System.out.println("aaa");
		FlowListAction fa = new FlowListAction();
		fa.execute();
	}
}

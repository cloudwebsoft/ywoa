package com.redmoon.oa.fileark;

import org.htmlparser.tags.CompositeTag;

/**
 * @Description: 
 * @author: 
 * @Date: 2017-4-13上午11:01:09
 */
public class VideoTag extends CompositeTag {
	
    private static final String mIds[] = {   
        "video"   
    };   
    
    private static final String mEndTagEnders[] = {   
        "video"   
    };   
	   
	@Override
	public String[] getIds() {
		return mIds;
	}
	
	public String[] getEndTagEnders() {   
        return mEndTagEnders;   
    }	
}

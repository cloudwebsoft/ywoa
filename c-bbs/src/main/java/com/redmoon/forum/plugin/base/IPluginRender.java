package com.redmoon.forum.plugin.base;

import com.redmoon.forum.MsgDb;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.forum.MsgPollDb;

public interface IPluginRender {
    MsgPollDb RenderVote(HttpServletRequest request, MsgDb md);
    String RenderContent(HttpServletRequest request, MsgDb md);
    String RenderAttachment(HttpServletRequest request, MsgDb md);
    String RenderTitle(HttpServletRequest request, MsgDb md);
    
    /**
     * 用于listtopic.jsp，显示文章标题
     * @param request
     * @param md
     * @param length -1 表示标题长度不限
     * @return
     */
    String RenderThreadTitle(HttpServletRequest request, MsgDb md, int length);
}

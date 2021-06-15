package com.cloudweb.oa.controller;


import com.cloudweb.oa.entity.OaNoticeReply;
import com.cloudweb.oa.service.IOaNoticeReplyService;
import com.cloudweb.oa.service.impl.OaNoticeReplyServiceImpl;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author fgf
 * @since 2020-01-01
 */
@RestController
@RequestMapping("/notice/reply")
public class OaNoticeReplyController {
    @Autowired
    HttpServletRequest request;

    @Autowired
    IOaNoticeReplyService oaNoticeReplyService;

    @ResponseBody
    @RequestMapping(value = "/reply", method = RequestMethod.POST, produces = {"text/html;", "application/json;charset=UTF-8;"})
    public String create(@RequestParam String content,
                         long noticeId) {
        com.redmoon.oa.android.Privilege pvg = new com.redmoon.oa.android.Privilege();
        boolean re = pvg.auth(request);
        JSONObject json = new JSONObject();
        if (!re) {
            try {
                json.put("ret", "0");
                json.put("msg", "权限非法！");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }

        String userName = pvg.getUserName();
        try {
            OaNoticeReply oaNoticeReply = oaNoticeReplyService.getOaNoticeReply(noticeId, userName);
            if (oaNoticeReply==null) {
                json.put("ret", "0");
                json.put("msg", "无记录，不能回复！");
                return json.toString();
            }
            oaNoticeReply.setContent(content);
            oaNoticeReply.setReplyTime(LocalDateTime.now());
            re = oaNoticeReplyService.updateById(oaNoticeReply);

            if (re) {
                json.put("ret", "1");
                json.put("msg", "操作成功！");
            } else {
                json.put("ret", "0");
                json.put("msg", "操作失败！");
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
}

package com.cloudweb.oa.controller;


import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.entity.Account;
import com.cloudweb.oa.entity.OaNoticeReply;
import com.cloudweb.oa.service.IOaNoticeReplyService;
import com.cloudweb.oa.vo.Result;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.redmoon.oa.notice.NoticeDb;
import com.redmoon.oa.notice.NoticeReplyDb;
import com.redmoon.oa.pvg.Privilege;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author fgf
 * @since 2020-01-01
 */
@Api(tags = "通知回复模块")
@RestController
@RequestMapping("/notice/reply")
public class OaNoticeReplyController {
    @Autowired
    HttpServletRequest request;

    @Autowired
    IOaNoticeReplyService oaNoticeReplyService;

    @ApiOperation(value = "回复", notes = "回复", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "通知ID", required = false, dataType = "Long"),
            @ApiImplicitParam(name = "content", value = "回复内容", required = true, dataType = "String")
    })
    @ResponseBody
    @RequestMapping(value = "/add", method = RequestMethod.POST, produces = {"text/html;", "application/json;charset=UTF-8;"})
    public Result<Object> add(@RequestParam(required = true) Long id,
                                @RequestParam(required = true) String content
    ) {
        com.redmoon.oa.android.Privilege pvg = new com.redmoon.oa.android.Privilege();
        boolean re = pvg.auth(request);
        if (!re) {
            return new Result<>(false, "权限非法");
        }

        String userName = pvg.getUserName();
        OaNoticeReply oaNoticeReply = oaNoticeReplyService.getOaNoticeReply(id, userName);
        if (oaNoticeReply == null) {
            return new Result<>(false, "无记录，不能回复");
        }
        oaNoticeReply.setContent(content);
        oaNoticeReply.setReplyTime(LocalDateTime.now());
        return new Result<>(oaNoticeReplyService.updateById(oaNoticeReply));
    }

    @ApiOperation(value = "回复列表", notes = "回复列表", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "通知ID", required = false, dataType = "Long"),
            @ApiImplicitParam(name = "page", value = "页数", required = true, dataType = "Integer"),
            @ApiImplicitParam(name = "pageSize", value = "每页条数", required = true, dataType = "Integer")
    })
    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.POST, produces = {"text/html;", "application/json;charset=UTF-8;"})
    public Result<Object> list(@RequestParam(required = true) Long id,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "20") int pageSize) {
        PageHelper.startPage(page, pageSize); // 分页
        List<OaNoticeReply> list = oaNoticeReplyService.getReplyHasContent(id);
        PageInfo<OaNoticeReply> pageInfo = new PageInfo<>(list);

        JSONObject jsonObject = new JSONObject();
        Result<Object> result = new Result<>(jsonObject);
        jsonObject.put("list", list);
        jsonObject.put("total", pageInfo.getTotal());
        jsonObject.put("page", page);
        return result;
    }

    /*@ApiOperation(value = "回复", notes = "回复", httpMethod = "POST")
    @ResponseBody
    @RequestMapping(value = "/addReply", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public Result<Object> addReply(@RequestParam(required = true) Long id, @RequestParam(required = true) String content) {
        Privilege privilege = new Privilege();
        String userName = privilege.getUser(request);
        NoticeReplyDb noticeReplyDb = new NoticeReplyDb();
        noticeReplyDb.setUsername(userName);
        noticeReplyDb.setNoticeid(id);
        noticeReplyDb.setContent(content);
        boolean re = false;
        try {
            re = noticeReplyDb.save();
        } catch (ErrMsgException | ResKeyException e) {
            LogUtil.getLog(getClass()).error(e);
        }

        if (re) {
            java.util.Date rDate = new java.util.Date();
            NoticeDb noticeDb = new NoticeDb();
            noticeDb = noticeDb.getNoticeDb(id);
            if (noticeDb.getIs_forced_response() == 1) {
                NoticeReplyDb nrdb = new NoticeReplyDb();
                nrdb.setIsReaded("1");
                nrdb.setReadTime(rDate);
                nrdb.setNoticeid(id);
                nrdb.setUsername(userName);
                nrdb.saveStatus();
            }
        }
        return new Result<>(re);
    }*/
}

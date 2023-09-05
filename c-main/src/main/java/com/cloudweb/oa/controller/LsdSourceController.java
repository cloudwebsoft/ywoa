package com.cloudweb.oa.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.utils.SysUtil;
import com.cloudweb.oa.vo.Result;
import com.redmoon.oa.base.IAttachment;
import com.redmoon.oa.visual.FormDAO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Vector;

@Api(tags = "大屏资源库")
@RestController
@RequestMapping("/lsd")
public class LsdSourceController {

    @Autowired
    SysUtil sysUtil;

    @ApiOperation(value = "列表", notes = "列表", httpMethod = "POST")
    @RequestMapping(value = "/list", method = RequestMethod.POST, produces={"application/json;charset=UTF-8;"})
    @ResponseBody
    public Result<Object> list() {
        String formCode = "lsd_source";
        /*FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);*/
        FormDAO dao = new FormDAO();
        List<FormDAO> list = dao.selectList(formCode, "select id from ft_lsd_source order by orders desc");
        JSONArray arr = new JSONArray();

        for (FormDAO fdao : list) {
            com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
            json.put("id", fdao.getId());
            json.put("name", fdao.getFieldValue("name"));
            arr.add(json);
            Vector<IAttachment> v = fdao.getAttachments();
            JSONArray arrAtt = new JSONArray();
            json.put("images", arrAtt);
            for (IAttachment att : v) {
                JSONObject jsonAtt = new JSONObject();
                // jsonAtt.put("path", sysUtil.getRootPath() + "/" + att.getVisualPath() + "/" + att.getDiskName());
                jsonAtt.put("path", att.getVisualPath() + "/" + att.getDiskName());
                jsonAtt.put("name", att.getName());
                arrAtt.add(jsonAtt);
            }
        }
        return new Result<>(arr);
    }
}

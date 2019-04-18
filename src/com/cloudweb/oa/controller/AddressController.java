package com.cloudweb.oa.controller;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.SkinUtil;
import com.cloudweb.oa.bean.Address;
import com.cloudweb.oa.service.AddressService;
import com.redmoon.oa.address.AddressDb;
import com.redmoon.oa.android.Privilege;
import com.redmoon.weixin.bean.SortModel;
import com.redmoon.weixin.util.CharacterParser;
import com.redmoon.weixin.util.PinyinComparator;
import net.sf.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * 新的通讯录controller
 * 2018-12-28 14:49:34
 */

@Controller
public class AddressController {
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private AddressService addressService;

    /**
     * 原来遗留，不清楚具体作用
     * @param section
     * @param sortModels
     * @return
     */
    public int getPositionForSection(int section, List<SortModel> sortModels) {
        for (int i = 0; i < sortModels.size(); i++) {
            String sortStr = sortModels.get(i).getSortLetters();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 暂未找到使用处
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/public/address/list", method = RequestMethod.GET, produces = {"text/html;", "application/json;charset=UTF-8;"})
    public String list() {
        JSONObject json = new JSONObject();
        Privilege privilege = new Privilege();
        boolean re = privilege.auth(request);
        if (!re) {
            try {
                json.put("res", "-1");
                // json.put("msg", "时间过期");
                return json.toString();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        JSONArray arr = new JSONArray();
        List<SortModel> sortModels = new ArrayList<SortModel>();
        net.sf.json.JSONObject obj = new net.sf.json.JSONObject();

        String what = ParamUtil.get(request, "what");
        int type = ParamUtil.getInt(request, "type", AddressService.TYPE_USER);
        String sql;
        if (type == AddressService.TYPE_USER)
            sql = "select id from address where userName=" + StrUtil.sqlstr(privilege.getUserName()) + " and type=" + AddressService.TYPE_USER;
        else {
            sql = "select id from address where type=" + type;
        }

        if (!what.equals("")) {
            sql += " and (person like " + StrUtil.sqlstr("%" + what + "%") + " or company like " + StrUtil.sqlstr("%" + what + "%") + " or mobile like " + StrUtil.sqlstr("%" + what + "%") + ")";
        }

        sql += " order by id desc";

        Address addr;
        Iterator ir = addressService.listSql(sql).getResult().iterator();;
        while (ir.hasNext()) {
            addr = (Address) ir.next();
            String person = addr.getPerson();
            String pinyin = CharacterParser.getInstance().getSelling(person);
            String sortString = pinyin.substring(0, 1).toUpperCase();
            SortModel sortUserModel = new SortModel();
            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
                sortUserModel.setSortLetters(sortString.toUpperCase());
            } else {
                sortUserModel.setSortLetters("#");
            }
            sortUserModel.setObjs(addr);
            sortModels.add(sortUserModel);
        }

        if (sortModels != null && sortModels.size() > 0) {
            PinyinComparator pinyinComparator = new PinyinComparator();
            Collections.sort(sortModels, pinyinComparator);
            for (int i = 0; i < sortModels.size(); i++) {
                net.sf.json.JSONObject itemObj = new net.sf.json.JSONObject();
                int sec = sortModels.get(i).getSortLetters().charAt(0);
                addr = (Address) sortModels.get(i).getObjs();
                net.sf.json.JSONObject userObj = new net.sf.json.JSONObject();
                userObj.put("person", addr.getPerson());
                userObj.put("mobile", addr.getMobile());
                userObj.put("company", addr.getCompany());
                userObj.put("email", addr.getEmail());
                userObj.put("id", addr.getId());

                if (i == getPositionForSection(sec, sortModels)) {
                    itemObj.put("isGroup", true);
                    itemObj.put("name", sortModels.get(i).getSortLetters());
                    itemObj.put("pyName", sortModels.get(i).getSortLetters());
                    arr.add(itemObj);

                    net.sf.json.JSONObject itemObj2 = new net.sf.json.JSONObject();
                    itemObj2.put("isGroup", false);
                    itemObj2.put("name", addr.getPerson());
                    itemObj2.put("pyName", CharacterParser.getInstance()
                            .getSelling(addr.getPerson()));
                    itemObj2.put("user", userObj);
                    arr.add(itemObj2);
                } else {
                    itemObj.put("isGroup", false);
                    itemObj.put("name", addr.getPerson());
                    itemObj.put("pyName", CharacterParser.getInstance().getSelling(
                            addr.getPerson()));
                    itemObj.put("user", userObj);
                    arr.add(itemObj);
                }
            }
        }
        if (arr != null && arr.size() > 0) {
            obj.put("res", 0);
            obj.put("datas", arr);
        } else {
            obj.put("res", -1);
        }
        return obj.toString();
    }

    /**
     * 编辑通讯录
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/address/edit", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String edit(Address address){
        JSONObject json = new JSONObject();
        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        String priv = "read";
        if (!privilege.isUserPrivValid(request, priv)) {
            try {
                json.put("res", "0");
                json.put("msg", SkinUtil.LoadString(request, "pvg_invalid"));
                return json.toString();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        boolean re = false;
        try {
            try {
                re  = addressService.save(address);
                if (re) {
                    json.put("ret", "1");
                    json.put("msg", "操作成功！");
                } else {
                    json.put("ret", "0");
                    json.put("msg", "操作失败！");
                }
            }catch (ErrMsgException e){
                json.put("ret", 0);
                json.put("msg", e.getMessage());
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }


    /**
     * 新增通讯录
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/address/create", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String create(Address address) {
        JSONObject json = new JSONObject();
        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        String priv = "read";
        if (!privilege.isUserPrivValid(request, priv)) {
            try {
                json.put("res", "0");
                json.put("msg", SkinUtil.LoadString(request, "pvg_invalid"));
                return json.toString();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        boolean re = false;
        try {
            try {
                re = addressService.create(address);
                if (re) {
                    json.put("ret", "1");
                    json.put("msg", "操作成功！");
                } else {
                    json.put("ret", "0");
                    json.put("msg", "操作失败！");
                }
            } catch (ErrMsgException e) {
                json.put("ret", 0);
                json.put("msg", e.getMessage());
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    /**
     * 批量删除
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/address/delBatch", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String delBatch(String ids) {
        JSONObject json = new JSONObject();
        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        String priv = "read";
        if (!privilege.isUserPrivValid(request, priv)) {
            try {
                json.put("res", "0");
                json.put("msg", SkinUtil.LoadString(request, "pvg_invalid"));
                return json.toString();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        boolean re = true;
        try {
            try {
                addressService.delBatch(ids);
                if (re) {
                    json.put("ret", "1");
                    json.put("msg", "操作成功！");
                } else {
                    json.put("ret", "0");
                    json.put("msg", "操作失败！");
                }
            } catch (ErrMsgException e) {
                json.put("ret", 0);
                json.put("msg", e.getMessage());
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @RequestMapping(value = "/address/list",method = RequestMethod.GET)
    public String listTemp(){
        return "redirect:/address/address.jsp";
    }
}

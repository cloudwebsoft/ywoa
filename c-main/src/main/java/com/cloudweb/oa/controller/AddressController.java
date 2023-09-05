package com.cloudweb.oa.controller;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.Paginator;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.SkinUtil;
import com.alibaba.fastjson.JSONArray;
import com.cloudweb.oa.bean.Address;
import com.cloudweb.oa.service.AddressService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.address.DirectoryView;
import com.redmoon.oa.address.Leaf;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.ui.SkinMgr;
import com.redmoon.weixin.bean.SortModel;
import com.redmoon.weixin.util.CharacterParser;
import com.redmoon.weixin.util.PinyinComparator;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
     * 用于手机端
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/public/address/list", method = RequestMethod.GET, produces = {"text/html;", "application/json;charset=UTF-8;"})
    public String list() {
        JSONObject json = new JSONObject();
        String userName = "";
        Privilege privilege = new Privilege();
        boolean re = privilege.auth(request);
        userName = privilege.getUserName();
        if (!re) {
            try {
                json.put("res", "-1");
                // json.put("msg", "时间过期");
                return json.toString();
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }

        com.alibaba.fastjson.JSONArray arr = new com.alibaba.fastjson.JSONArray();
        List<SortModel> sortModels = new ArrayList<SortModel>();
        com.alibaba.fastjson.JSONObject obj = new com.alibaba.fastjson.JSONObject();

        String what = ParamUtil.get(request, "what");
        int type = ParamUtil.getInt(request, "type", AddressService.TYPE_USER);
        String sql;
        if (type == AddressService.TYPE_USER) {
            sql = "select * from address where userName=" + StrUtil.sqlstr(userName) + " and type=" + AddressService.TYPE_USER;
        } else {
            sql = "select * from address where type=" + type;
        }

        if (!what.equals("")) {
            sql += " and (person like " + StrUtil.sqlstr("%" + what + "%") + " or company like " + StrUtil.sqlstr("%" + what + "%") + " or mobile like " + StrUtil.sqlstr("%" + what + "%") + ")";
        }

        sql += " order by id desc";

        Address addr;
        Iterator ir = addressService.listSql(sql).getResult().iterator();
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
                com.alibaba.fastjson.JSONObject itemObj = new com.alibaba.fastjson.JSONObject();
                int sec = sortModels.get(i).getSortLetters().charAt(0);
                addr = (Address) sortModels.get(i).getObjs();
                com.alibaba.fastjson.JSONObject userObj = new com.alibaba.fastjson.JSONObject();
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

                    com.alibaba.fastjson.JSONObject itemObj2 = new com.alibaba.fastjson.JSONObject();
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
    @RequestMapping(value = "/address/save", method = RequestMethod.POST, produces = {"text/html;charset=UTF-8;", "application/json;"})
    public String save(Address address){
        JSONObject json = new JSONObject();
        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        String priv = "read";
        if (!privilege.isUserPrivValid(request, priv)) {
            try {
                json.put("res", "0");
                json.put("msg", SkinUtil.LoadString(request, "pvg_invalid"));
                return json.toString();
            } catch (JSONException e) {
                LogUtil.getLog(getClass()).error(e);
            }
        }
        boolean re = false;
        try {
            try {
                Address addr = addressService.getAddress(address.getId());
                if (address.getType() == ConstUtil.ADDRESS_TYPE_PUBLIC) {
                    if (!privilege.isUserPrivValid(request, "admin.address.public")) {
                        throw new ErrMsgException(com.redmoon.oa.pvg.Privilege.MSG_INVALID);
                    }
                } else {
                    if (!privilege.getUser(request).equals(addr.getUserName())) {
                        throw new ErrMsgException("非法操作！");
                    }
                }
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
            LogUtil.getLog(getClass()).error(e);
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
                LogUtil.getLog(getClass()).error(e);
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
            LogUtil.getLog(getClass()).error(e);
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
                LogUtil.getLog(getClass()).error(e);
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
            LogUtil.getLog(getClass()).error(e);
        }
        return json.toString();
    }

/*    @RequestMapping(value = "/address/list",method = RequestMethod.GET)
    public String listTemp(){
        return "redirect:/address/address.jsp";
    }*/

    @RequestMapping("/address/list")
    public String list(Model model) {
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        String priv = "read";
        if (!pvg.isUserPrivValid(request, priv)) {
            model.addAttribute("info", SkinUtil.LoadString(request, "pvg_invalid"));
            return "error";
        }

        String mode = ParamUtil.get(request, "mode");
        int type = ParamUtil.getInt(request, "type", AddressService.TYPE_USER);
        if (type == AddressService.TYPE_PUBLIC) {
            if (!"show".equals(mode)) {
                if (!pvg.isUserPrivValid(request, "admin.address.public")) {
                    model.addAttribute("info", SkinUtil.LoadString(request, "pvg_invalid"));
                    return "error";
                }
            }
        }

        String userName = pvg.getUser(request);
        String op = ParamUtil.get(request, "op");
        String typeId = ParamUtil.get(request, "dir_code");
        String person = ParamUtil.get(request, "person");
        String company = ParamUtil.get(request, "company");
        String mobile = ParamUtil.get(request, "mobile");

        Paginator paginator = new Paginator(request);
        int curPage = paginator.getCurPage();

        String searchStr = "";
        searchStr += "person=" + StrUtil.UrlEncode(person);
        searchStr += "&company=" + StrUtil.UrlEncode(company);
        searchStr += "&typeId=" + typeId;
        searchStr += "&mobile=" + mobile;

        String orderBy = ParamUtil.get(request, "orderBy");
        if (orderBy.equals("")) {
            orderBy = "id";
        }
        String sort = ParamUtil.get(request, "sort");
        if (sort.equals("")) {
            sort = "desc";
        }

        int pageSize = ParamUtil.getInt(request, "pageSize", 20);
        String sql = addressService.getSql(op, userName, type, typeId, person, company, mobile, orderBy, sort, pvg.getUserUnitCode(request));
        ListResult lr = addressService.listResult(sql, curPage, pageSize);

        model.addAttribute("result", lr.getResult());
        model.addAttribute("total", lr.getTotal());

        model.addAttribute("op", op);
        model.addAttribute("mode", mode);
        model.addAttribute("typeId", typeId);
        model.addAttribute("person", person);
        model.addAttribute("company", company);
        model.addAttribute("mobile", mobile);
        model.addAttribute("type", type);
        model.addAttribute("userName", userName);
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));
        model.addAttribute("searchStr", searchStr);
        model.addAttribute("isUseSMS", com.redmoon.oa.sms.SMSFactory.isUseSMS());

        model.addAttribute("pageSize", pageSize);
        model.addAttribute("curPage", curPage);
        model.addAttribute("orderBy", orderBy);
        model.addAttribute("sort", sort);

        com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
        String sql3des = cn.js.fan.security.ThreeDesUtil.encrypt2hex(ssoCfg.getKey(), sql);
        model.addAttribute("sql3des", sql3des);

        boolean canManage = type==AddressService.TYPE_USER || (type==AddressService.TYPE_PUBLIC && pvg.isUserPrivValid(request, "admin.address.public"));
        model.addAttribute("canManage", canManage);

        Leaf lf = new Leaf();
        if (type==AddressService.TYPE_PUBLIC) {
            lf = lf.getLeaf(Leaf.USER_NAME_PUBLIC);
        }
        else {
            lf = lf.getLeaf(userName);
        }
        DirectoryView dv = new DirectoryView(lf);
        int rootlayer = 1;
        StringBuffer sb = new StringBuffer();
        try {
            dv.ShowDirectoryAsOptionsWithCode(sb, lf, rootlayer);
            model.addAttribute("dirOpts", sb.toString());
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }

        // return "address/address";
        return "th/address/address";
    }

    @RequestMapping("/address/show")
    public String show(int id, Model model) {
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        String priv = "read";
        if (!pvg.isUserPrivValid(request, priv)) {
            model.addAttribute("info", SkinUtil.LoadString(request, "pvg_invalid"));
            return "error";
        }

        Address addr = addressService.getAddress(id);
        model.addAttribute("addr", addr);
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        // return "address/address_show";
        return "th/address/address_show";
    }

    @RequestMapping("/address/add")
    public String add(Model model) {
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        String priv = "read";
        if (!pvg.isUserPrivValid(request, priv)) {
            model.addAttribute("info", SkinUtil.LoadString(request, "pvg_invalid"));
            return "error";
        }

        int type = ParamUtil.getInt(request, "type", ConstUtil.ADDRESS_TYPE_USER);
        if (type == ConstUtil.ADDRESS_TYPE_PUBLIC) {
            if (!pvg.isUserPrivValid(request, "admin.address.public")) {
                model.addAttribute("info", SkinUtil.LoadString(request, "pvg_invalid"));
                return "error";
            }
        }
        String typeId = ParamUtil.get(request, "typeId");
        model.addAttribute("type", type);
        model.addAttribute("typeId", typeId);
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        String rootCode = pvg.getUser(request);
        if (type == AddressService.TYPE_PUBLIC) {
            rootCode = Leaf.USER_NAME_PUBLIC;
        }
        Leaf lf = new Leaf();
        lf = lf.getLeaf(rootCode);
        DirectoryView dv = new DirectoryView(lf);
        int rootlayer = 1;
        StringBuffer sb = new StringBuffer();
        try {
            dv.ShowDirectoryAsOptionsWithCode(sb, lf, rootlayer);
            model.addAttribute("dirOpts", sb.toString());
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }

        return "th/address/address_add";
    }

    @RequestMapping("/address/edit")
    public String edit(int id, Model model) {
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        String priv = "read";
        if (!pvg.isUserPrivValid(request, priv)) {
            model.addAttribute("info", SkinUtil.LoadString(request, "pvg_invalid"));
            return "error";
        }

        Address address = addressService.getAddress(id);

        model.addAttribute("addr", address);
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        String tabIdOpener = ParamUtil.get(request, "tabIdOpener");
        model.addAttribute("tabIdOpener", tabIdOpener);

        int type = ParamUtil.getInt(request, "type", AddressService.TYPE_USER);
        model.addAttribute("type", type);
        String rootCode = pvg.getUser(request);
        if (type == AddressService.TYPE_PUBLIC) {
            rootCode = Leaf.USER_NAME_PUBLIC;
        }
        Leaf lf = new Leaf();
        lf = lf.getLeaf(rootCode);
        DirectoryView dv = new DirectoryView(lf);
        int rootlayer = 1;
        StringBuffer sb = new StringBuffer();
        try {
            dv.ShowDirectoryAsOptionsWithCode(sb, lf, rootlayer);
            model.addAttribute("dirOpts", sb.toString());
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error(e);
        }

        return "th/address/address_edit";
    }
}

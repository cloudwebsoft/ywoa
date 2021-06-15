package com.redmoon.weixin.mgr;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.flow.Directory;
import com.redmoon.oa.flow.DirectoryView;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.post.PostFlowMgr;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.weixin.bean.SortModel;
import com.redmoon.weixin.util.CharacterParser;
import com.redmoon.weixin.util.PinyinComparator;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * @Description: 取字母section在sortModels列表中的索引位置
 * @author:
 * @Date: 2016-8-15上午08:55:36
 */
public class FlowDoMgr {
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
     * 发起流程列表初始化 listview 字母 A-Z排序
     *
     * @param request
     * @param userName
     * @return
     * @Description:
     */
    public JSONObject flowInitList(HttpServletRequest request, String userName) {
        JSONArray arr = new JSONArray();
        List<SortModel> sortModels = new ArrayList<SortModel>();
        JSONObject obj = new JSONObject();
        Directory dir = new Directory();
        Leaf rootlf = dir.getLeaf(Leaf.CODE_ROOT);
        DirectoryView dv = new DirectoryView(rootlf);
        Vector<Leaf> children;
        children = dir.getChildren(Leaf.CODE_ROOT);
        PostFlowMgr pfMgr = new PostFlowMgr();
        // 5.0（不含）之前的老版中，绩效考核流程权限判定，5.0以后已弃用
        ArrayList<String> list = pfMgr.listCanUserStartFlow(userName);
        for (Leaf childlf : children) {
            if (childlf.isOpen() && dv.canUserSeeWhenInitFlow(request, childlf)) {
                for (Leaf leaf : dir.getChildren(childlf.getCode())) {
                    boolean isMobileStart = false;
                    if (leaf.isOpen()
                            && dv.canUserSeeWhenInitFlow(request, leaf)
                            && leaf.isMobileStart()) {
                        if ("performance".equals(leaf.getParentCode())) {
                            if (list.contains(leaf.getCode())) {
                                isMobileStart = true;
                            }
                        } else {
                            isMobileStart = true;
                        }
                        if (isMobileStart) {
                            SortModel sortModel = new SortModel();
                            sortModel.setObjs(leaf);
                            String pinyin = CharacterParser.getInstance().getSelling(leaf.getName());
                            String sortString = pinyin.substring(0, 1).toUpperCase();
                            // 正则表达式，判断首字母是否是英文字母
                            if (sortString.matches("[A-Z]")) {
                                sortModel.setSortLetters(sortString.toUpperCase());
                            } else {
                                sortModel.setSortLetters("#");
                            }
                            sortModels.add(sortModel);
                        }
                    }

                }
            }
        }
        if (sortModels.size() > 0) {
            PinyinComparator pinyinComparator = new PinyinComparator();
            Collections.sort(sortModels, pinyinComparator);
            for (int i = 0; i < sortModels.size(); i++) {

                JSONObject itemObj = new JSONObject();
                int sec = sortModels.get(i).getSortLetters().charAt(0);

                Leaf cLeaf = (Leaf) sortModels.get(i).getObjs();
                String code = cLeaf.getCode();
                String name = cLeaf.getName();
                String params = cLeaf.getParams();
                params = params.replaceFirst("\\$userName", userName);
                int type = cLeaf.getType();
                if (i == getPositionForSection(sec, sortModels)) {
                    itemObj.put("isGroup", true);
                    itemObj.put("name", sortModels.get(i).getSortLetters());
                    itemObj.put("pyName", sortModels.get(i).getSortLetters());
                    arr.add(itemObj);
                    JSONObject itemObj2 = new JSONObject();
                    itemObj2.put("isGroup", false);
                    itemObj2.put("name", name);
                    itemObj2.put("code", code);
                    itemObj2.put("type", type);
                    itemObj2.put("pyName", CharacterParser.getInstance().getSelling(name));
                    itemObj2.put("params", params);
                    arr.add(itemObj2);
                } else {
                    itemObj.put("isGroup", false); // 索引页面的分组
                    itemObj.put("code", code);
                    itemObj.put("type", type);
                    itemObj.put("name", name);
                    itemObj.put("pyName", CharacterParser.getInstance().getSelling(name));
                    arr.add(itemObj);
                }
            }
        }
        if (arr.size() > 0) {
            obj.put("res", 0);
            obj.put("datas", arr);
        } else {
            obj.put("res", -1);
        }
        return obj;
    }

    /**
     * 所有用户 字母A-Z
     *
     * @return
     * @Description:
     */
    public JSONObject usersInitList() {
        JSONArray arr = new JSONArray();
        List<SortModel> sortModels = new ArrayList<SortModel>();
        JSONObject obj = new JSONObject();
        // UserDb userDb = new UserDb();
        // 通过三表联合查询，3000多人的情况下，约为80ms
        String sql = "select u.name,realName,gender,mobile,photo,d.name from users u, dept_user du, department d where u.name=du.user_name and d.code=du.dept_code and isValid=1 and isPass=1 and u.name<>" + StrUtil.sqlstr("system");
        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri = null;
        ResultRecord rd = null;
        try {
            ri = jt.executeQuery(sql);
            while (ri.hasNext()) {
                rd = (ResultRecord) ri.next();
                String userName = rd.getString(1);
                UserDb ud = new UserDb();
                String realName = rd.getString(2);
                int gender = rd.getInt(3);
                String mobile = rd.getString(4);
                String photo = rd.getString(5);
                String deptName = rd.getString(6);
                ud.setName(userName);
                ud.setRealName(realName);
                ud.setGender(gender);
                ud.setMobile(mobile);
                ud.setPhoto(photo);
                ud.setParty(deptName); // 借用party存储deptName
                // 通过3表联合查询优化
                // UserDb ud = new UserDb(userName);
                // String realName = ud.getRealName();
                String pinyin = CharacterParser.getInstance().getSelling(realName);
                if ("".equals(pinyin)) {
                    DebugUtil.i(getClass(), "usersInitList realName=", realName);
                    continue;
                }
                String sortString = pinyin.substring(0, 1).toUpperCase();
                SortModel sortUserModel = new SortModel();
                // 正则表达式，判断首字母是否是英文字母
                if (sortString.matches("[A-Z]")) {
                    sortUserModel.setSortLetters(sortString.toUpperCase());
                } else {
                    sortUserModel.setSortLetters("#");
                }
                sortUserModel.setObjs(ud);
                sortModels.add(sortUserModel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        if (sortModels.size() > 0) {
            PinyinComparator pinyinComparator = new PinyinComparator();
            Collections.sort(sortModels, pinyinComparator);
            for (int i = 0; i < sortModels.size(); i++) {
                JSONObject itemObj = new JSONObject();
                int sec = sortModels.get(i).getSortLetters().charAt(0);
                UserDb users = (UserDb) sortModels.get(i).getObjs();
                JSONObject userObj = new JSONObject();
                String name = users.getName();
                // DeptUserDb dud = new DeptUserDb(name);
                // String deptName = dud.getDeptName();
                userObj.put("name", users.getName());
                userObj.put("realName", users.getRealNameRaw());
                userObj.put("mobile", users.getMobile());
                userObj.put("photo", users.getPhoto());
                userObj.put("gender", users.getGender());
                userObj.put("dName", users.getParty());  // 借用party存储deptName
                if (i == getPositionForSection(sec, sortModels)) {
                    // 如果其首字母在链表中的位置为i，则说明其为sortModels列表中第一个首字母为sec的元素，加入组名
                    itemObj.put("isGroup", true);
                    itemObj.put("name", sortModels.get(i).getSortLetters());
                    itemObj.put("pyName", sortModels.get(i).getSortLetters());
                    arr.add(itemObj);
                    JSONObject itemObj2 = new JSONObject();
                    itemObj2.put("isGroup", false);
                    itemObj2.put("name", users.getRealNameRaw());
                    itemObj2.put("pyName", CharacterParser.getInstance()
                            .getSelling(users.getRealNameRaw()));
                    itemObj2.put("user", userObj);
                    arr.add(itemObj2);
                } else {
                    itemObj.put("isGroup", false);
                    itemObj.put("name", users.getRealNameRaw());
                    itemObj.put("pyName", CharacterParser.getInstance().getSelling(
                            users.getRealNameRaw()));
                    itemObj.put("user", userObj);
                    arr.add(itemObj);
                }
            }
        }
        if (arr.size() > 0) {
            obj.put("res", 0);
            obj.put("datas", arr);
        } else {
            obj.put("res", -1);
        }
        return obj;
    }
}

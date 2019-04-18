package com.redmoon.weixin.mgr;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.weixin.Config;
import com.redmoon.weixin.enums.Enum;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.List;

public class WXAddressCallBack {
    public String contentXml;

    /**
     * 解析微信XML
     * @return
     */
    public HashMap<String,String> parseXml(){
        HashMap<String,String> hashMap = new HashMap<String, String>();
        try {
            Document document = DocumentHelper.parseText(contentXml);
            Element root = document.getRootElement();
            List<Element> list = root.elements();
            for(Element e:list){
                hashMap.put(e.getName(),e.getText());
            }
        } catch (DocumentException e) {
        }
        return hashMap;
    }

    /**
     * 处理回调类型
     * @throws ErrMsgException
     */
    public void dispose() throws ErrMsgException {
        com.redmoon.weixin.Config weixinCfg = Config.getInstance();
        if (!weixinCfg.getBooleanProperty("isUse")) {
            return;
        }
        // 以OA为准
        if (!weixinCfg.getBooleanProperty("isSyncWxToOA")) {
            return;
        }
        HashMap<String,String> hashMap = parseXml();
        if(hashMap!=null && hashMap.size()>0){
            if(hashMap.containsKey(Enum.emWXChangeKey.emMsgType) &&hashMap.containsKey(Enum.emWXChangeKey.emEvent) && hashMap.containsKey(Enum.emWXChangeKey.emChangeType)){
                if(hashMap.get(Enum.emWXChangeKey.emMsgType).equals(Enum.emMsgType.emEvent) && hashMap.get(Enum.emWXChangeKey.emEvent).equals(Enum.emEvent.emChangeContact)){
                    String _changeType = hashMap.get(Enum.emWXChangeKey.emChangeType);
                    UserDeptByWXMgr _userDeptByWx = new UserDeptByWXMgr();
                    if(_changeType.equals(Enum.emChangeType.emCreateUser)){//创建用户
                        _userDeptByWx.createUser(hashMap);
                    }else if(_changeType.equals(Enum.emChangeType.emDeleteUser)){//删除用户
                        String _userId = hashMap.get("UserID");
                        UserMgr _userMgr = new UserMgr();
                        try {
                            _userMgr.del(_userId);
                        } catch (ResKeyException e) {
                            Logger.getLogger(WXAddressCallBack.class.getName()).error("微信同步OA删除失败，"+e.getMessage());
                        }
                    }else if(_changeType.equals(Enum.emChangeType.emUpdateUser)){//修改用户
                        _userDeptByWx.createUser(hashMap);
                    }else if(_changeType.equals(Enum.emChangeType.emCreateParty)){
                        _userDeptByWx.createDept(hashMap);
                    }else if(_changeType.equals(Enum.emChangeType.emDeleteParty)){
                        Integer _Id = StrUtil.toInt(hashMap.get("Id"),0);
                        DeptDb _deptDb = new DeptDb(_Id);
                        if(_deptDb!=null && _deptDb.isLoaded()){
                            _deptDb.del();
                        }
                    }else if(_changeType.equals(Enum.emChangeType.emUpdateParty)){
                        Integer _Id = StrUtil.toInt(hashMap.get("Id"),0);
                        String _Name = hashMap.get("Name");
                        DeptDb _deptDb = new DeptDb(_Id);
                        if(_deptDb!=null && _deptDb.isLoaded()){
                            _deptDb.setName(_Name);
                            _deptDb.save();
                        }
                    }
                }
            }
        }
    }
}

package com.redmoon.oa.questionnaire;

import org.apache.log4j.Logger;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import java.util.Iterator;

public class QuestionnaireFormItemMgr {
    Logger logger = Logger.getLogger(QuestionnaireFormItemMgr.class.getName());

    public QuestionnaireFormItemMgr() {
    }

    public boolean create(HttpServletRequest request) throws ErrMsgException {
        String errmsg = "";

        int formId = ParamUtil.getInt(request, "form_id");
        String itemName = ParamUtil.get(request, "item_name");
        int itemType = ParamUtil.getInt(request, "item_type");
        int itemIndex = ParamUtil.getInt(request, "item_index");
        String itemOptions = ParamUtil.get(request, "item_options");
        int checkedType = ParamUtil.getInt(request, "checked_type");

        if (itemName.equals("")) {
            errmsg += "项目名称不能为空！\\n";
        }
        if(itemType==QuestionnaireFormItemDb.ITEM_TYPE_RADIO_GROUP || itemType==QuestionnaireFormItemDb.ITEM_TYPE_CHECKBOX) {
            if(itemOptions.equals("")) {
                errmsg += "项目选项不能为空！\\n";
            }
        } else {
            itemOptions = "";
        }
        if (!errmsg.equals("")) {
            throw new ErrMsgException(errmsg);
        }

        QuestionnaireFormItemDb qfid = new QuestionnaireFormItemDb();

        qfid.setFormId(formId);
        qfid.setItemName(itemName);
        qfid.setItemType(itemType);
        qfid.setItemIndex(itemIndex);
        qfid.setCheckedType(checkedType);

        boolean re = qfid.create();
        if (re) {
            String[] ary = StrUtil.split(itemOptions, ":,:");
            if (ary != null) {
                int len = ary.length;
                for (int i = 0; i < len; i++) {
                    String name = ary[i];
                    QuestionnaireFormSubitemDb qfsd = new
                            QuestionnaireFormSubitemDb();
                    qfsd.setName(name);
                    qfsd.setOrders(i);
                    qfsd.setItemId(qfid.getItemId());
                    qfsd.create();
                }
            }
        }

        return re;
    }

    public QuestionnaireFormItemDb getQuestionnaireFormItemDb(int itemId) {
        QuestionnaireFormItemDb qfid = new QuestionnaireFormItemDb();
        return qfid.getQuestionnaireFormItemDb(itemId);
    }


    public boolean save(HttpServletRequest request) throws ErrMsgException {
        boolean re = false;

        String errmsg = "";

        int itemId = ParamUtil.getInt(request, "item_id");
        String itemName = ParamUtil.get(request, "item_name");
        int itemType = ParamUtil.getInt(request, "item_type");
        int itemIndex = ParamUtil.getInt(request, "item_index");
        String itemOptions = ParamUtil.get(request, "item_options");
        int checkedType = ParamUtil.getInt(request, "checked_type");

        if (itemName.equals("")) {
            errmsg += "项目名称不能为空！\\n";
        }
        if(itemType==QuestionnaireFormItemDb.ITEM_TYPE_RADIO_GROUP || itemType==QuestionnaireFormItemDb.ITEM_TYPE_CHECKBOX) {
            if(itemOptions.equals("")) {
                errmsg += "项目选项不能为空！\\n";
            }
        } else {
            itemOptions = "";
        }
        if (!errmsg.equals("")) {
            throw new ErrMsgException(errmsg);
        }

        QuestionnaireFormItemDb qfid = getQuestionnaireFormItemDb(itemId);

        qfid.setItemName(itemName);
        qfid.setItemType(itemType);
        qfid.setItemIndex(itemIndex);
        qfid.setCheckedType(checkedType);

        // 修改subitem
        QuestionnaireFormSubitemDb qfsd = new QuestionnaireFormSubitemDb();
        Iterator ir = qfsd.listSubItems(itemId).iterator();
        // 先删除
        while (ir.hasNext()) {
            qfsd = (QuestionnaireFormSubitemDb) ir.next();
            qfsd.del();
        }
        if(itemType==QuestionnaireFormItemDb.ITEM_TYPE_RADIO_GROUP || itemType==QuestionnaireFormItemDb.ITEM_TYPE_CHECKBOX) {
            // 后增加
            String[] ary = StrUtil.split(itemOptions, ":,:");
            if (ary != null) {
                int len = ary.length;
                for (int i = 0; i < len; i++) {
                    String name = ary[i];
                    qfsd.setName(name);
                    qfsd.setOrders(i);
                    qfsd.setItemId(itemId);
                    qfsd.create();
                }
            }
        }
        return qfid.save();
    }

    public boolean del(HttpServletRequest request) throws ErrMsgException {
        int itemId = ParamUtil.getInt(request, "item_id");
        QuestionnaireFormItemDb qfid = getQuestionnaireFormItemDb(itemId);
        if (qfid==null || !qfid.isLoaded()) {
            throw new ErrMsgException("该项已不存在！");
        }
        // 删除子项
        if (qfid.getItemType() == QuestionnaireFormItemDb.ITEM_TYPE_RADIO_GROUP ||
            qfid.getItemType() == QuestionnaireFormItemDb.ITEM_TYPE_CHECKBOX) {
            QuestionnaireFormSubitemDb qfsd = new QuestionnaireFormSubitemDb();
            Iterator ir = qfsd.listSubItems(itemId).iterator();
            while (ir.hasNext()) {
                qfsd = (QuestionnaireFormSubitemDb) ir.next();
                qfsd.del();
            }
        }
        return qfid.del();
    }

}

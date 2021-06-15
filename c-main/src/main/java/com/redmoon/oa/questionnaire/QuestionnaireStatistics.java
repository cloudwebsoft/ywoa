package com.redmoon.oa.questionnaire;

import java.util.*;

import com.cloudwebsoft.framework.db.JdbcTemplate;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.*;

public class QuestionnaireStatistics {
    public QuestionnaireStatistics() {
    }
    
    public static String[][] getNumOfKind() {
    	return null;
    }
    
    /**
     * 取得真正的票数
     * @param itemId
     * @return
     * @throws ErrMsgException
     */
    public int[] itemValueRealStatistics(int itemId) throws ErrMsgException {
        QuestionnaireFormItemDb qfid = new QuestionnaireFormItemDb();
        qfid = qfid.getQuestionnaireFormItemDb(itemId);
        QuestionnaireFormSubitemDb qfsd = new QuestionnaireFormSubitemDb();
        Vector items = qfsd.listSubItems(itemId);
        int size = items.size();
        int[] optionValues = new int[size];
        String sql = "select id from oa_questionnaire_item where item_id=" + itemId;
        QuestionnaireItemDb qid = new QuestionnaireItemDb();
        // 取出所有的答案项
        Vector vItems = qid.list(sql);
        Iterator iItems = vItems.iterator();
        if (qfid.getItemType() == QuestionnaireFormItemDb.ITEM_TYPE_CHECKBOX) {
            while (iItems.hasNext()) {
                qid = (QuestionnaireItemDb) iItems.next();
                int weight = qid.getWeight();
                sql = "select subitem_id from oa_questionnaire_subitem where questionnaire_num=" + qid.getQuestionnaireNum();
                QuestionnaireSubitemDb qsd = new QuestionnaireSubitemDb();
                Vector vSubitems = qsd.list(sql);
                Iterator iSubitems = vSubitems.iterator();
                while (iSubitems.hasNext()) {
                    qsd = (QuestionnaireSubitemDb) iSubitems.next();
                    int subId = qsd.getSubitemValue();
                    for (int k=0; k<size; k++) {
                        if (subId==((QuestionnaireFormSubitemDb)items.elementAt(k)).getId())
                            optionValues[k] += 1;
                    }
                }
            }
        } else if (qfid.getItemType() == QuestionnaireFormItemDb.ITEM_TYPE_RADIO_GROUP) {
            while (iItems.hasNext()) {
                qid = (QuestionnaireItemDb) iItems.next();
                int weight = qid.getWeight();                
                int subId = Integer.parseInt(qid.getItemValue());
                for (int k = 0; k < size; k++) {
                    if (subId == ((QuestionnaireFormSubitemDb) items.elementAt(k)).getId()) {
                        optionValues[k] += 1;
                        break;
                    }
                }
            }
        } else {
            throw new ErrMsgException("不支持统计的项目类型！");
        }
        return optionValues;
    }    

    /**
     * 取得票数*权重
     * @param itemId
     * @return
     * @throws ErrMsgException
     */
    public int[] itemValueStatistics(int itemId) throws ErrMsgException {
        QuestionnaireFormItemDb qfid = new QuestionnaireFormItemDb();
        qfid = qfid.getQuestionnaireFormItemDb(itemId);
        QuestionnaireFormSubitemDb qfsd = new QuestionnaireFormSubitemDb();
        Vector items = qfsd.listSubItems(itemId);
        int size = items.size();
        int[] optionValues = new int[size];
        String sql = "select id from oa_questionnaire_item where item_id=" + itemId;
        QuestionnaireItemDb qid = new QuestionnaireItemDb();
        // 取出所有的答案项
        Vector vItems = qid.list(sql);
        Iterator iItems = vItems.iterator();
        if (qfid.getItemType() == QuestionnaireFormItemDb.ITEM_TYPE_CHECKBOX) {
            while (iItems.hasNext()) {
                qid = (QuestionnaireItemDb) iItems.next();
                int weight = qid.getWeight();
                sql = "select subitem_id from oa_questionnaire_subitem where questionnaire_num=" + qid.getQuestionnaireNum();
                QuestionnaireSubitemDb qsd = new QuestionnaireSubitemDb();
                Vector vSubitems = qsd.list(sql);
                Iterator iSubitems = vSubitems.iterator();
                while (iSubitems.hasNext()) {
                    qsd = (QuestionnaireSubitemDb) iSubitems.next();
                    int subId = qsd.getSubitemValue();
                    for (int k=0; k<size; k++) {
                        if (subId==((QuestionnaireFormSubitemDb)items.elementAt(k)).getId())
                            optionValues[k] += 1*weight;
                    }
                }
            }
        } else if (qfid.getItemType() == QuestionnaireFormItemDb.ITEM_TYPE_RADIO_GROUP) {
            while (iItems.hasNext()) {
                qid = (QuestionnaireItemDb) iItems.next();
                int weight = qid.getWeight();                
                int subId = Integer.parseInt(qid.getItemValue());
                for (int k = 0; k < size; k++) {
                    if (subId == ((QuestionnaireFormSubitemDb) items.elementAt(k)).getId()) {
                        optionValues[k] += 1*weight;
                        break;
                    }
                }
            }
        } else {
            throw new ErrMsgException("不支持统计的项目类型！");
        }
        return optionValues;
    }
    
    /**
     * 取得问卷题目的参与人数
     * @param formId
     * @return
     */
    public static int getNumOfJoin(int formId, int itemId) {
		int count = 0;
    	String sql = "select count(user_name) from oa_questionnaire_item where form_id=" + formId + " and item_id=" + itemId;
    	try {
    		JdbcTemplate jdbc = new JdbcTemplate();
    		ResultIterator ri = jdbc.executeQuery(sql);	
    		int row = 0;
    		while(ri.hasNext()) {
    			ResultRecord  rr = (ResultRecord)ri.next();
    			count = rr.getInt(1);
    			row++;
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}    	
		return count;
    }
}

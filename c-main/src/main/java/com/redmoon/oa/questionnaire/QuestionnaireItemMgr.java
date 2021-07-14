package com.redmoon.oa.questionnaire;

import java.util.*;

import javax.servlet.http.*;
import cn.js.fan.util.*;
import com.redmoon.oa.db.*;
import org.apache.log4j.*;

import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.pvg.UserGroupDb;

public class QuestionnaireItemMgr {
    Logger logger = Logger.getLogger(QuestionnaireItemMgr.class.getName());

    public QuestionnaireItemMgr() {
    }

    public boolean create(HttpServletRequest request) throws ErrMsgException {
        Privilege privilege = new Privilege();
        boolean flag = false;
        int questionnaireNum = (int) SequenceManager.nextID(SequenceManager.QUESTIONNAIRE_NUM);
        int formId = ParamUtil.getInt(request,"form_id");
        String errmsg = "";
        String kind = ""; // 当有多条权限满足时，取权重最大的角色对应的投票种类
        Vector vItems = new Vector();
        String [] itemIds = StrUtil.split(ParamUtil.get(request, "sItemId"), ":");
        if (itemIds==null)
        	throw new ErrMsgException("问卷题目尚未设置!");
        for(int i=0;i<itemIds.length;i++) {// 首先遍历一遍检测项目是否填写完整
            int itemId = Integer.parseInt(itemIds[i]);
            QuestionnaireFormItemDb qfid = new QuestionnaireFormItemDb();
            qfid = qfid.getQuestionnaireFormItemDb(itemId);
            vItems.add(qfid);
            String itemName = qfid.getItemName();
            int itemType = qfid.getItemType();
            int checkedType = qfid.getCheckedType();
            String inputName = "item" + itemId;
            if(checkedType==QuestionnaireFormItemDb.MUST_BE_FILLED) {//如果为必须填写项目
                if (itemType == QuestionnaireFormItemDb.ITEM_TYPE_CHECKBOX) {//如果是多选类型
                    String[] itemValues = request.getParameterValues(inputName);
                    if (itemValues == null) {
                        errmsg +="问卷项目：“" + itemName + "”填写不完整！\\n";
                    }
                } else {//其他类型
                    String itemValue = ParamUtil.get(request, inputName);
                    if (itemValue.equals("")) {
                        errmsg += "问卷项目：“" + itemName + "”填写不完整！\\n";
                    }
                }
            }
        }
        
        // 根据权限检查选择项是否有超出了规定的数量
        UserDb user = new UserDb();
        UserGroupDb[] groups = null;
        user = user.getUserDb(privilege.getUser(request));
        if (user.isLoaded()) {
            groups = user.getGroups();
        }
        RoleDb[] roles = user.getRoles();
        // 取用户所在的角色或用户组所赋予的最大值
        int maxA = 0, maxB = 0, maxC = 0, maxD = 0, maxE = 0, maxF = 0;
        int maxWeight = 1;
        QuestionnairePriv lp = new QuestionnairePriv();
        Iterator ir = lp.list(formId).iterator();
        while (ir.hasNext()) {
        	lp = (QuestionnairePriv)ir.next();
        	int weight = lp.getWeight();
        	
        	int limitA = lp.getLimitA();
        	int limitB = lp.getLimitB();
        	int limitC = lp.getLimitC();
        	int limitD = lp.getLimitD();
        	int limitE = lp.getLimitE();
        	int limitF = lp.getLimitF();
        	
        	boolean isCanSee = false;
        	//　权限项对应的是组用户
            if (lp.getType() == QuestionnairePriv.TYPE_USERGROUP) {
                // 组为everyone
                if (lp.getName().equals(UserGroupDb.EVERYONE)) {
                    if (lp.getAppend() == 1) {
                    	isCanSee = true;
                    	if (weight > maxWeight) {
                    		maxWeight = weight;
                        	kind = lp.getKind();
                    	}
                    }
                } else {
                    if (groups != null) {
                        int len = groups.length;
                        // 判断该用户所在的组是否有权限
                        for (int i = 0; i < len; i++) {
                            if (groups[i].getCode().equals(lp.getName())) {
                                if (lp.getSee() == 1) {
                                	isCanSee = true;
                                	if (weight > maxWeight) {
                                		maxWeight = weight;
                                    	kind = lp.getKind();                                		
                                	}                                	
                                }
                                break;
                            }
                        }
                    }
                }
            }
            else if (lp.getType()==QuestionnairePriv.TYPE_ROLE) {
                if (roles!=null) {
                        // 判断该用户所属的角色是否有权限
                        int len = roles.length;
                        for (int i = 0; i < len; i++) {
                        	if (lp.getName().equals(RoleDb.CODE_MEMBER)) {
                                if (lp.getSee() == 1) {
                                	isCanSee = true;
                                	if (weight > maxWeight) {
                                		maxWeight = weight;
                                    	kind = lp.getKind();
                                	}                                	
                                }
                                break;                        		
                        	}
                        	else if (roles[i].getCode().equals(lp.getName())) {
                                if (lp.getSee() == 1) {
                                	isCanSee = true;
                                	if (weight > maxWeight) {
                                		maxWeight = weight;
                                    	kind = lp.getKind();                                		
                                	}                                	
                                }
                                break;
                            }
                        }
                }
            }
            else if (lp.getType()==QuestionnairePriv.TYPE_USER) { //　个人用户
                if (lp.getName().equals(user.getName())) {
                    if (lp.getSee() == 1) {
                    	isCanSee = true;
                    	if (weight > maxWeight) {
                    		maxWeight = weight;
                        	kind = lp.getKind();    		
                    	}
                    }
                }
            }
            if (isCanSee) {
            	if (limitA > maxA) {
            		maxA = limitA;
            	}
            	if (limitB > maxB) {
            		maxB = limitB;
            	}
            	if (limitC > maxC) {
            		maxC = limitC;
            	}
            	if (limitD > maxD) {
            		maxD = limitD;
            	}
            	if (limitE > maxE) {
            		maxE = limitE;
            	}
            	if (limitF > maxF) {
            		maxF = limitF;
            	}            	
            }
        }
        
      	String[] ary = {"A", "B", "C", "D", "E", "F"};        
        int[] maxLimit = {maxA, maxB, maxC, maxD, maxE, maxF};
        
        int[] counts = new int[6];
        for (int i=0; i<counts.length; i++) {
        	counts[i] = 0;
        }
        Iterator iItems = vItems.iterator();
        while(iItems.hasNext()) {
            QuestionnaireFormItemDb qfid = (QuestionnaireFormItemDb)iItems.next();
            int itemId = qfid.getItemId();
            int itemType = qfid.getItemType();
            if(itemType==QuestionnaireFormItemDb.ITEM_TYPE_RADIO_GROUP) {
                String inputName = "item" + itemId;
                int itemValue = ParamUtil.getInt(request, inputName, -1);
    			Iterator irSub = qfid.getSubItems().iterator();
    			int i = 0;
				while (irSub.hasNext()) {
					QuestionnaireFormSubitemDb qfsd = (QuestionnaireFormSubitemDb)irSub.next();
					if (itemValue==qfsd.getId()) {
						counts[i]++;
						break;
					}
					i++;
				}
            } else if(itemType == QuestionnaireFormItemDb.ITEM_TYPE_CHECKBOX) {
                String inputName = "item" + itemId;
                String [] itemValues = request.getParameterValues(inputName);
                if(itemValues == null) {//前面已经检测必须填写的项目，这里为空说明用户没有填写非必须填写的项目，不需要处理
                    continue;
                }
    			Iterator irSub = qfid.getSubItems().iterator();
    			int i = 0;
				while (irSub.hasNext()) {
					QuestionnaireFormSubitemDb qfsd = (QuestionnaireFormSubitemDb)irSub.next();
					for (int k=0; k<itemValues.length; k++) {
						if (itemValues[k].equals(String.valueOf(qfsd.getId()))) {
							counts[i]++;
							break;
						}
					}
					i++;
				}                
            }
        }      
        
        for (int i=0; i<counts.length; i++) {
        	if (maxLimit[i]==0) {
        		continue;
        	}
        	if (counts[i]>maxLimit[i]) {
        		errmsg += "选项" + ary[i] + "不能超过" + maxLimit[i] + "个\\n";
        	}
        }
        
        if (!errmsg.equals("")) {
            throw new ErrMsgException(errmsg);
        }
        
        iItems = vItems.iterator();
        while(iItems.hasNext()) {
            QuestionnaireFormItemDb qfid = (QuestionnaireFormItemDb)iItems.next();
            int itemId = qfid.getItemId();
            int itemType = qfid.getItemType();
            if(itemType==QuestionnaireFormItemDb.ITEM_TYPE_INPUT || itemType==QuestionnaireFormItemDb.ITEM_TYPE_TEXTAREA || itemType==QuestionnaireFormItemDb.ITEM_TYPE_RADIO_GROUP) {
                String inputName = "item" + itemId;
                String itemValue = ParamUtil.get(request, inputName);
                QuestionnaireItemDb qid = new QuestionnaireItemDb();
                qid.setQuestionnaireNum(questionnaireNum);
                qid.setItemId(itemId);
                qid.setItemValue(itemValue);
                String userName = privilege.isUserLogin(request) ? privilege.getUser(request) : "匿名用户";
                qid.setUserName(userName);
                qid.setFormId(formId);
                qid.setWeight(maxWeight);
                qid.setKind(kind);
                flag = qid.create();
            } else if(itemType == QuestionnaireFormItemDb.ITEM_TYPE_CHECKBOX) {
                String inputName = "item" + itemId;
                String [] itemValues = request.getParameterValues(inputName);
                if(itemValues == null) {//前面已经检测必须填写的项目，这里为空说明用户没有填写非必须填写的项目，不需要处理
                    continue;
                }
                QuestionnaireItemDb qid = new QuestionnaireItemDb();
                qid.setQuestionnaireNum(questionnaireNum);
                qid.setItemId(itemId);
                qid.setItemValue("复选框，内容见附表");
                String userName = privilege.isUserLogin(request) ? privilege.getUser(request) : "匿名用户";
                qid.setUserName(userName);
                qid.setFormId(formId);
                qid.setWeight(maxWeight);
                qid.setKind(kind);                
                flag = qid.create();
                for(int k=0; k<itemValues.length; k++) {
                    QuestionnaireSubitemDb qsd = new QuestionnaireSubitemDb();
                    qsd.setQuestionnaireNum(questionnaireNum);
                    qsd.setItemId(itemId);
                    qsd.setSubitemValue(StrUtil.toInt(itemValues[k]));
                    int questionnaireFormId = qfid.getFormId();
                    qsd.setQuestionnaireFormId(questionnaireFormId);
                    flag = qsd.create();
                }
            }
        }
        return flag;
    }
}

package com.redmoon.oa.android.registrationApproval;



import org.json.JSONException;
import com.redmoon.oa.android.base.BaseAction;
import com.redmoon.oa.kernel.License;

/**
 * @Description: 
 * @author: 
 * @Date: 2015-12-15下午04:41:52
 */
public class LiscenseByTypeAction extends BaseAction{


	@Override
	public void executeAction() {
		// TODO Auto-generated method stub
		super.executeAction();
	
		
		try {
			jReturn.put(RES, String.valueOf(RESULT_SUCCESS));
			jResult.put(RETURNCODE, RETURNCODE_SUCCESS);
			License license = License.getInstance();
			String type = license.getType();
			jResult.put(RETURNCODE, RETURNCODE_SUCCESS);
			jResult.put(DATA, type);
			jReturn.put(RESULT, jResult);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			logger.error(LiscenseByTypeAction.class.getName()+":"+e.getMessage());
		}
		
	} 
	
	
	
	

	
}

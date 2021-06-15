package com.redmoon.oa.superCheck;

import java.util.Random;

import org.apache.log4j.Logger;

import SuperDog.Dog;
import SuperDog.DogStatus;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;

import com.redmoon.oa.kernel.License;

public class CheckSuperKey {
	Logger logger = Logger.getLogger( CheckSuperKey.class.getName() );
	private volatile static CheckSuperKey csk;
	private CheckSuperKey(){
	}
	public static CheckSuperKey getInstance(){
		if(csk==null){
            synchronized(CheckSuperKey.class){
                if(csk==null){
                	csk=new CheckSuperKey();
                }
            }
        }
        return csk;

	}
	public int checkKey() throws Exception {
		boolean isTemp = false;
        java.util.Date eDate = License.getInstance().getExpiresDate();		
		// 3年以内，则认为是临时许可证
        if (DateUtil.datediff(eDate, new java.util.Date()) < 3*365) {
        	isTemp = true;
        }
		// 20161219 临时许可证时取消对于软件狗的检测，以免域名为*时出现问题
		if (isTemp) {
			return DogStatus.DOG_STATUS_OK;
		}		
		
		String enterpriseNum = License.getInstance().getEnterpriseNum();
		if (enterpriseNum == null || enterpriseNum.equals("")
				|| enterpriseNum.equals("yimi") || enterpriseNum.equals("OA")
				|| enterpriseNum.equals("ywrj")
				|| enterpriseNum.equals("yimihome")) {
			return DogStatus.DOG_STATUS_OK;
		}
		
		String domain = License.getInstance().getDomain();		
		if (enterpriseNum.equals("OA")) {
			if (domain.equals("*")) {
				throw new ErrMsgException("企业号为OA，许可证非法！"); 
			}
		}
		
		String type = License.getInstance().getType();
		int status = DogStatus.DOG_STATUS_OK; 
		// OEM版或者SRC版且未绑定域名则进行超级狗校验
		// fgf 20161010 OEM版且未绑定域名则进行超级狗校验
		// if ((type.equals(License.TYPE_OEM) || (type.equals(License.TYPE_SRC))) && (domain.equals("*"))){
		// OEM不再限制IP
		if (false && type.equals(License.TYPE_OEM) && domain.equals("*")){
			int i, j;
			i = j = 0; 
	
			Dog curDog = new Dog(EncryptionArray.GENARR_FEATUREID);
	
			/**********************************************************************
			 * login establish a context for SuperDog
			 */
			/* login feature which you selected */
			curDog.login(Vcode.strVendorCode);
			status = curDog.getLastError();
	
			if (DogStatus.DOG_STATUS_OK != status) {
				if (status == DogStatus.DOG_INV_VCODE) {
					logger.error("Invalid vendor code.");
				} else if (status == DogStatus.DOG_UNKNOWN_VCODE) {
					logger.error("Vendor Code not recognized by API.");
				} else {
					logger.error("Login to feature failed with status: "
							+ status);
				}
				return status;
			}
	
			// Generate a random index number
			Random random = new Random();
			i = (int) (Math.random() * EncryptionArray.GENERATE_COUNT);
	
			byte queryData[] = new byte[EncryptionArray.ENCRYPTDATA_LEN];
			for (j = 0; j < EncryptionArray.ENCRYPTDATA_LEN; ++j) {
				queryData[j] = EncryptionArray.encryptionArray[i][0][j];
			}
	
			/**********************************************************************
			 * encrypt function encrypts a block of data using SuperDog (minimum
			 * buffer size is 16 bytes)
			 */
			curDog.encrypt(queryData);
			status = curDog.getLastError();
	
			if (DogStatus.DOG_STATUS_OK != status) {
				logger.error("Dog encrypt failed with status: " + status);
			}
	
			for (j = 0; j < EncryptionArray.ENCRYPTDATA_LEN; ++j) {
				if (0 != (queryData[j] ^ EncryptionArray.encryptionArray[i][1][j])) {
					logger.error("Encrypted data is wrong.");
					curDog.logout();
					return -1;
				}
			}
			curDog.logout();
			status = curDog.getLastError();
		}
		return status;
	}
}

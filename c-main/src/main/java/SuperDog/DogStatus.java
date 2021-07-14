/*
 *  Copyright (C) 2014 SafeNet, Inc. All rights reserved.
 *
 */

package SuperDog;

public class DogStatus
{
	/**
	 * return codes 
	 */
	/** Request successfully completed */
	public static final int DOG_STATUS_OK = 0;

	/** Request exceeds data file range */
	public static final int DOG_MEM_RANGE = 1;

	/** System is out of memory */
	public static final int DOG_INSUF_MEM = 3;

	/** Too many open login sessions */
	public static final int DOG_TMOF = 4;
	
	/** Access denied */
	public static final int DOG_ACCESS_DENIED = 5;	

	/** Required SuperDog not found */
	public static final int DOG_NOT_FOUND = 7;

	/** Encryption/decryption data length is too short */
	public static final int DOG_TOO_SHORT = 8;

	/** Invalid input handle */
	public static final int DOG_INV_HND = 9;

	/** Specified File ID not recognized by API */
	public static final int DOG_INV_FILEID = 10;

	/** Invalid XML format */
	public static final int DOG_INV_FORMAT = 15;

	/** Unable to execute function in this context */
	public static final int DOG_REQ_NOT_SUPP = 16;

	/** SuperDog to be updated not found */
	public static final int DOG_KEYID_NOT_FOUND = 18;

	/** Required XML tags not found; Contents in binary data are missing
	 * or invalid */
	public static final int DOG_INV_UPDATE_DATA = 19;

	/** Update not supported by SuperDog */
	public static final int DOG_INV_UPDATE_NOTSUPP = 20;

	/** Update counter is set incorrectly */
	public static final int DOG_INV_UPDATE_CNTR = 21;

	/** Invalid Vendor Code passed */
	public static final int DOG_INV_VCODE = 22;

	/** Passed time value is outside supported value range */
	public static final int DOG_INV_TIME = 24;

	/** Acknowledge data requested by the update; however the ack_data
	 * input parameter is NULL */
	public static final int DOG_NO_ACK_SPACE = 26;

	/** Program running on a terminal server */
	public static final int DOG_TS_DETECTED = 27;

	/** Unknown algorithm used in V2C file */
	public static final int DOG_UNKNOWN_ALG = 29;

	/** Signature verification failed */
	public static final int DOG_INV_SIG = 30;

	/** Requested Feature not available */
	public static final int DOG_FEATURE_NOT_FOUND = 31;
	
	/** Communication error between API and local SuperDog License Manager */
	public static final int DOG_LOCAL_COMM_ERR = 33;

	/** Vendor Code not recognized by API */
	public static final int DOG_UNKNOWN_VCODE = 34;

	/** Invalid XML specification */
	public static final int DOG_INV_SPEC = 35;

	/** Invalid XML scope */
	public static final int DOG_INV_SCOPE = 36;

	/** Too many SuperDog currently connected */
	public static final int DOG_TOO_MANY_KEYS = 37;

	/** Session was interrupted */
	public static final int DOG_BROKEN_SESSION = 39;

	/** Feature has expired */
	public static final int DOG_FEATURE_EXPIRED = 41;

	/** SuperDog License Manager version too old */
	public static final int DOG_OLD_LM = 42;
	
	/** USB error occurred when communicating with a SuperDog */
	public static final int DOG_DEVICE_ERR = 43;

	/** System time has been tampered with */
	public static final int DOG_TIME_ERR = 45;

	/** Communication error occurred in secure channel */
	public static final int DOG_SCHAN_ERR = 46;

	/** Unable to locate a Feature matching the scope */
	public static final int DOG_SCOPE_RESULTS_EMPTY = 50;

	/** Trying to install a V2C file with an update counter that is out
	 * of sequence with the update counter in the SuperDog.
	 * The values of the update counter in the file are lower than
	 * those in the SuperDog. */
	public static final int DOG_UPDATE_TOO_OLD = 54;

	/** Trying to install a V2C file with an update counter that is out
	 * of sequence with the update counter in the SuperDog.
	 * The first value of the update counter in the file is greater than
	 * the value in the SuperDog. */
	public static final int DOG_UPDATE_TOO_NEW = 55;

	/** Unable to locate dynamic library for API */
	public static final int DOG_NO_API_DYLIB = 400;

	/** Dynamic library for API is invalid */
	public static final int DOG_INV_API_DYLIB = 401;

	/** Object incorrectly initialized */
	public static final int DOG_INVALID_OBJECT = 500;

	/** Invalid function parameter */
	public static final int DOG_INVALID_PARAMETER = 501;

	/** Logging in twice to the same object */
	public static final int DOG_ALREADY_LOGGED_IN = 502;

	/** Logging out twice of the same object */
	public static final int DOG_ALREADY_LOGGED_OUT = 503;

	/** Incorrect use of system or platform */
	public static final int DOG_OPERATION_FAILED = 525;

	/** Requested function not implemented; In the case of the API Dispatcher; API DLL too old */
	public static final int DOG_NOT_IMPL = 698;

	/** Internal error occurred in API */
	public static final int DOG_INT_ERR = 699;

	public static String runtime_library_x86_windows = "DogJava";
	public static String runtime_library_x64_windows = "DogJava_x64";

	public static String runtime_library_x86_linux = "DogJava";
	public static String runtime_library_x64_linux = "DogJava_x86_64";
	
	public static void Init()
	{
		String operatingSystem = System.getProperty("os.name");
		String architecture = System.getProperty("os.arch");

		try
		{
			/* Windows library loading */
			if (operatingSystem.indexOf("Windows") != -1)
			{
				if (architecture.equals("x86"))
				{
					System.loadLibrary(runtime_library_x86_windows);
				}
				else if (architecture.equals("x86_64") || architecture.equals("amd64"))
				{
					System.loadLibrary(runtime_library_x64_windows);
				}				
				else
				{
					System.loadLibrary(runtime_library_x86_windows);
				} 
			}
			else
			{
				/* Linux library loading */
				if (operatingSystem.indexOf("Linux") != -1)
				{
					if (architecture.equals("x86"))
					{
						System.loadLibrary(runtime_library_x86_linux);
					}
					else if(architecture.equals("x86_64") || architecture.equals("amd64"))
					{
						System.loadLibrary(runtime_library_x64_linux);
					}
					else
					{
						System.loadLibrary(runtime_library_x86_linux);
					}
			 	}
			 	else {
					System.loadLibrary(runtime_library_x86_windows);
			 	}
			}
		}
		catch (UnsatisfiedLinkError e) {
			com.redmoon.oa.Config oacfg = new com.redmoon.oa.Config();
			oacfg.put("systemIsOpen", "false");
			oacfg.put("systemStatus", "请使用正版授权系统");
			throw e;
		}
	}
}

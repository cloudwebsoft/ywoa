/*
 *  Copyright (C) 2012 SafeNet, Inc. All rights reserved.
 *  Use is subject to license terms.
 */

package SuperDog;

import java.nio.*;
import SuperDog.DogStatus;
import SuperDog.DogTime;
import SuperDog.DogApiVersion;

public class Dog 
{
  /**
   * handle - pointer to the resulting session handle.
   */
  private int[] handle = {0};

  /**
   * Unique identifier of the Feature.
   */
  private long featureid;

  /**
   * Status of the last function call.
   */
  private int status;

  /**
   * getSessionInfo() format to retrieve update info (C2V).
   */
  public static final String DOG_UPDATEINFO  = new String("<dogformat format=\"updateinfo\"/>"); 

  /**
   * getSessionInfo() format to retrieve session info.
   */
  public static final String DOG_SESSIONINFO = new String("<dogformat format=\"sessioninfo\"/>");  

  /**
   * getSessionInfo() format to retrieve SuperDog info.
   */
  public static final String DOG_KEYINFO     = new String("<dogformat format=\"keyinfo\"/>");

  /** 
   * The default Feature ID.
   * Present in every SuperDog.
   */
  public static final long DOG_DEFAULT_FID = 0;

  /** 
   * Minimal block size for encrypt() and decrypt() methods.
   */
  public static final int DOG_MIN_BLOCK_SIZE = 16;
    
  /** 
   * File ID for default read-write data file.
   */
  public static final long DOG_FILEID_RW = 0xfff4;

  /**
   * Returns the error that occurred in the last function call.
   */
  public int getLastError()
  {
    return status;
  }

  static
  {
    DogStatus.Init();
  }

  /*
   * private native methods 
   */
  private static native int Login(long feature_id,String vendor_code,int handle[]);
  private static native int LoginScope(long feature_id,String scope,String vendor_code,int handle[]);
  private static native int Logout(int handle);
  private static native int Encrypt(int handle, byte buffer[], int length);
  private static native String EncryptString(int handle, String buffer, int status[]);
  private static native int Decrypt(int handle, byte buffer[], int length);
  private static native String DecryptString(int handle, String buffer, int status[]);
  private static native int GetSize(int handle, long fileid, int size[]);
  private static native int Read(int handle, long fileid, int offset, int length, byte buffer[]);
  private static native int Write(int handle, long fileid, int offset, int length, byte buffer[]);
  private static native int GetTime(int handle, long time[]);
  private static native byte[] GetInfo(String scope,String format,String vendor_code,int status[]);
  private static native byte[] GetSessioninfo(int handle,String format,int status[]);
  private static native String Update(String update_data,int status[]);
  private static native void Free(long info);


  /**
   * Dog constructor.
   *
   *
   * @param feature_id   Unique identifier of the Feature.
   *
   */
  public Dog(long feature_id) 
  {
    status = DogStatus.DOG_STATUS_OK;
    featureid = feature_id;
    handle[0]=0;
  }

  /*
   * Attempt to logout if object is finalized
   */
  protected void finalize() 
  {
    logout();
  }

  /**
   * Logs in to a Feature.
   *
   * Establishes a session context.
   * <p>
   * If a previously established session context exists, the session
   * will be logged out.
   *
   * @param vendor_code      The Vendor Code.
   *
   * @return true/false - indicates success or failure.
   *
   * @see #loginScope
   * @see #logout
   * @see #getLastError
   */
  public boolean login(String vendor_code) 
  {
    if (vendor_code == null)
        status = DogStatus.DOG_INV_VCODE;
    else {
        synchronized(this) 
		{
            logout();
            status = Dog.Login(featureid, vendor_code, handle);
        }
    }
    return (status == DogStatus.DOG_STATUS_OK);
  }

  /**
   * Logs in to a Feature according to customizable search parameters.
   *
   * This function is an extended login function, where the search for the
   * Feature can be restricted.
   * <p>
   * If a previously established session context exists, the session
   * will be logged out.
   *
   * @param scope            The dog_scope of the Feature search.
   * @param vendor_code      The Vendor Code.
   *
   * @return true/false - indicates success or failure.
   *
   * @see #login
   * @see #logout
   * @see #getLastError
   */
  public boolean loginScope(String scope, String vendor_code)
  {
    if (vendor_code == null)
        status = DogStatus.DOG_INV_VCODE;
    else if (scope == null)
        status = DogStatus.DOG_INV_SCOPE;
    else 
	{
        synchronized(this)
		{
            logout();
            status = Dog.LoginScope(featureid, scope, vendor_code,handle);
        }
    }
    return (status == DogStatus.DOG_STATUS_OK);
  }

  /**
   * Logs out from a session and frees all allocated resources for the session.
   *
   * @return true/false - indicates success or failure.
   *
   * @see #login
   * @see #getLastError
   */
  public boolean logout() 
  {
    if (handle[0]==0)
    {
      status = DogStatus.DOG_INV_HND;
      return true;
    }
    synchronized(this)
	{
        status = Dog.Logout(handle[0]);
        if (status == DogStatus.DOG_STATUS_OK)
            handle[0]=0;
    }
    return (status == DogStatus.DOG_STATUS_OK);
  }

  /** 
   * Encrypts a buffer. 
   *
   * This is the reverse operation of the decrypt() method. 
   * <p>
   * If the encryption fails (e.g. Dog removed during the process) the 
   * data buffer is unmodified.
   *
   * @param buffer      The buffer to be encrypted.
   * 			(16 bytes minimum).
   *
   * @return true/false - indicates success or failure.
   *
   * @see #decrypt
   * @see #getLastError
   */
  public boolean encrypt(byte[] buffer) 
  {
    if (buffer == null)
        status = DogStatus.DOG_INVALID_PARAMETER;
    else
        status = Dog.Encrypt(handle[0], buffer, buffer.length);
    return (status == DogStatus.DOG_STATUS_OK);
  }
  
  /** 
   * Encrypts a string. 
   *
   * This is the reverse operation of the decryptString() method. 
   * <p>
   * If the encryption fails (e.g. Dog removed during the process) the 
   * data buffer is unmodified.
   *
   * @param  buffer      The string to be encrypted.
   *
   * @return encryptedString - The encrypted string.
   *
   * @see #decryptString
   * @see #getLastError
   */
  public String encryptString(String buffer) 
  {
	int[] dll_status = {0};
	String s = null;

    if (buffer == null)
    {
    	status = DogStatus.DOG_INVALID_PARAMETER;
    	return null;
    }
           
    s = Dog.EncryptString(handle[0], buffer, dll_status);
    status = dll_status[0];
    
    return s;
  }
    
  /** 
   * Decrypts a buffer.
   *
   * This is the reverse operation of the encrypt() method. 
   * <p>
   * If the decryption fails (e.g. Dog removed during the process) the 
   * data buffer is unmodified.
   *
   * @param buffer      The buffer to be decrypted.
   * 				(16 bytes minimum).
   *
   * @return true/false - indicates success or failure.
   *
   * @see #encrypt
   * @see #getLastError
   */
  public boolean decrypt(byte[] buffer) 
  {
    if (buffer == null)
        status = DogStatus.DOG_INVALID_PARAMETER;
    else
        status = Dog.Decrypt(handle[0], buffer, buffer.length);
    return (status == DogStatus.DOG_STATUS_OK);
  }
  
  /** 
   * Decrypts a string.
   *
   * This is the reverse operation of the encryptString() method. 
   * <p>
   * If the decryption fails (e.g. Dog removed during the process) the 
   * data buffer is unmodified.
   *
   * @param  buffer      The string to be decrypted.
   *
   * @return decryptedString - The decrypted string.
   *
   * @see #encryptString
   * @see #getLastError
   */
  public String decryptString(String buffer) 
  {
	int[] dll_status = {0};
	String s = null;
	  
	if (buffer == null)
	{
		status = DogStatus.DOG_INVALID_PARAMETER;
		return null;
	}
     
    s = Dog.DecryptString(handle[0], buffer, dll_status);
    status = dll_status[0];
    return s;
  }

  /** 
   * Retrieves information about SuperDog. 
   * 
   * Acquires information about SuperDog. 
   * The programmer can choose the scope and output structure of the data.
   * The function has a "scope" parameter that defines the scope using 
   * XML syntax.
   * <p>
   * This function is not used in a login context, so it can be used 
   * in a generic "Monitor" application.
   * <p>
   * @param      scope       XML definition of the information scope.
   * @param      format      XML definition of the output data structure.
   * @param      vendor_code The Vendor Code.
   * @return     info        - The returned information (XML list).
   *
   * @see #getSessionInfo
   * @see #getLastError
   */
  public String getInfo(String scope, String format, String vendor_code)
  {
    byte[] info={0};
    int[] status1 = {0};
    String s=null;

    status = DogStatus.DOG_STATUS_OK;
    if (vendor_code == null)
        status = DogStatus.DOG_INV_VCODE;
    else if (scope == null)
        status = DogStatus.DOG_INV_SCOPE;
    else if (format == null)
        status = DogStatus.DOG_INV_FORMAT;
    if (status != DogStatus.DOG_STATUS_OK)
        return null;

    info = Dog.GetInfo(scope, format, vendor_code, status1);

    status = status1[0];
    if( status == DogStatus.DOG_STATUS_OK)
      s = new String(info);
  
    return s;
  }

  /** 
   * Retrieves information regarding a session context.
   *
   * @param      format       XML definition of the output data structure.
   * @return     info         - The returned information (XML list).
   *
   * @see #getLastError
   */
  public String getSessionInfo(String format)
  {
    byte[] info={0};
    int[] status1 = {0};
    String s=null;

    if (format == null) 
	{
        status = DogStatus.DOG_INV_FORMAT;
        return null;
    }

    info = Dog.GetSessioninfo(handle[0], format, status1);

    status = status1[0];
    if( status == DogStatus.DOG_STATUS_OK)
      s = new String(info);
  
    return s;
  }

  /**
   * Reads a data file.
   * 
   * @param fileid       ID of the file to read.
   * @param offset       Position in the file.
   * @param buffer       Buffer for the retrieved data.
   *
   * @return true/false - indicates success or failure.
   *
   * @see #getLastError
   * @see #write
   * @see #getSize
   */
  public boolean read(long fileid, int offset, byte[] buffer) 
  {
    if (buffer == null)
      status = DogStatus.DOG_INVALID_PARAMETER;
    else if (offset < 0)
      status = DogStatus.DOG_INVALID_PARAMETER;
    else
      status = Dog.Read(handle[0], fileid, offset, buffer.length, buffer);
    return (status == DogStatus.DOG_STATUS_OK);
  }

  /**
   * Writes to a data file. 
   *
   * @param fileid       ID of the file to write.
   * @param offset       Position in the file.
   * @param buffer       The data to write.
   *
   * @return true/false - indicates success or failure.
   *
   * @see #getLastError
   * @see #read
   * @see #getSize
   */
  public boolean write(long fileid, int offset, byte[] buffer) 
  {
    if (buffer == null)
        status = DogStatus.DOG_INVALID_PARAMETER;
    else if (offset < 0)
        status = DogStatus.DOG_INVALID_PARAMETER;
    else
        status = Dog.Write(handle[0], fileid, offset, buffer.length, buffer);
    return (status == DogStatus.DOG_STATUS_OK);
  }

  /** 
   * Retrieves the byte size of a data file.
   *
   * @param  fileid       ID of the file to be queried.
   *
   * @return Size of the file.
   *
   * @see #getLastError
   * @see #read
   * @see #write
   */
  public int getSize(long fileid) 
  {
    int[] size = {0};
    status = Dog.GetSize(handle[0], fileid, size);    
    return size[0];
  }

  /**
   * Writes update information to a SuperDog. 
   * 
   * The update BLOB contains all necessary data to perform the update: 
   * Where to write (to which SuperDog), the necessary
   * access data (Vendor Code) and the update itself.
   * <p>
   * If requested by the update BLOB, the function returns an Acknowledge BLOB,
   * which is signed/encrypted by the updated instance and contains 
   * proof that this update was successfully installed. 
   *
   * @param      update_data      The complete update data.
   *
   * @return     ack_data - The acknowledged data (if requested).
   *
   * @see #getLastError
   */
  public String update(String update_data)
  {
    int[] dll_status = {0};
    String s = null;

    if (update_data == null)
	{
        status = DogStatus.DOG_INVALID_PARAMETER;
        return null;
    }

    s = Dog.Update(update_data, dll_status);
    status = dll_status[0];
     
    return s;
  }
  
  /** 
   * Reads the current time from a SuperDog.
   * 
   * Time values are returned as the number of seconds that have elapsed 
   * since Jan-01-1970 0:00:00 UTC.
   * <p>
   *
   * @return A DogTime object.
   */
  public DogTime getTime() 
  {
    long[] time={0};
    DogTime rtcTime;
    status = Dog.GetTime(handle[0], time);
    rtcTime = new DogTime(time[0]);
    if( status == DogStatus.DOG_STATUS_OK )
      status = rtcTime.getLastError();
    return rtcTime;
  }

  /** 
   * Reads the SuperDog API Version.
   *
   * @param vendor_code      The Vendor Code.
   *
   * @return A DogApiVersion object.
   *
   * @see #getLastError
   */
  public DogApiVersion getVersion(String vendor_code) 
  {
    DogApiVersion version;
    version = new DogApiVersion(vendor_code);
    status = version.getLastError();
    return version;
  }

  
}



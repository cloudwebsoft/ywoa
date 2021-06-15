/*
 *  Copyright (C) 2014 SafeNet, Inc. All rights reserved.
 *  Use is subject to license terms.
 */

package SuperDog;
import java.io.UnsupportedEncodingException;
import SuperDog.DogStatus;

public class DogApiVersion
{
   private int major_version[]={0};
   private int minor_version[]={0};
   private int build_server[]={0};
   private int build_number[]={0};
   private int status;

   /**
    * private native functions
    *
    */
       
   private static native int GetVersion(int major_version[],
                                       int minor_version[],
                                       int build_server[],
                                       int build_number[],
                                       byte vendor_code[]);


  /**
   * IA 64 not considered yet
   */
  static
  {
    DogStatus.Init();
  }

  /**
   * DogApiVersion constructor
   *
   * @param      vendor_code   The Vendor Code.
   *
   */
  public DogApiVersion(String vendor_code)
  {
	try
        {
            // Following code is added to ensure that byte array passed to JNI interface
            // is NULL terminated. Ideally the JNI GetVersion interface should be accepting
            // vendor_code as String like other JNI interface dog_login, dog_login_scope etc.
            // But changing JNI interface will result in incompatible function signature
            // Another solution will be to add new JNI interface
            int vc_bytes_count = vendor_code.length();
            byte tmp_vendor_code[] = new byte[vc_bytes_count + 1];

            System.arraycopy(vendor_code.getBytes("UTF-8"), 0, tmp_vendor_code, 0, vc_bytes_count);
            tmp_vendor_code[vc_bytes_count] = 0; // NULL termination

            status = GetVersion(major_version, minor_version, build_server, build_number, tmp_vendor_code);
        }
        catch (UnsupportedEncodingException ex)
        {
            // cannot happen, so ignore
        } 
  }

  /**
   *  Returns the error that occurred in the last function call.
   */
   public int getLastError()
   {
      return status;
   }

  /**
   *  Returns the SuperDog API major version. 
   */
   public int majorVersion() 
   {
     return major_version[0];
   }

  /**
   *  Returns the SuperDog API minor version.
   *
   */
   public int minorVersion()
   {
     return minor_version[0];
   }

  /**
   *  Returns the SuperDog API build number.
   *
   */
   public int buildNumber() 
   {
     return build_number[0];
   }
}

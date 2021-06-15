package cn.js.fan.mail;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class PopupAuthenticator extends Authenticator
{
    String username;
    String password;

    public PopupAuthenticator()
    {
        username = null;
        password = null;
    }

    public void init(String s, String s1)
    {
        username = s;
        password = s1;
    }

    protected PasswordAuthentication getPasswordAuthentication()
    {
        return new PasswordAuthentication(username, password);
    }
}

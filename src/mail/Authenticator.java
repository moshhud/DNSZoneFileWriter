package mail;
import javax.mail.PasswordAuthentication;

public class Authenticator
      extends javax.mail.Authenticator {
    private PasswordAuthentication authentication;
    String senderEamil;
    String senderEmailPassword;
    public Authenticator(String p_Email,String p_emailPassword) {
      senderEamil=p_Email;
      senderEmailPassword=p_emailPassword;
      authentication = new PasswordAuthentication(senderEamil,senderEmailPassword);
    }

    public PasswordAuthentication getPasswordAuthentication() {
      return authentication;
    }
  }
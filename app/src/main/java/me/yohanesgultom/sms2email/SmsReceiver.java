package me.yohanesgultom.sms2email;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by yohanesgultom on 13/05/16.
 */
public class SmsReceiver extends BroadcastReceiver {

    static String patternRegex = "SMS Code: (\\d+). .*";
    static Pattern pattern = Pattern.compile(patternRegex);
    static String realName = "Sender";
    static String username = "sender@email.com";
    static String password = "senderpassword";
    static String subject = "SMS Code";
    static String to = "recipient@email.com";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Info", "Received intent: " + intent.getAction());
        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Bundle bundle = intent.getExtras();
            SmsMessage[] messages = null;
            if (bundle != null){
                try{
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    messages = new SmsMessage[pdus.length];
                    for(int i=0; i<messages.length; i++){
                        messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                        String messageBody = messages[i].getMessageBody();
                        Matcher matcher = pattern.matcher(messageBody);
                        if (matcher.find()) {
                            String code = matcher.group(1);
                            Log.i("Code received", code);
                            sendMail(to, subject, code);
                        }
                    }
                }catch(Exception e){
                    Log.e("Exception", e.getMessage(), e);
                }
            }
        }
    }

    private void sendMail(String email, String subject, String messageBody) {
        Session session = createSessionObject();

        try {
            Message message = createMessage(email, subject, messageBody, session);
            new NetworkTask().execute(message);
        } catch (AddressException e) {
            Log.e("Exception", e.getMessage(), e);
        } catch (MessagingException e) {
            Log.e("Exception", e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {
            Log.e("Exception", e.getMessage(), e);
        }
    }


    private Session createSessionObject() {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        return Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    private Message createMessage(String email, String subject, String messageBody, Session session) throws MessagingException, UnsupportedEncodingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username, realName));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(email, email));
        message.setSubject(subject);
        message.setText(messageBody);
        return message;
    }
}

class NetworkTask extends AsyncTask<Message, Void, Void> {

    @Override
    protected Void doInBackground(Message... messages) {
        for (Message message:messages) {
            try {
                Transport.send(message);
            } catch (MessagingException e) {
                Log.e("NetworkTask Exception", e.getMessage(), e);
            }
        }
        return null;
    }
}

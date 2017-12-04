package jinr.arch;

import java.sql.*;
import java.io.*;
import dubna.walt.util.MD5;

import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import javax.mail.util.*;

public class ServiceSendMailAccept extends dubna.walt.service.Service {

    boolean debugMode = false;
    boolean sendFiles = true;
    Vector file_names = null;

    public void start() throws Exception {
        cfgTuner.outCustomSection("prereport", out);
        this.beforeStart();
        debugMode = cfgTuner.getParameter("mailDebug").equals("true");
        sendFiles = cfgTuner.getParameter("sendFiles").equals("true");
        if (debugMode) {
            System.out.println(">>> =================================");
        }
        if (debugMode) {
            System.out.println("===> ServiceSendMailAccept:");
        }
        if (sendFiles) {
            storeFiles();
        }
        sendMails();
        cfgTuner.outCustomSection("report", out);
    }

    public void sendMails() {
        String idsSentSuccess = "";
        String emailsSentSuccess = "";
        String idsSentFail = "";
        String emailsSentFail = "";

        Properties props = new Properties();
        props.put("mail.smtp.host", cfgTuner.getParameter("mailServer"));
				props.put("mail.smtps.auth", "true");
        if (debugMode) {
            System.out.println("===> mailServer:" + cfgTuner.getParameter("mailServer"));
        }
        if (debugMode) {
            System.out.println("===> mail.smtp.host: " + props.get("mail.smtp.host"));
        }
        props.put("mail.smtp.user", cfgTuner.getParameter("mailUser"));
        if (debugMode) {
            System.out.println("===> mailUser:" + cfgTuner.getParameter("mailUser"));
        }
        Session session = Session.getDefaultInstance(props, null);
        session.setDebug(cfgTuner.getParameter("mailDebug").equals("true"));

        String mailTo = cfgTuner.getParameter("mailTo");
        if (mailTo.length() == 0) {
            return;
        }
        List<String> mailList = Arrays.asList(mailTo.split(";"));

        for (String email : mailList) {
            
                List<String> aMailPerson = Arrays.asList(email.split(":"));
                try {
                MimeMessage msg = new MimeMessage(session);
                msg.setSentDate(new java.util.Date());
                String charset = cfgTuner.getParameter("mailCharset");
                if (charset.length() < 2) {
                    charset = "windows-1251";
                }
                msg.setHeader("charset", charset);

                if (debugMode) {
                    System.out.println("===> mailFrom:" + cfgTuner.getParameter("mailFrom"));
                }
                if (cfgTuner.enabledOption("mailFrom")) {
                    msg.setFrom(new InternetAddress(cfgTuner.getParameter("mailFrom")));
                }

                StringTokenizer to = new StringTokenizer(cfgTuner.getParameter("mailTo"), ";");
                msg.setRecipients(Message.RecipientType.TO, parseAddreses(aMailPerson.get(0)));

                msg.setSubject(cfgTuner.getParameter("subject"), charset);
                if (debugMode) {
                    System.out.println("===> subject:" + cfgTuner.getParameter("subject"));
                }

                String[] body = cfgTuner.getCustomSection("msgBody");
                String m_body = "";
                for (int i = 0; i < body.length; i++) {
                    m_body += body[i] + "\n";
                }
                m_body = m_body.replaceAll("\\$PERSON_ID\\$", aMailPerson.get(1));
                MimeBodyPart mbp1 = new MimeBodyPart();
                if (cfgTuner.enabledExpression("mailHTML")) {
                    mbp1.setDataHandler(new DataHandler(new ByteArrayDataSource(m_body, "text/html; charset=" + charset)));
                } else {
                    mbp1.setText(m_body, charset);
                }

                Multipart mp = new MimeMultipart();
                mp.addBodyPart(mbp1);

                if (sendFiles) {
                    MimeBodyPart mbp2 = new MimeBodyPart();
                    mbp2.setText("—истема автоматической рассылки документов", charset);
                    mp.addBodyPart(mbp2);
                    if (file_names != null) {
                        for (int i = 0; i < file_names.size(); i++) {
                            String file_name = (String) file_names.elementAt(i);
                            MimeBodyPart mbp3 = new MimeBodyPart();
                            FileDataSource fds = new FileDataSource(file_name);
                            mbp3.setDataHandler(new DataHandler(fds));
                            mbp3.setFileName(fds.getName());
                            mp.addBodyPart(mbp3);
                        }
                    }
                }
                msg.setContent(mp);
//                Transport.send(msg);
							Transport transport = session.getTransport("smtps");
																	transport.connect(cfgTuner.getParameter("mailServer")
																	 , cfgTuner.getParameter("mailUser" ), cfgTuner.getParameter("mailUserPw" ));
																	transport.sendMessage(msg, msg.getAllRecipients());
																	transport.close();


                emailsSentSuccess += ", "+aMailPerson.get(0);
                idsSentSuccess += ","+aMailPerson.get(1);
            } catch (Exception mex) {

                cfgTuner.addParameter("MAIL_SEND_ERROR", mex.toString());
                emailsSentFail += ", "+aMailPerson.get(0);
                idsSentFail += ","+aMailPerson.get(1);
            }

        }
        cfgTuner.addParameter("IDSSENTSUCCESS", idsSentSuccess);
        cfgTuner.addParameter("EMAILSSENTSUCCESS", emailsSentSuccess);
    }

    protected InternetAddress[] parseAddreses(String addrs) throws AddressException {
        if (debugMode) {
            System.out.println("===> addrs:" + addrs);
        }
        if (addrs == null || addrs.length() < 3) {
            return null;
        }
        StringTokenizer to = new StringTokenizer(addrs, ";");
        int numAddrs = to.countTokens();
        InternetAddress[] address = new InternetAddress[numAddrs];
        for (int i = 0; i < numAddrs; i++) {
            address[i] = new InternetAddress(to.nextToken());
        }
        return address;
    }

    protected void storeFiles() throws Exception {
        String sql = getSQL("SQL");
        oracle.sql.BLOB bl = null;
        String file_name = "";
        file_names = new Vector(5, 5);
        try {
            ResultSet r = dbUtil.getResults(sql);
            while (r.next()) {
                String destPath = cfgTuner.getParameter("tmpPath").replace('/', '\\');
                file_name = destPath + r.getString(1);
                file_names.add(file_name);
                System.out.println("file_name:" + file_name);
                File f = null;
                FileOutputStream lf = null;
                f = new File(destPath);
                if (!f.exists()) {
                    if (!f.mkdirs()) {
                        throw new Exception("Could not create destination directory");
                    }
                }
                lf = new FileOutputStream(file_name, false);
                if (lf == null) {
                    throw new Exception("Could not write output file");
                }
                bl = (oracle.sql.BLOB) r.getBlob(2);
                System.out.println("BLOB=" + bl);
                System.out.println("LENGTH=" + bl.length());
                byte[] dat = bl.getBytes(1, (int) bl.length());
                lf.write(dat);
                lf.flush();
                lf.close();
            }
            dbUtil.closeResultSet(r);
        } catch (Exception e) {
            e.printStackTrace(out);
        }
    }

}

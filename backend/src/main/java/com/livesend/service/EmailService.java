package com.livesend.service;

import com.livesend.entity.Contact;
import com.livesend.entity.FileItem;
import com.livesend.entity.SendTask;
import com.livesend.entity.User;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Properties;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendEmailWithAttachments(SendTask task) throws MessagingException {
        User user = task.getUser();
        List<Contact> contacts = task.getContacts();
        List<FileItem> files = task.getFiles();

        if (contacts.isEmpty()) {
            return;
        }

        JavaMailSender mailSender = createMailSender(user);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(user.getEmail());
        
        String[] toEmails = contacts.stream()
                .map(Contact::getEmail)
                .toArray(String[]::new);
        helper.setTo(toEmails);

        helper.setSubject("来自 " + user.getUsername() + " 的重要消息");

        String htmlContent = buildEmailContent(task);
        helper.setText(htmlContent, true);

        for (FileItem fileItem : files) {
            File file = new File(fileItem.getFilePath());
            if (file.exists()) {
                helper.addAttachment(fileItem.getOriginalName(), file);
            }
        }

        mailSender.send(message);
    }

    private JavaMailSender createMailSender(User user) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        String host = user.getEmailHost();
        Integer port = user.getEmailPort();
        String username = user.getEmailUsername();
        String password = user.getEmailPassword();

        if (host != null && !host.isEmpty()) {
            mailSender.setHost(host);
        }
        if (port != null) {
            mailSender.setPort(port);
        }
        if (username != null && !username.isEmpty()) {
            mailSender.setUsername(username);
        }
        if (password != null && !password.isEmpty()) {
            mailSender.setPassword(password);
        }

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }

    private String buildEmailContent(SendTask task) {
        User user = task.getUser();
        return """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body {
                            font-family: 'Microsoft YaHei', sans-serif;
                            background-color: #FFF5F5;
                            margin: 0;
                            padding: 20px;
                        }
                        .container {
                            max-width: 600px;
                            margin: 0 auto;
                            background-color: white;
                            border-radius: 16px;
                            padding: 40px;
                            box-shadow: 0 4px 20px rgba(255, 182, 193, 0.3);
                        }
                        .header {
                            text-align: center;
                            margin-bottom: 30px;
                        }
                        .heart {
                            font-size: 48px;
                            color: #FF6B9D;
                        }
                        h1 {
                            color: #FF6B9D;
                            margin: 20px 0;
                        }
                        .content {
                            color: #666;
                            line-height: 1.8;
                            font-size: 16px;
                        }
                        .footer {
                            text-align: center;
                            margin-top: 40px;
                            padding-top: 20px;
                            border-top: 1px solid #FFE4E9;
                            color: #999;
                            font-size: 14px;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <div class="heart">💝</div>
                            <h1>来自远方的问候</h1>
                        </div>
                        <div class="content">
                            <p>亲爱的朋友，</p>
                            <p>这是一条来自 <strong>%s</strong> 的消息。</p>
                            <p>有些话，有些文件，他/她想传递给你。请查看附件中的内容。</p>
                            <p>每一份文件都承载着特别的意义，请好好珍惜。</p>
                            <p>愿温暖与你同在。</p>
                        </div>
                        <div class="footer">
                            <p>— 由 Live Send 传递 —</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(user.getUsername());
    }

    public void sendTestEmail(User user, String toEmail) throws MessagingException {
        JavaMailSender mailSender = createMailSender(user);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(user.getEmail());
        helper.setTo(toEmail);
        helper.setSubject("Live Send 测试邮件");

        String htmlContent = """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body {
                            font-family: 'Microsoft YaHei', sans-serif;
                            background-color: #FFF5F5;
                            margin: 0;
                            padding: 20px;
                        }
                        .container {
                            max-width: 600px;
                            margin: 0 auto;
                            background-color: white;
                            border-radius: 16px;
                            padding: 40px;
                            box-shadow: 0 4px 20px rgba(255, 182, 193, 0.3);
                        }
                        .header {
                            text-align: center;
                            margin-bottom: 30px;
                        }
                        .heart {
                            font-size: 48px;
                            color: #FF6B9D;
                        }
                        h1 {
                            color: #FF6B9D;
                            margin: 20px 0;
                        }
                        .content {
                            color: #666;
                            line-height: 1.8;
                            font-size: 16px;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <div class="heart">💌</div>
                            <h1>测试成功！</h1>
                        </div>
                        <div class="content">
                            <p>恭喜！你的邮箱配置已成功验证。</p>
                            <p>你现在可以开始创建发送任务，在需要的时候传递你的爱与记忆。</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}

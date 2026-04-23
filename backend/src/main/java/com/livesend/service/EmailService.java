package com.livesend.service;

import com.livesend.entity.Contact;
import com.livesend.entity.FileItem;
import com.livesend.entity.Note;
import com.livesend.entity.SendTask;
import com.livesend.entity.User;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Service
public class EmailService {

    private static final String SEND_TASK_TEMPLATE = "email/send-task-template";
    private static final String TEST_TEMPLATE = "email/test-email-template";

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private EncryptionService encryptionService;

    public void sendEmailWithAttachments(SendTask task) throws MessagingException {
        User user = task.getUser();
        List<Contact> contacts = task.getContacts();
        List<FileItem> files = task.getFiles();
        List<Note> notes = task.getNotes();

        if (contacts == null || contacts.isEmpty()) {
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

        String htmlContent = renderSendTaskTemplate(user, notes, files, contacts);
        helper.setText(htmlContent, true);

        for (FileItem fileItem : files) {
            File file = new File(fileItem.getFilePath());
            if (file.exists()) {
                helper.addAttachment(fileItem.getOriginalName(), file);
            }
        }

        for (Note note : notes) {
            if (note.getContent() != null && !note.getContent().isEmpty()) {
                String noteContent = note.getTitle() + "\n\n" + note.getContent();
                DataSource dataSource = new ByteArrayDataSource(noteContent.getBytes(StandardCharsets.UTF_8), "text/plain;charset=UTF-8");
                helper.addAttachment(note.getTitle() + ".txt", dataSource);
            }
        }

        mailSender.send(message);
    }

    public void sendTestEmail(User user, String toEmail) throws MessagingException {
        JavaMailSender mailSender = createMailSender(user);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(user.getEmail());
        helper.setTo(toEmail);
        helper.setSubject("Live Send 测试邮件");

        String htmlContent = renderTestTemplate();
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    private String renderSendTaskTemplate(User user, List<Note> notes, List<FileItem> files, List<Contact> contacts) {
        Context context = new Context(Locale.CHINESE);
        
        List<Map<String, Object>> notesData = null;
        if (notes != null && !notes.isEmpty()) {
            notesData = notes.stream()
                    .map(note -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("title", note.getTitle());
                        map.put("content", escapeHtml(note.getContent()));
                        return map;
                    })
                    .collect(Collectors.toList());
        }
        
        List<String> fileNames = null;
        if (files != null && !files.isEmpty()) {
            fileNames = files.stream()
                    .map(FileItem::getOriginalName)
                    .collect(Collectors.toList());
        }
        
        List<Map<String, Object>> contactsData = null;
        if (contacts != null && !contacts.isEmpty()) {
            contactsData = contacts.stream()
                    .map(contact -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("name", contact.getName());
                        map.put("email", contact.getEmail());
                        return map;
                    })
                    .collect(Collectors.toList());
        }
        
        context.setVariable("senderName", user.getUsername());
        context.setVariable("notes", notesData);
        context.setVariable("fileNames", fileNames);
        context.setVariable("contacts", contactsData);
        
        return templateEngine.process(SEND_TASK_TEMPLATE, context);
    }

    private String renderTestTemplate() {
        Context context = new Context(Locale.CHINESE);
        return templateEngine.process(TEST_TEMPLATE, context);
    }

    private JavaMailSender createMailSender(User user) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        String host = user.getEmailHost();
        Integer port = user.getEmailPort();
        String username = user.getEmailUsername();
        String password = user.getEmailPassword();

        if (password != null && !password.isEmpty()) {
            if (encryptionService.isEncrypted(password)) {
                password = encryptionService.decrypt(password);
            }
        }

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

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}

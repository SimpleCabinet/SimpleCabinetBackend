package pro.gravit.launchermodules.simplecabinet;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class SimpleCabinetMailSender {
    private transient final SimpleCabinetModule module;

    public SimpleCabinetMailSender(SimpleCabinetModule module) {
        this.module = module;
    }

    public Message newEmailInstance(String addressTo) throws MessagingException {
        Properties properties = new Properties();
        //Хост или IP-адрес почтового сервера
        SimpleCabinetConfig.MailSenderConfig config = module.config.mail;
        properties.put("mail.smtp.host", config.host);
        //Требуется ли аутентификация для отправки сообщения
        properties.put("mail.smtp.auth", String.valueOf(config.auth));
        //Порт для установки соединения
        properties.put("mail.smtp.socketFactory.port", String.valueOf(config.port));
        //Фабрика сокетов, так как при отправке сообщения Yandex требует SSL-соединения
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        //Создаем соединение для отправки почтового сообщения
        Session session = Session.getDefaultInstance(properties,
                //Аутентификатор - объект, который передает логин и пароль
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(config.username, config.password);
                    }
                });

        //Создаем новое почтовое сообщение
        Message message = new MimeMessage(session);
        //От кого
        message.setFrom(new InternetAddress(config.from));
        //Кому
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(addressTo));
        //Тема письма
        //message.setSubject("Очень важное письмо!!!");
        //Текст письма
        //message.setText("Hello, Email!");
        //Поехали!!!
        //Transport.send(message);
        return message;
    }

    public void sendEmail(Message message) throws MessagingException {
        Transport.send(message);
    }

    public void simpleSendEmail(String addressTo, String title, String content) {
        try {
            Message message = newEmailInstance(addressTo);
            message.setSubject(title);
            message.setContent(content, "text/html; charset=UTF-8");
            sendEmail(message);
        }  catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}

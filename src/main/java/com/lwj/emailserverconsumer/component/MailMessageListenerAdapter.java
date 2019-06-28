package com.lwj.emailserverconsumer.component;

import com.alibaba.fastjson.JSONObject;
import com.lwj.common.model.MailMessageModel;
import com.lwj.common.utils.HessianSerialization;
import com.lwj.common.utils.KryoSerializer;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.nio.file.Files;

/**
 * @Auth: lwj
 * @Date: 2019/6/27 9:59
 */
@Component("mailMessageListenerAdapter")
public class MailMessageListenerAdapter extends MessageListenerAdapter {


    @Resource
    private JavaMailSender mailSender;

    @Resource
    private KryoSerializer kryo;

    @Resource
    private HessianSerialization hessian;

    @Value("${mail.username}")
    private String mailUsername;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        try {
//            if (message.getBody().length > 0) {
                MailMessageModel mailMessageModel = hessian.deserialize(message.getBody(), MailMessageModel.class);
                //解析RabbitMQ消息体
//            String messageBody = new String(message.getBody());
//            MailMessageModel mailMessageModel = JSONObject.toJavaObject(JSONObject.parseObject(messageBody), MailMessageModel.class);
                //发送邮件
                String to = mailMessageModel.getTo();
                String subject = mailMessageModel.getSubject();
                String text = mailMessageModel.getText();
                File[] files = mailMessageModel.getFiles();

                sendHtmlMail(to, subject, text,files);
                //手动ACk
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
//            }else {
//                throw new RuntimeException();
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendHtmlMail(String to, String subject, String text, File[] files) throws Exception {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage,true);
        mimeMessageHelper.setFrom(mailUsername);
        mimeMessageHelper.setTo(to);
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setText(text);
        for (File file:files){
            mimeMessageHelper.addAttachment(file.getName(),file);
        }
        //发送邮件
        mailSender.send(mimeMessage);
    }
}

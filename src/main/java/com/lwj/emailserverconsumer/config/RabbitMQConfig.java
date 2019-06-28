package com.lwj.emailserverconsumer.config;

import com.lwj.emailserverconsumer.component.MailMessageListenerAdapter;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 * @Auth: lwj
 * @Date: 2019/6/27 8:57
 */
@Configuration
@ComponentScan(basePackages = {"com.lwj.emailserverconsumer","com.lwj.common"})
@PropertySource(value = {"classpath:application.properties"})
public class RabbitMQConfig {

    @Autowired
    private Environment env;

    @Bean
    public ConnectionFactory connectionFactory() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(env.getProperty("mq.host").trim());
        factory.setPort(Integer.valueOf(env.getProperty("mq.port").trim()));
        factory.setUsername(env.getProperty("mq.username").trim());
        factory.setPassword(env.getProperty("mq.password"));
        factory.setVirtualHost(env.getProperty("mq.vhost").trim());
        return factory;
    }

    @Bean
    public CachingConnectionFactory cachingConnectionFactory() throws Exception {
        return new CachingConnectionFactory(connectionFactory());

    }

    @Bean
    public RabbitTemplate rabbitTemplate() throws Exception {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(cachingConnectionFactory());
        rabbitTemplate.setChannelTransacted(true);
        return rabbitTemplate;
    }

    @Bean
    public AmqpAdmin amqpAdmin() throws Exception {
        return new RabbitAdmin(cachingConnectionFactory());
    }

    @Bean
    public SimpleMessageListenerContainer listenerContainer(
            @Qualifier("mailMessageListenerAdapter") MailMessageListenerAdapter mailMessageListenerAdapter) throws Exception {
        String queueName = env.getProperty("mq.queue").trim();
        SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer(cachingConnectionFactory());
        simpleMessageListenerContainer.setQueueNames(queueName);
        simpleMessageListenerContainer.setMessageListener(mailMessageListenerAdapter);
        //设置手动ACK
        simpleMessageListenerContainer.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return simpleMessageListenerContainer;
    }


}

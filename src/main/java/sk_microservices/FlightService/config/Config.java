package sk_microservices.FlightService.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.Queue;

@Configuration
public class Config {

    @Value("${spring.activemq.broker-url}")
    private String brokerURL;

    @Bean
    public Queue userQueue(){
        return new ActiveMQQueue("user.queue");
    }

    @Bean
    public Queue ticketQueue(){return new ActiveMQQueue("ticket.queue");}

    @Bean
    public ActiveMQConnectionFactory activeMQConnectionFactory(){
        ActiveMQConnectionFactory activeMQConnectionFactory= new ActiveMQConnectionFactory();
        activeMQConnectionFactory.setBrokerURL(brokerURL);
        return activeMQConnectionFactory;
    }

    @Bean
    public JmsTemplate jmsTemplate(ActiveMQConnectionFactory activeMQConnectionFactory){
        return new JmsTemplate(activeMQConnectionFactory);
    }

}

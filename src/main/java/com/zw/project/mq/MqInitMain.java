package com.zw.project.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import static com.zw.project.mq.BiMqConstant.*;

/**
 * 用于创建测试程序用到的交换机和队列（只用在程序启动前执行一次）
 */
public class MqInitMain {

    public static void main(String[] args) {
        try {
            // 创建连接工厂
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("120.26.52.171");
            factory.setPort(5672);
            factory.setVirtualHost("/");
            factory.setUsername("admin");
            factory.setPassword("123456");
            // 创建连接
            Connection connection = factory.newConnection();
            // 创建通道
            Channel channel = connection.createChannel();
            // 定义交换机的名称为"code_exchange"
            // 声明交换机，指定交换机类型为 direct
            channel.exchangeDeclare(BI_EXCHANGE_NAME, "direct");

            // 创建队列，随机分配一个队列名称
            // 声明队列，设置队列持久化、非独占、非自动删除，并传入额外的参数为 null
            channel.queueDeclare(BI_QUEUE_NAME, true, false, false, null);
            // 将队列绑定到交换机，指定路由键为 "my_routingKey"
            channel.queueBind(BI_QUEUE_NAME, BI_EXCHANGE_NAME, BI_ROUTING_KEY);
        } catch (Exception e) {
        	// 异常处理
        }
    }
}

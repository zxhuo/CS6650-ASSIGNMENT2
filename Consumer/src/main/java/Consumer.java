

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import models.PayLoad;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeoutException;

public class Consumer {
    private final static String QUEUE_NAME = "SkierMQ";
    private final static int THREAD_POOL_SIZE = 3;
    private final static String RMQIP = "34.212.223.13";
    private final static int RMQPORT = 5672;
    private final static String RMQ_USER = "zhixiang";
    private final static String RMQ_PASSWORD = "123456";


    public static void main(String[] args) throws IOException, TimeoutException {
        Gson gson = new Gson();
        ConcurrentHashMap<Integer, CopyOnWriteArrayList<PayLoad>> map = new ConcurrentHashMap<>();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RMQIP);
        factory.setPort(RMQPORT);
        factory.setUsername(RMQ_USER);
        factory.setPassword(RMQ_PASSWORD);
        Connection connection = factory.newConnection();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Channel channel = connection.createChannel();
                    channel.queueDeclare(QUEUE_NAME, false, false, false, null);

                    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                      String message = new String(delivery.getBody(), "UTF-8");
                      PayLoad payload = gson.fromJson(message, PayLoad.class);
                      int id = payload.getSkierID();
                      if(!map.contains(payload.getSkierID())){
                          map.put(id, new CopyOnWriteArrayList<>());
                      }
                      map.get(id).add(payload);
                    };
                    channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        for(int i = 0; i < THREAD_POOL_SIZE; i++){
            Thread thread = new Thread(runnable);
            thread.start();
        }

        System.out.println("[x] Connection is ready, " + THREAD_POOL_SIZE+
                " Thread waiting for messages. To exit press CTRL+C\"");

//        Channel channel = connection.createChannel();
//        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
//        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
//            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
//            System.out.println("[x] Received '" + message + "'");
//        };
//        channel.basicConsume(QUEUE_NAME, true, deliverCallback,consumerTag -> {});
//        System.out.println("[*] Waiting for message");
    }
}

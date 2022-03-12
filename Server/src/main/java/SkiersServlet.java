
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.squareup.okhttp.Response;
import io.swagger.client.model.*;
import io.swagger.client.model.LiftRide;
import models.PayLoad;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@WebServlet(name = "SkierServlet", value = "/SkierServlet")
public class SkiersServlet extends HttpServlet {

    private final String VERTICAL="vertical";
    private final String RESORT="resorts";
    private final String SEASONS="seasons";
    private final String DAYS = "days";
    private final String SKIERS = "skiers";
    private final int MINYEAR = 0;
    private final int MAXYEAR = 2022;
    private final int MINDAY = 0;
    private final int MAXDAY = 366;
    private Gson gson = new Gson();
    private final static String QUEUE_NAME = "SkierMQ";
    private ConnectionFactory factory = new ConnectionFactory();
    private BlockingQueue<Channel> pool = new LinkedBlockingQueue<>();
    private final int POOL_SIZE = 15;

    private final String RMQIP = "34.212.223.13";
    private final int RMQPORT = 5672;
    private final String RMQ_USER = "zhixiang";
    private final String RMQ_PASSWORD = "123456";

    @Override
    public void init() throws ServletException {
        super.init();
        factory.setHost(RMQIP);
        factory.setPort(RMQPORT);
        factory.setUsername(RMQ_USER);
        factory.setPassword(RMQ_PASSWORD);
        try {
            Connection connection = factory.newConnection();
            for(int i = 0; i < POOL_SIZE; i++){
                Channel channel = connection.createChannel();
                pool.add(channel);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String urlPath = req.getPathInfo();

        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            ResponseMsg msg = new ResponseMsg();
            msg.setMessage("url is not Valid");
            res.getWriter().write(gson.toJson(msg));
            return;
        }

        String[] urlParts = urlPath.split("/");

        if (!isUrlValid(urlParts, req)) {
            ResponseMsg msg = new ResponseMsg();
            msg.setMessage("url is Not valid");
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write(gson.toJson(msg));
        } else {
            if(urlParts[2].equals(VERTICAL)){
                SkierVerticalResorts vertical = new SkierVerticalResorts();
                res.getWriter().write(gson.toJson(vertical));
            } else{
                // needed to be done;
                LiftRide liftride = new LiftRide();
                res.getWriter().write("34507");
            }
            res.setStatus(HttpServletResponse.SC_OK);
        }
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String urlPath = req.getPathInfo();

        if (urlPath == null || urlPath.isEmpty()) {
            ResponseMsg msg = new ResponseMsg();
            msg.setMessage("url is Not valid");
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write(gson.toJson(msg));
            return;
        }

        String[] urlParts = urlPath.split("/");

        if (!isUrlValid(urlParts, req)) {
            ResponseMsg msg = new ResponseMsg();
            msg.setMessage("url is Not valid");
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write(gson.toJson(msg));
        } else {
            PayLoad payload = gson.fromJson(req.getReader(), PayLoad.class);
            payload.setResortID(Integer.parseInt(urlParts[1]));
            payload.setSeasonID(urlParts[3]);
            payload.setDayID(urlParts[5]);
            payload.setSkierID(Integer.parseInt(urlParts[7]));
            res.setStatus(HttpServletResponse.SC_OK);
            String message = gson.toJson(payload);
            res.getWriter().write(message);
            queueMessage(message);

        }
    }

    private void queueMessage(String message) {
        try{
            Channel cur_channel = pool.poll(20, TimeUnit.MILLISECONDS);
            cur_channel.queueDeclare(QUEUE_NAME,false,false,false,null);
            cur_channel.basicPublish("", QUEUE_NAME,null,message.getBytes(StandardCharsets.UTF_8));
            pool.add(cur_channel);
        }catch (IOException | InterruptedException e){
            e.printStackTrace();
        }
    }

    private boolean isUrlValid(String[] urlPath, HttpServletRequest req) {
        if(urlPath.length == 8){
            try {
                int resortID = Integer.parseInt(urlPath[1]);
                int seasonID = Integer.parseInt(urlPath[3]);
                int dayID = Integer.parseInt(urlPath[5]);
                int skierID = Integer.parseInt(urlPath[7]);

                return (seasonID > MINYEAR &&
                        seasonID <= MAXYEAR &&
                        dayID >= MINDAY &&
                        dayID <= MAXDAY &&
                        urlPath[2].equals(SEASONS) &&
                        urlPath[4].equals(DAYS) &&
                        urlPath[6].equals(SKIERS)
                );
            }
            catch (Exception e){
                return false;
            }

        }else if(urlPath.length == 3){
            try{
                int skierID = Integer.parseInt(urlPath[1]);
                return(urlPath[2].equals(VERTICAL) && req.getParameter(RESORT) != null);
            } catch (Exception e){
                return false;
            }
        }else{
            return false;
        }
    }

}

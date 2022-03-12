import io.swagger.client.*;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import jdk.jshell.Snippet;

import javax.print.attribute.DateTimeSyntax;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;


public class PhaseOneThread extends Thread {

    private static final String BASE_PATH ="http://upic-633370685.us-west-2.elb.amazonaws.com:8080/Server_war";

    //private static final String BASE_PATH ="http://18.236.241.64:8080/Server_war";

    private int numPostReq;
    private int startID;
    private int endID;
    private int numLifts;
    private int endTime;
    private int startTime;
    private int DEFULT_RESORT_ID;
    private String DEFULT_SEASON_ID;
    private String DEFULT_DAY_ID;
    private CountDownLatch latch;
    private CountDownLatch totalThread;
    private StatusBoard board;

    public PhaseOneThread(int numPostReq, int startID, int endID, int numLifts, int startTime,
                          int endTime, String DEFULT_SEASON_ID,String DEFULT_DAY_ID, int DEFULT_RESORT_ID,
                          CountDownLatch latch, CountDownLatch totalThread, StatusBoard board){
        this.numPostReq = numPostReq;
        this.startID = startID;
        this.endID = endID;
        this.numLifts = numLifts;
        this.startTime = startTime;
        this.endTime = endTime;
        this.DEFULT_RESORT_ID = DEFULT_RESORT_ID;
        this.DEFULT_SEASON_ID = DEFULT_SEASON_ID;
        this.DEFULT_DAY_ID = DEFULT_DAY_ID;
        this.latch = latch;
        this.totalThread = totalThread;
        this.board = board;
    }

    @Override
    public void run(){
        Timestamp sTimer;
        Timestamp eTimer;
        List<String> dataList = new ArrayList<>();
        int suc = 0;
        int unsuc = 0;
        int code = 200;
        ApiClient client = new ApiClient();
        client.setBasePath(BASE_PATH);
        SkiersApi apiInstance = new SkiersApi(client);
        for(int i = 0;i < numPostReq; i++){
            int randomSkierID = ThreadLocalRandom.current().nextInt(endID-startID) + startID;
            int randomLiftID = ThreadLocalRandom.current().nextInt(numLifts) + 1;
            int randomTime = ThreadLocalRandom.current().nextInt(endTime-startTime) + startTime;
            int randomWaitTime = ThreadLocalRandom.current().nextInt(10);
            LiftRide lr = new LiftRide();
            lr.setTime(randomTime);
            lr.setLiftID(randomLiftID);
            lr.setWaitTime(randomWaitTime);
            sTimer = new Timestamp(System.currentTimeMillis());
            for(int j = 0; j < 5; j++){
                try{
                    apiInstance.writeNewLiftRide(lr,DEFULT_RESORT_ID, DEFULT_SEASON_ID, DEFULT_DAY_ID, randomSkierID);
                    suc++;
                    break;
                }
                catch (ApiException e){
                    if(j == 4){
                        code = e.getCode();
                        unsuc++;
                        System.out.println(e);
                    }
                }
            }
            eTimer = new Timestamp(System.currentTimeMillis());
            long latency = eTimer.getTime() - sTimer.getTime();
            String curData = sTimer.toString()+",POST,"+latency+","+code+"\n";
            dataList.add(curData);
        }
        board.addCsv(dataList);
        board.addTotalReq(numPostReq);
        board.addSucReq(suc);
        board.addUnsucReq(unsuc);
        latch.countDown();
        totalThread.countDown();
    }
}

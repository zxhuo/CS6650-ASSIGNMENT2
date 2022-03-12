import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Array;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Client2 {

    private static final int MAX_THREADS = 1024;
    private static final int MAX_SKIERS = 50000;
    private static final int MIN_LIFTS = 5;
    private static final int MAX_LIFTS = 60;
    private static final int MAX_RUNS = 20;
    private static final String DEFULT_SEASON_ID = "2022";
    private static final String DEFULT_DAY_ID = "1";
    private static final int DEFULT_RESORT_ID = 1;

    private static int numThreads;
    private static int numSkiers;
    private static int numLifts;
    private static int numRuns;
    private static int port;

    public static void main(String[] args) {
        if(!isValidArgs(args)){
            return;
        }
        int numP1Thread = (int)Math.round(numThreads/4);
        int numP3Thread = (int)Math.round(numThreads/10);
        StatusBoard board = new StatusBoard();
        CountDownLatch totalThread = new CountDownLatch(numP1Thread+numThreads+numP3Thread);

        PhaseOne p_one = new PhaseOne(numP1Thread, numSkiers, numRuns,1, 90,
                numLifts,DEFULT_SEASON_ID,DEFULT_DAY_ID,DEFULT_RESORT_ID,
                (int) Math.round(numP1Thread/5), totalThread, board);
        PhaseTwo p_two = new PhaseTwo(numThreads,numSkiers, numRuns,91, 360,
                numLifts,DEFULT_SEASON_ID,DEFULT_DAY_ID,DEFULT_RESORT_ID,
                (int) Math.round(numP1Thread/5),totalThread, board);
        PhaseThree p_three = new PhaseThree(numP3Thread, numSkiers, numRuns,361, 420,
                numLifts,DEFULT_SEASON_ID,DEFULT_DAY_ID,DEFULT_RESORT_ID,
                totalThread, board);

        try {
            Timestamp startTime = new Timestamp(System.currentTimeMillis());
            p_one.run();
            p_two.run();
            p_three.run();
            totalThread.await();
            Timestamp endTime = new Timestamp(System.currentTimeMillis());
            long wallTime = endTime.getTime() - startTime.getTime();
            System.out.println("Successful Request: "+board.getSucReq());
            System.out.println("Unsuccessful Request: "+board.getUnsucReq());
            System.out.println("Total Request: "+board.getTotalReq());
            System.out.println("WALL TIME: " + wallTime + " ms");
            System.out.println("throughput: " +(int)board.getTotalReq()/((int)wallTime/1000) + "/sec");
            writeFile(board.getCsv());
            dataParser(board, wallTime);


        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void writeFile(List<String> list) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("data.csv"));
            bw.write("start_time,type,latency,code\n" );
            for(String data: list){
                bw.write(data);
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void dataParser(StatusBoard board, long wallTime) {
        List<String> list = board.getCsv();
        List<Integer> times = new ArrayList<>();
        for(String s: list){
            String[] parts = s.split(",");
            // 1, POST, 22, 200, \n
            int time = Integer.parseInt(parts[2]);
            times.add(time);
        }
        Collections.sort(times);
        int timesSize = times.size();
        // mean time
        int sum = 0;
        for(int i: times){
            sum+=i;
        }
        double meanTime = sum/timesSize;
        // median time
        double medianTime;
        if(times.size() %2 == 1){
            medianTime = times.get(timesSize/2);
        }else{
            medianTime = (times.get(timesSize/2) + times.get(timesSize/2 - 1))/2;
        }
        //throughput
        int throughput = (int) (board.getTotalReq()/(wallTime/1000));

        //99th response time
        int nnPerc = times.get((int) (timesSize*0.99) - 1);
        //min and max response time
        int minTime = times.get(0);
        int maxTime = times.get(timesSize - 1);

        System.out.println("mean time: " + meanTime + " ms");
        System.out.println("median time: "+ medianTime + " ms");
        System.out.println("throughput: " + throughput + " /sec");
        System.out.println("99%: " + nnPerc + " ms");
        System.out.println("min time: " + minTime + " ms");
        System.out.println("max time: " + maxTime + " ms");
    }

    private static boolean isValidArgs(String[] args) {
        if(args.length != 5){
            System.out.println("MISS ARGS");
            return false;
        }else{
            try{
                numThreads = Integer.parseInt(args[0]);
                numSkiers = Integer.parseInt(args[1]);
                numLifts = Integer.parseInt(args[2]);
                numRuns = Integer.parseInt(args[3]);
                port = Integer.parseInt(args[4]);
                if (numThreads > MAX_THREADS) {
                    System.out.println("OVER MAX THREADS");
                    return false;
                }
                if (numSkiers > MAX_SKIERS) {
                    System.out.println("OVER MAX SKIERS");
                    return false;
                }
                if (numLifts < MIN_LIFTS || numLifts > MAX_LIFTS) {
                    System.out.println("LIFTS NOT IN THE RANGE");
                    return false;
                }
                if (numRuns > MAX_RUNS) {
                    System.out.println("OVER MAX RUNS");
                    return false;
                }
                return true;
            }catch (Exception e){
                System.out.println("WRONG INPUTS");
                return false;
            }
        }
    }
}

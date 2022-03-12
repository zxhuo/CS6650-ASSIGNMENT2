import java.util.concurrent.CountDownLatch;

public class PhaseThree {

    private final double MULTIPILER = 0.1;

    private int numThreads;
    private int numSkiers;
    private int numRuns;
    private int startTime;
    private int endTime;
    private int numLifts;
    private int DEFULT_RESORT_ID;
    private String DEFULT_SEASON_ID;
    private String DEFULT_DAY_ID;
    private CountDownLatch totalThread;
    private StatusBoard board;


    public PhaseThree(int numThreads, int numSkiers, int numRuns, int startTime, int endTime,
                      int numLifts,String DEFULT_SEASON_ID,String DEFULT_DAY_ID, int DEFULT_RESORT_ID,
                      CountDownLatch totalThread, StatusBoard board) {

        this.numThreads = numThreads;
        this.numSkiers = numSkiers;
        this.numRuns = numRuns;
        this.startTime = startTime;
        this.endTime = endTime;
        this.numLifts = numLifts;
        this.DEFULT_RESORT_ID = DEFULT_RESORT_ID;
        this.DEFULT_SEASON_ID = DEFULT_SEASON_ID;
        this.DEFULT_DAY_ID = DEFULT_DAY_ID;
        this.totalThread = totalThread;
        this.board = board;
    }

    public void run() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(numThreads);
        for(int i = 0; i < numThreads; i++){
            int startid = 1 + i * (numSkiers/numThreads);
            int endid = (i + 1) * (numSkiers/numThreads);
            int numPostReq = (int)Math.round((numRuns * MULTIPILER));
            Thread t = new PhaseThreeThread(numPostReq, startid, endid, numLifts, startTime, endTime,
                    DEFULT_SEASON_ID,DEFULT_DAY_ID,DEFULT_RESORT_ID, latch, totalThread, board);
            t.start();
        }
        latch.await();
    }
}

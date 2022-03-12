import java.util.ArrayList;
import java.util.List;

public class StatusBoard {

    private int sucReq;
    private int unsucReq;
    private int totalReq;
    private List<String> csv;

    public StatusBoard() {
        this.sucReq = 0;
        this.unsucReq = 0;
        this.totalReq = 0;
        this.csv = new ArrayList<>();
    }

    public synchronized void addSucReq(int n){
        sucReq += n;
    }

    public synchronized void addUnsucReq(int n){
        unsucReq += n;
    }

    public synchronized void addTotalReq(int n){
        totalReq += n;
    }

    public synchronized void addCsv(List<String> list){csv.addAll(list);}

    public int getTotalReq(){
        return totalReq;
    }

    public int getSucReq() {
        return sucReq;
    }

    public int getUnsucReq() {
        return unsucReq;
    }

    public List<String> getCsv(){return csv;}
}

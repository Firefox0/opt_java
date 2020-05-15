public class Pc_threads extends Thread {

    Requests r;

    public Pc_threads (Requests r) {
        this.r = r;
    }

    public void run() {
        this.r.handle_downloads();
    }

    public void threads(int amount_threads) {
        for (int i = 0; i < amount_threads; i++) {
            new Pc_threads(this.r).start();
        }
    }
}
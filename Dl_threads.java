public class Dl_threads extends Thread {

    Requests r;

    public Dl_threads (Requests r) {
        this.r = r;
    }

    public void run() {
        this.r.handle_downloads();
    }

    public void threads(int amount_threads) {
        for (int i = 0; i < amount_threads; i++) {
            new Dl_threads(this.r).start();
        }
    }
}

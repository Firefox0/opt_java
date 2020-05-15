public class Multithreading extends Thread {

    Requests r;

    public Multithreading (Requests r) {
        this.r = r;
    }

    public void run() {
        this.r.handle_downloads();
    }

    public void threads(int amount_threads) {
        for (int i = 0; i < amount_threads; i++) {
            new Multithreading(this.r).start();
        }
    }
}

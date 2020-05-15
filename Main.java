public class Main {

    public static void main(String[] args) {
        Requests r = new Requests();
        r.main(1, 5);
        Multithreading mt = new Multithreading(r);
        mt.threads(3);
    }
}
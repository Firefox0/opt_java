import java.util.Scanner;

public class Main {
    
    public static void main(String[] args) {
        System.out.println("-- OPT Downloader --\n");
        Scanner scanner = new Scanner(System.in);
        System.out.print("Starting episode: ");
        int start = scanner.nextInt();
        System.out.print("Ending episode (including): ");
        int end = scanner.nextInt();
        System.out.print("Amount threads: ");
        int amount_threads = scanner.nextInt();
        scanner.close();
        System.out.print("\n");
        Requests r = new Requests();
        r.main(start, end);
        System.out.print("\n");
        Multithreading mt = new Multithreading(r);
        mt.threads(amount_threads);
    }

}
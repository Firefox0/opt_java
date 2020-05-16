import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Requests {

    private String base_url_fh = "http://onepiece-tube.com/folge/";
    private String base_url_sh = "-tBvZaU";
    private String dir = String.format("%s\\One Piece", System.getProperty("user.dir"));
    private ArrayList<Pair> videos = new ArrayList<Pair>();
    private int start = 0;
    private int amount_episodes = 0;
    private int download_counter = 0;
    private int episode_counter = 0;
    private final int BUFFER_SIZE = 4096;

    public void check_dir() {
        File f = new File(this.dir);
        if (!f.isDirectory()) {
            f.mkdir();
        }
    }

    public String get_opt_url(int episode) {
        String opt_url = String.format("%s%s%s", this.base_url_fh, episode, this.base_url_sh);
        return opt_url;
    }

    public String request_source(String url) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        HttpResponse<String> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
        String response_body = response.body();
        return response_body;
    }

    public String extract_html_url(String source) {
        String[] split_source = source.split("\"");
        for (String string : split_source) {
            if (string.contains("https://www.ani-stream.com/embed")) {
                String html_url = string;
                return html_url;
            }
        }
        return null;
    }

    public String extract_video_url(String source) {
        String[] split_source = source.split("\"");
        for (String string : split_source) {
            if (string.contains("https://node")) {
                String video_url = string;
                return video_url;
            }
        }
        return null;
    }

    public void download_video(String url, String directory) {
        try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
                FileOutputStream out = new FileOutputStream(directory)) {
            byte buffer[] = new byte[this.BUFFER_SIZE];
            int num_bytes;
            while ((num_bytes = in.read(buffer, 0, this.BUFFER_SIZE)) != -1) {
                out.write(buffer, 0, num_bytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized Pair handle_iteration() {
        if (this.download_counter >= this.videos.size()) {
            return null;
        }
        Pair video = this.videos.get(this.download_counter);
        this.download_counter++;
        return video;
    }

    public void handle_downloads() {
        while (this.download_counter < this.amount_episodes) {
            Pair pair = this.handle_iteration();
            if (pair == null) {
                try {
                    TimeUnit.MILLISECONDS.sleep(50);
                } catch (InterruptedException e) {
                    assert true;
                }
                continue;
            }
            int episode = pair.integer();
            String video_url = pair.string();
            System.out.println(String.format("Downloading episode %s...", episode));
            String directory = String.format("%s\\One Piece - %s.mp4", this.dir, episode);
            this.download_video(video_url, directory);
            System.out.println(String.format("Finished downloading %s...", episode));
        }
    }
    
    public synchronized Pair handle_episode_iteration() {
        int current_episode = this.episode_counter + this.start;
        System.out.println(String.format("Processing episode %s...", current_episode));
        String starting_url = this.get_opt_url(current_episode);
        this.episode_counter++;
        Pair pair = new Pair(current_episode, starting_url);
        return pair;
    }

    public String get_video_url() {
        Pair starting = this.handle_episode_iteration();
        int episode = starting.integer();
        String starting_url = starting.string();
        String opt_source = this.request_source(starting_url);
        String html = this.extract_html_url(opt_source);
        String html_source = this.request_source(html);
        String video_url = this.extract_video_url(html_source);

        if (video_url == null) {
            System.out.println("Error while trying to get direct video url. Your IP might be banned.");
            return video_url;
        } 

        Pair final_pair = new Pair(episode, video_url);
        this.videos.add(final_pair);
        System.out.println(String.format("Finished processing episode: %s (%s)", episode, video_url));
        return video_url;
    }

    public void handle_videos() {
        while (this.episode_counter < this.amount_episodes) {
            if (this.get_video_url() == null) {
                System.exit(0);
            }
        }
    }
    
    public void main(int start, int end) {
        this.start = start;
        this.amount_episodes = end-start+1;
        this.check_dir();
    }
}
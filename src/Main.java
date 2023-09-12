import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Main {
    private static final String PROPERTIES_FILE_PATH = "config/application.properties";
    public static void main(String[] args) {
        List<String> keywords = readKeywordsFromFile("config/keywords.txt");

        for (String s:keywords) {
            String response = getConnection("search", s);
            System.out.println(response);
        }
    }

    public static String getApiKey() {
        Properties properties = new Properties();

        try (FileInputStream input = new FileInputStream(PROPERTIES_FILE_PATH)) {
            properties.load(input);
            return properties.getProperty("api_key");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("API 키를 가져올 수 없습니다.", e);
        }
    }

    public static List<String> readKeywordsFromFile(String filePath) {
        try {
            List<String> keywords = new ArrayList<>();
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = br.readLine()) != null) {
                keywords.add(line);
            }
            br.close();
            return keywords;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("키워드 파일 읽기 오류: " + e.getMessage(), e);
        }
    }

    public static String getConnection(String type, String param) {
        try {
            URI uri;
            String queryString = "key=" + getApiKey();

            switch (type) {
                case "search" -> queryString += "&part=snippet&maxResults=50" + "&q=" + param;
                case "videos", "channels" -> queryString += "&part=snippet,statistics" + "&id=" + param;
                default -> throw new IllegalArgumentException("타입 재지정 필요");
            }

            uri = new URI("https", "www.googleapis.com", "/youtube/v3/" + type, queryString, null);

            String urlString = uri.toASCIIString();

            System.out.println(urlString);

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");

            int responseCode = conn.getResponseCode();
            System.out.println("Response Code : " + responseCode);

            if (responseCode == 200) {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                }

                conn.disconnect();
                return sb.toString();
            } else {
                conn.disconnect();
                throw new RuntimeException("[Failed] 요청 실패 - Response Code : " + responseCode);
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException("HTTP 요청 중 오류 발생: " + e.getMessage(), e);
        }
    }
}
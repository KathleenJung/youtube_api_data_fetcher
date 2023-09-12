import Entity.Channel;
import Entity.Video;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
            parseData(response);
        }
    }

    private static void parseData(String jsonData) throws ClassCastException {
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonData);
            JSONArray dataArray = (JSONArray) jsonObject.get("items");

            for (Object obj:dataArray) {
                JSONObject item = (JSONObject) obj;
                JSONObject ids = (JSONObject) item.get("id");
                System.out.println(ids);

                JSONObject snippet = (JSONObject) item.get("snippet");

                Video video = parseVideoData((String) ids.get("videoId"));
                video.setVideoId((String) ids.get("videoId"));

                Channel channel = parseChannelData((String) snippet.get("channelId"));
                channel.setChannelId((String) snippet.get("channelId"));
                channel.setChannelTitle((String) snippet.get("channelTitle"));
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private static Channel parseChannelData(String channelId) throws ParseException {
        Channel channel = new Channel();

        JSONParser parser = new JSONParser();
        String channelInfo = getConnection("channels", channelId);

        JSONObject cJsonObject = (JSONObject) parser.parse(channelInfo);
        JSONArray cJsonArray = (JSONArray) cJsonObject.get("items");
        JSONObject cValue = (JSONObject) cJsonArray.get(0);
        JSONObject cStatistics = (JSONObject) cValue.get("statistics");
        channel.setViewCount(Integer.parseInt((String) cStatistics.get("viewCount")));
        channel.setSubscriberCount(Integer.parseInt((String) cStatistics.get("subscriberCount")));
        channel.setVideoCount(Integer.parseInt((String) cStatistics.get("videoCount")));

        return channel;
    }

    private static Video parseVideoData(String videoId) throws ParseException {
        Video video = new Video();

        JSONParser parser = new JSONParser();
        String videoInfo = getConnection("videos", videoId);

        JSONObject vJsonObject = (JSONObject) parser.parse(videoInfo);
        JSONArray vDataArray = (JSONArray) vJsonObject.get("items");
        JSONObject vValue = (JSONObject) vDataArray.get(0);
        JSONObject vStatistics = (JSONObject) vValue.get("statistics");
        video.setViewCount(Integer.parseInt((String)vStatistics.get("viewCount")));
        video.setLikeCount(Integer.parseInt((String)vStatistics.get("likeCount")));
        video.setCommentCount(Integer.parseInt((String)vStatistics.get("commentCount")));

        return video;
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
                case "search" -> queryString += "&part=snippet&maxResults=50&order=viewCount" + "&q=" + param;
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
                System.out.println(sb);
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
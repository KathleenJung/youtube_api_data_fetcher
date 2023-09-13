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
    private static final String OUT_FILE_PATH = "config/data.csv";

    private static List<String> apiKeys = new ArrayList<>();
    private static int currentApiKeyIndex = 0;

    private static File file;

    private static BufferedWriter bw;

    static {
        try (FileInputStream input = new FileInputStream(PROPERTIES_FILE_PATH)) {
            Properties properties = new Properties();
            properties.load(input);

            int apiKeyIndex = 1;
            String apiKey;
            while ((apiKey = properties.getProperty("api_key" + apiKeyIndex)) != null) {
                apiKeys.add(apiKey);
                apiKeyIndex++;
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("API 키를 가져올 수 없습니다.", e);
        }
    }

    public static void main(String[] args) throws IOException {
        List<String> keywords = readKeywordsFromFile("config/keywords.txt");

        file = new File(OUT_FILE_PATH);
        bw = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8));

        bw.write("ID,키워드,채널명,채널ID,채널정보,조회수,구독자수,영상수,VIEW,LIKE,COMMENT" + System.lineSeparator());

        for (String s : keywords) {
            String response = getConnection("search", s);
            parseData(s, response);
        }
        bw.close();
    }

    private static void parseData(String keyword, String jsonData) throws ClassCastException {

        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonData);
            JSONArray dataArray = (JSONArray) jsonObject.get("items");

            Video video;
            Channel channel;

            for (Object obj : dataArray) {
                JSONObject item = (JSONObject) obj;
                JSONObject ids = (JSONObject) item.get("id");
                System.out.println(ids);

                JSONObject snippet = (JSONObject) item.get("snippet");

                if(ids.get("videoId") == null) {
                    break;
                }
                video = parseVideoData((String) ids.get("videoId"));
                video.setId((String) ids.get("videoId"));

                channel = parseChannelData((String) snippet.get("channelId"));
                channel.setId((String) snippet.get("channelId"));
                channel.setTitle(((String) snippet.get("channelTitle")).replace(","," ").replaceAll("\\s+", " "));

                bw.write(video.getId() + "," + keyword + ","
                        + channel.getTitle() + "," + channel.getId() + "," + channel.getDescription() + ","
                        + channel.getViewCount() + "," + channel.getSubscriberCount() + "," + channel.getVideoCount() + ","
                        + video.getViewCount() + "," + video.getLikeCount() + "," + video.getCommentCount() + System.lineSeparator());
                bw.flush();
            }

        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
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
        JSONObject cSnnipet = (JSONObject) cValue.get("snippet");
        JSONObject cStatistics = (JSONObject) cValue.get("statistics");

        if (cSnnipet.get("description") != null) {
            channel.setDescription(((String) cSnnipet.get("description")).replace("\n", " ").replace(","," ").replaceAll("\\s+", " "));
        }
        if (cStatistics.get("viewCount") != null) {
            channel.setViewCount(Long.parseLong((String) cStatistics.get("viewCount")));
        }
        if (cStatistics.get("subscriberCount") != null) {
            channel.setSubscriberCount(Long.parseLong((String) cStatistics.get("subscriberCount")));
        }
        if (cStatistics.get("videoCount") != null) {
            channel.setVideoCount(Long.parseLong((String) cStatistics.get("videoCount")));
        }

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

        if (vStatistics.get("viewCount") != null) {
            video.setViewCount(Long.parseLong((String) vStatistics.get("viewCount")));
        }
        if (vStatistics.get("likeCount") != null) {
            video.setLikeCount(Long.parseLong((String) vStatistics.get("likeCount")));
        }
        if (vStatistics.get("commentCount") != null) {
            video.setCommentCount(Long.parseLong((String) vStatistics.get("commentCount")));
        }

        return video;
    }

    public static String getNextApiKey() {
        if (currentApiKeyIndex < apiKeys.size()) {
            return apiKeys.get(currentApiKeyIndex++);
        } else {
            throw new RuntimeException("더 이상 사용 가능한 API 키가 없습니다.");
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
            String queryString = "key=" + apiKeys.get(currentApiKeyIndex);

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
            } else if (responseCode == 403) {
                System.out.println("[Failed] API Key 일일 호출 횟수 초과 - Response Code : " + responseCode);
                getNextApiKey();
                return getConnection(type, param);
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
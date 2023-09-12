package Entity;

import lombok.Data;

@Data
public class Channel {
    private String channelId;
    private String channelTitle;
    private int subscriberCount;
    private int viewCount;
    private int videoCount;
}

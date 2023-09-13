package Entity;

import lombok.Data;

@Data
public class Channel {
    private String id;
    private String title;
    private String description;
    private Long subscriberCount;
    private Long viewCount;
    private Long videoCount;
}

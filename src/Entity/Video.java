package Entity;

import lombok.Data;

@Data
public class Video {
    private String videoId;
    private int viewCount;
    private int likeCount;
    private int commentCount;
}

package Entity;

import lombok.Data;

@Data
public class Video {
    private String id;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
}

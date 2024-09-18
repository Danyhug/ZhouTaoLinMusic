package cn.zhoutaolinmusic.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Objects;

@Data
@NoArgsConstructor
@ToString
public class HotVideo implements Serializable {

    private static final long serialVersionUID = 1L;

    String hotFormat;

    Double hot;

    Long videoId;

    String title;

    public HotVideo(Double hot, Long videoId, String title){
        this.hot = hot;
        this.videoId = videoId;
        this.title = title;
    }

    public void hotFormat(){
        double source = this.hot * 100;
        String formatNum = new DecimalFormat("0.00").format(source);

        this.setHotFormat(formatNum + "万");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HotVideo hotVideo = (HotVideo) o;
        return Objects.equals(videoId, hotVideo.videoId) &&
                Objects.equals(title, hotVideo.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(videoId, title);
    }
}

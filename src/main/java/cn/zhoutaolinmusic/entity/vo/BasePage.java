package cn.zhoutaolinmusic.entity.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

@Data
public class BasePage {

    private Long page = 1L;
    private Long limit = 15L;

    public IPage page(){
        long currentPage = (this.page != null) ? this.page : 1L;
        long recordsPerPage = (this.limit != null) ? this.limit : 15L;

        return new Page<>(currentPage, recordsPerPage);
    }
}

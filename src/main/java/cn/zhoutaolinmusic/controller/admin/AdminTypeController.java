package cn.zhoutaolinmusic.controller.admin;

import cn.zhoutaolinmusic.authority.Authority;
import cn.zhoutaolinmusic.entity.video.VideoType;
import cn.zhoutaolinmusic.entity.vo.BasePage;
import cn.zhoutaolinmusic.service.video.TypeService;
import cn.zhoutaolinmusic.utils.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/type")
public class AdminTypeController {
    @Autowired
    private TypeService typeService;

    /**
     * 获取分类
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Authority("admin:type:get")
    public Result get(@PathVariable Long id) {
        return Result.ok(typeService.getById(id));
    }

    /**
     * 根据页数获取分类
     * @param basePage
     * @return
     */
    @GetMapping("/page")
    @Authority("admin:type:page")
    public Result page(BasePage basePage) {
        IPage page = typeService.page(basePage.page(), null);
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @PostMapping("")
    @Authority("admin:type:add")
    public Result add(@RequestBody @Validated VideoType videoType) {
        int count = typeService.count(new LambdaQueryWrapper<VideoType>().eq(VideoType::getName, videoType.getName()));
        if (count > 0) {
            return Result.error("分类已存在");
        }
        typeService.save(videoType);
        return Result.ok("分类添加成功");
    }

    /**
     * 修改分类
     * @param videoType
     * @return
     */
    @PutMapping("")
    @Authority("admin:type:update")
    public Result update(@RequestBody @Validated VideoType videoType) {
        typeService.updateById(videoType);
        return Result.ok("分类修改成功");
    }

    @DeleteMapping("/{id}")
    @Authority("admin:type:delete")
    public Result delete(@PathVariable Long id) {
        typeService.removeById(id);
        return Result.ok("分类删除成功");
    }
}

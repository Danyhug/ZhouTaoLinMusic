package cn.zhoutaolinmusic.controller.admin;

import cn.zhoutaolinmusic.authority.Authority;
import cn.zhoutaolinmusic.config.LocalCache;
import cn.zhoutaolinmusic.entity.Setting;
import cn.zhoutaolinmusic.entity.json.SettingScoreJson;
import cn.zhoutaolinmusic.service.SettingService;
import cn.zhoutaolinmusic.utils.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/setting")
public class AdminSettingController {

    @Autowired
    private SettingService settingService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping
    @Authority("admin:setting:get")
    public Result get() throws JsonProcessingException {
        final Setting setting = settingService.list(null).get(0);
        final SettingScoreJson settingScoreJson = objectMapper.readValue(setting.getAuditPolicy(), SettingScoreJson.class);
        setting.setSettingScoreJson(settingScoreJson);
        return Result.ok(setting);
    }


    @PutMapping
    @Authority("admin:setting:update")
    public Result update(@RequestBody @Validated Setting setting){
        settingService.updateById(setting);
        for (String s : setting.getAllowIp().split(",")) {
            LocalCache.put(s,true);
        }
        return Result.ok("修改成功");
    }
}

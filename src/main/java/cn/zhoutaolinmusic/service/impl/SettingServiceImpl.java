package cn.zhoutaolinmusic.service.impl;

import cn.zhoutaolinmusic.entity.Setting;
import cn.zhoutaolinmusic.mapper.SettingMapper;
import cn.zhoutaolinmusic.service.SettingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


@Service
public class SettingServiceImpl extends ServiceImpl<SettingMapper, Setting> implements SettingService {

}

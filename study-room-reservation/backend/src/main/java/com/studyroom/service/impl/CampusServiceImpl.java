package com.studyroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyroom.common.BusinessException;
import com.studyroom.entity.Campus;
import com.studyroom.mapper.CampusMapper;
import com.studyroom.service.CampusService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class CampusServiceImpl extends ServiceImpl<CampusMapper, Campus> implements CampusService {

    @Override
    public IPage<Campus> pageCampus(Page<Campus> page, String name) {
        LambdaQueryWrapper<Campus> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Campus::getName, name);
        }
        wrapper.orderByDesc(Campus::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public Campus getById(Long id) {
        Campus campus = super.getById(id);
        if (campus == null) {
            throw new BusinessException("校区不存在");
        }
        return campus;
    }

    @Override
    public void add(Campus campus) {
        campus.setCreateTime(LocalDateTime.now());
        this.save(campus);
    }

    @Override
    public void update(Campus campus) {
        campus.setUpdateTime(LocalDateTime.now());
        this.updateById(campus);
    }

    @Override
    public void delete(Long id) {
        // TODO: 检查该校区下是否有楼栋，如果有则不允许删除
        this.removeById(id);
    }
}
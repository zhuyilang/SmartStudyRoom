package com.studyroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyroom.entity.Blacklist;
import com.studyroom.mapper.BlacklistMapper;
import com.studyroom.service.BlacklistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BlacklistServiceImpl implements BlacklistService {

    private static final Logger log = LoggerFactory.getLogger(BlacklistServiceImpl.class);

    @Autowired private BlacklistMapper blacklistMapper;
    @Autowired private JdbcTemplate jdbcTemplate;

    private static final int MISS_THRESHOLD = 3;
    private static final int BAN_DAYS = 10;

    @Override
    public boolean isBanned(Long userId) {
        Blacklist ban = blacklistMapper.findActiveBan(userId);
        return ban != null;
    }

    @Override
    @Transactional
    public void recordMiss(Long userId) {
        Blacklist existing = blacklistMapper.findActiveBan(userId);

        if (existing != null) {
            // Already has a record — increment
            int newCount = existing.getMissCount() + 1;
            existing.setMissCount(newCount);
            if (newCount >= MISS_THRESHOLD) {
                existing.setBannedUntil(LocalDateTime.now().plusDays(BAN_DAYS));
                log.warn("User {} banned until {} (miss count: {})",
                        userId, existing.getBannedUntil(), newCount);
            }
            blacklistMapper.updateById(existing);
        } else {
            // Check if there's an old (expired) record
            LambdaQueryWrapper<Blacklist> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Blacklist::getUserId, userId);
            Blacklist old = blacklistMapper.selectOne(wrapper);

            if (old != null) {
                // Update existing record
                int newCount = old.getMissCount() + 1;
                old.setMissCount(newCount);
                if (newCount >= MISS_THRESHOLD) {
                    old.setBannedUntil(LocalDateTime.now().plusDays(BAN_DAYS));
                    log.warn("User {} banned until {} (miss count: {})",
                            userId, old.getBannedUntil(), newCount);
                }
                blacklistMapper.updateById(old);
            } else {
                // First miss — create new record
                Blacklist bl = new Blacklist();
                bl.setUserId(userId);
                bl.setMissCount(1);
                bl.setCreateTime(LocalDateTime.now());
                blacklistMapper.insert(bl);
                log.info("User {} first miss recorded", userId);
            }
        }
    }

    @Override
    public List<Blacklist> getActiveList() {
        LambdaQueryWrapper<Blacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.gt(Blacklist::getBannedUntil, LocalDateTime.now());
        return blacklistMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void removeBan(Long userId) {
        // Use JdbcTemplate to ensure NULL is written (MyBatis-Plus skips null fields by default)
        int updated = jdbcTemplate.update(
                "UPDATE blacklist SET miss_count = 0, banned_until = NULL WHERE user_id = ?", userId);
        if (updated > 0) {
            log.info("User {} ban removed", userId);
        }
    }
}

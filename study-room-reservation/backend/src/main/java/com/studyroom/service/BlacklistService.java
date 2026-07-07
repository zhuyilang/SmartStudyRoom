package com.studyroom.service;

import com.studyroom.entity.Blacklist;

import java.util.List;

public interface BlacklistService {

    /** Check if user is currently banned */
    boolean isBanned(Long userId);

    /** Record a missed reservation (no-show) */
    void recordMiss(Long userId);

    /** Get all active blacklist entries */
    List<Blacklist> getActiveList();

    /** Admin: remove user from blacklist */
    void removeBan(Long userId);
}

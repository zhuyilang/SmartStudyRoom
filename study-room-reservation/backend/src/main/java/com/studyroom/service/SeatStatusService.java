package com.studyroom.service;

public interface SeatStatusService {

    /** Mark seat as occupied */
    void occupySeat(Long seatId);

    /** Mark seat as free */
    void releaseSeat(Long seatId);
}

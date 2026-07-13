package com.studyroom.service;

import com.studyroom.entity.Reservation;

import java.util.List;

public interface ReservationService {

    /** Book a seat */
    Reservation reserve(Long userId, Long seatId, Long roomId);

    /** Cancel a reservation */
    void cancel(Long reservationId, Long userId);

    /** Sign in to a reservation */
    void signIn(Long reservationId, Long userId);

    /** Get user's reservations */
    List<Reservation> getMyReservations(Long userId, Integer status);

    /** Get single reservation detail */
    Reservation getById(Long reservationId);

    /** Get all active reservations */
    List<Reservation> getActiveReservations();
    void release(Long reservationId, Long userId);
}

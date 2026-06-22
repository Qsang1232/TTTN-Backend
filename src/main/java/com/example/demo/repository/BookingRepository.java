package com.example.demo.repository;

import com.example.demo.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    // 1. Kiểm tra trùng lịch (Tránh đặt chồng chéo)
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END " +
           "FROM Booking b " +
           "WHERE b.court.id = :courtId " +
           "AND b.status != 'CANCELLED' " +
           "AND b.startTime < :endTime " +
           "AND b.endTime > :startTime")
    boolean existsConflictingBooking(@Param("courtId") Long courtId, 
                                     @Param("startTime") LocalDateTime startTime, 
                                     @Param("endTime") LocalDateTime endTime);

    // 2. Lấy danh sách booking trong ngày (Để vẽ lịch hiển thị giờ trống)
    // Chỉ lấy đơn chưa hủy
    @Query("SELECT b FROM Booking b WHERE b.court.id = :courtId " +
           "AND b.status != 'CANCELLED' " +
           "AND b.startTime BETWEEN :startOfDay AND :endOfDay")
    List<Booking> findBookingsByCourtAndDate(@Param("courtId") Long courtId, 
                                             @Param("startOfDay") LocalDateTime startOfDay,
                                             @Param("endOfDay") LocalDateTime endOfDay);

    // 3. Thống kê
    @Query("SELECT SUM(b.totalPrice) FROM Booking b WHERE b.status != 'CANCELLED'")
    Double calculateTotalRevenue();

    @Query("SELECT COUNT(b) FROM Booking b WHERE DATE(b.startTime) = CURRENT_DATE")
    Long countBookingsToday();
}
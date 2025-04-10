package com.project.quanlycanghangkhong.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "flights")
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Số hiệu chuyến bay
    @Column(name = "flight_number", nullable = false)
    private String flightNumber;

    // Sân bay khởi hành (dự kiến)
    @Column(name = "departure_airport")
    private String departureAirport;

    // Sân bay hạ cánh (dự kiến)
    @Column(name = "arrival_airport")
    private String arrivalAirport;

    // Giờ khởi hành dự kiến (LocalTime)
    @Column(name = "departure_time")
    private LocalTime departureTime;

    // Giờ hạ cánh dự kiến (LocalTime)
    @Column(name = "arrival_time")
    private LocalTime arrivalTime;

    // Ngày bay (LocalDate)
    @Column(name = "flight_date")
    private LocalDate flightDate;

    // --- Các trường thêm vào ---

    // Giờ cất cánh thực tế tại sân bay đi
    @Column(name = "actual_departure_time")
    private LocalTime actualDepartureTime;

    // Giờ hạ cánh thực tế tại sân bay đến
    @Column(name = "actual_arrival_time")
    private LocalTime actualArrivalTime;

    // Giờ cất cánh thực tế tại sân bay đến (nếu có, ví dụ phục vụ trường hợp turnaround)
    @Column(name = "actual_departure_time_at_arrival")
    private LocalTime actualDepartureTimeAtArrival;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructor không tham số
    public Flight() {
    }

    // Constructor có tham số (chỉ dùng cho các trường cơ bản, bạn có thể mở rộng nếu cần)
    public Flight(String flightNumber, String departureAirport, String arrivalAirport,
                  LocalTime departureTime, LocalTime arrivalTime, LocalDate flightDate) {
        this.flightNumber = flightNumber;
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.flightDate = flightDate;
    }

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getDepartureAirport() {
        return departureAirport;
    }

    public void setDepartureAirport(String departureAirport) {
        this.departureAirport = departureAirport;
    }

    public String getArrivalAirport() {
        return arrivalAirport;
    }

    public void setArrivalAirport(String arrivalAirport) {
        this.arrivalAirport = arrivalAirport;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public LocalDate getFlightDate() {
        return flightDate;
    }

    public void setFlightDate(LocalDate flightDate) {
        this.flightDate = flightDate;
    }

    public LocalTime getActualDepartureTime() {
        return actualDepartureTime;
    }

    public void setActualDepartureTime(LocalTime actualDepartureTime) {
        this.actualDepartureTime = actualDepartureTime;
    }

    public LocalTime getActualArrivalTime() {
        return actualArrivalTime;
    }

    public void setActualArrivalTime(LocalTime actualArrivalTime) {
        this.actualArrivalTime = actualArrivalTime;
    }

    public LocalTime getActualDepartureTimeAtArrival() {
        return actualDepartureTimeAtArrival;
    }

    public void setActualDepartureTimeAtArrival(LocalTime actualDepartureTimeAtArrival) {
        this.actualDepartureTimeAtArrival = actualDepartureTimeAtArrival;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

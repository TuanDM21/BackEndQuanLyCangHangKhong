package com.project.quanlycanghangkhong.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.project.quanlycanghangkhong.dto.CreateFlightRequest;
import com.project.quanlycanghangkhong.dto.UpdateFlightRequest;
import com.project.quanlycanghangkhong.dto.FlightDTO;
import com.project.quanlycanghangkhong.dto.FlightTimeUpdateRequest;
import com.project.quanlycanghangkhong.model.Flight;

public interface FlightService {
	  FlightDTO createFlight(Flight flight);
	  FlightDTO createFlightFromRequest(CreateFlightRequest request);
	    List<FlightDTO> getAllFlights();
	    Optional<FlightDTO> getFlightById(Long id);
	    FlightDTO updateFlight(Long id, Flight flightData);
	    FlightDTO updateFlightFromRequest(Long id, UpdateFlightRequest request);
	    void deleteFlight(Long id);
	    List<FlightDTO> searchFlights(String keyword);
	    
	    // Thêm method lấy danh sách chuyến bay của hôm nay
	    List<FlightDTO> getTodayFlights();
	    // Thêm method mới: tìm các chuyến bay có flightDate chính xác
	    List<FlightDTO> getFlightsByExactDate(LocalDate flightDate);
	    List<FlightDTO> getFlightsByDateAndKeyword(LocalDate date, String keyword);
	    public void updateFlightTimes(Long id, FlightTimeUpdateRequest req) ;
	    
	    // Lấy entity Flight theo id
	    Flight getFlightEntityById(Long id);

	    /**
	     * Gửi notification cho các user phục vụ chuyến bay hoặc trực ca khi thay đổi actual time
	     */
	    void notifyUsersOnActualTimeChange(Long flightId, java.time.LocalTime actualTime, String eventType, String airportCode);
	    
	    // Thêm method tìm kiếm theo nhiều tiêu chí
	    List<FlightDTO> searchFlightsByCriteria(String date, String flightNumber, String departureAirport, String arrivalAirport);
}

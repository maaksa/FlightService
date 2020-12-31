package sk_microservices.FlightService.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sk_microservices.FlightService.entites.Flight;
import sk_microservices.FlightService.repository.FlightRepository;

import java.awt.print.Book;
import java.util.Collections;
import java.util.List;

@Service
public class FlightService {

    @Autowired
    FlightRepository flightRepository;

    public Page<Flight> findPaginated(Pageable pageable) {

        List<Flight> flights = flightRepository.findAll();

        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;
        List<Flight> list;

        if (flights.size() < startItem) {
            list = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, flights.size());
            list = flights.subList(startItem, toIndex);
        }

        Page<Flight> flightPage = new PageImpl<Flight>(list, PageRequest.of(currentPage, pageSize), flights.size());

        return flightPage;
    }

}

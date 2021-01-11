package sk_microservices.FlightService.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sk_microservices.FlightService.entites.Flight;
import sk_microservices.FlightService.forms.AddFlightForm;
import sk_microservices.FlightService.repository.FlightRepository;

import java.awt.print.Book;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    public Flight findById(long id){
        Optional<Flight> flight = flightRepository.findById(id);
        return flight.orElse(null);
    }

    public void deleteById(long id){
        flightRepository.deleteById(id);
    }

    public Flight save(AddFlightForm addFlightForm){
        Flight flight = new Flight(addFlightForm.getAvion(), addFlightForm.getPocetnaDestinacija(),
                addFlightForm.getKrajnjaDestinacija(), addFlightForm.getDuzinaLeta(), addFlightForm.getCena());
        return flightRepository.save(flight);
    }

}

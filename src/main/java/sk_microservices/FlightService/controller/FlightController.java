package sk_microservices.FlightService.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sipios.springsearch.anotation.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.*;
import sk_microservices.FlightService.entites.Airplane;
import sk_microservices.FlightService.entites.Flight;
import sk_microservices.FlightService.forms.AddFlightForm;
import sk_microservices.FlightService.forms.ticketservice.TicketForm;
import sk_microservices.FlightService.repository.AirplaneRepository;
import sk_microservices.FlightService.repository.FlightRepository;
import sk_microservices.FlightService.utils.UtilsMethods;

import javax.jms.Queue;
import java.text.SimpleDateFormat;
import java.util.*;

import static sk_microservices.FlightService.utils.UtilsMethods.HEADER_STRING;


@RestController
@RequestMapping("/flight")
public class FlightController {

    private AirplaneRepository airplaneRepository;
    private FlightRepository flightRepository;

    private JmsTemplate jmsTemplate;

    private Queue userQueue;

    private Queue ticketQueue;

    @Autowired
    public FlightController(JmsTemplate jmsTemplate, Queue userQueue, Queue ticketQueue,
                            AirplaneRepository airplaneRepository, FlightRepository flightRepository) {
        this.jmsTemplate = jmsTemplate;
        this.userQueue = userQueue;
        this.ticketQueue = ticketQueue;
        this.airplaneRepository = airplaneRepository;
        this.flightRepository = flightRepository;
    }

    @GetMapping("/searchFlight")
    public ResponseEntity<List<Flight>> searchForFlight(@SearchSpec Specification<Flight> specs, @RequestHeader(value = HEADER_STRING) String token) {

        if (token.isEmpty()) {
            System.out.println("parazan token");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        ResponseEntity<Boolean> response = UtilsMethods.checkAuthorization("http://localhost:8080/checkUser", token);
        if (response.getBody() == null) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(flightRepository.findAll(Specification.where(specs)), HttpStatus.OK);
    }

    @PostMapping("/save")
    public ResponseEntity<String> addFlight(@RequestBody AddFlightForm addFlightForm, @RequestHeader(value = HEADER_STRING) String token) {

        try {

            if (token.isEmpty()) {
                System.out.println("parazan token");
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            ResponseEntity<Boolean> response = UtilsMethods.checkAuthorization("http://localhost:8080/admin/checkAdmin", token);
            if (response.getBody() == null) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            Airplane avion = addFlightForm.getAvion();
            airplaneRepository.save(avion);

            Flight let = new Flight(avion, addFlightForm.getPocetnaDestinacija(), addFlightForm.getKrajnjaDestinacija(),
                    addFlightForm.getDuzinaLeta(), addFlightForm.getCena());

            flightRepository.save(let);

            return new ResponseEntity<String>("successfully added", HttpStatus.ACCEPTED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/capacity/{id}")
    public ResponseEntity<Integer> getCapacity(@PathVariable long id) {
        try {
            int capacity = flightRepository.getCapacityForFlight(id);
            return new ResponseEntity<>(capacity, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    //samo sa slobodnim kapacitetom mesta za putnike
    @GetMapping("/list")
    public ResponseEntity<List<Flight>> getFlights(@RequestHeader(value = HEADER_STRING) String token) {

        try {
            if (token.isEmpty()) {
                System.out.println("parazan token");
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            ResponseEntity<Boolean> response = UtilsMethods.checkAuthorization("http://localhost:8080/checkUser", token);
            if (response.getBody() == null) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            List<Flight> flightsToReturn = new ArrayList<>();
            List<Flight> flights = flightRepository.findAll();

            for (Flight f : flights) {
                if (f.getAvion().getKapacitetPutnika() != 0) {
                    flightsToReturn.add(f);
                }
            }

            return new ResponseEntity<List<Flight>>(flightsToReturn, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/allFlights")
    public ResponseEntity<List<Flight>> getAllFlights() {

        try {
            List<Flight> flights = flightRepository.findAll();

            return new ResponseEntity<List<Flight>>(flights, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteFlight(@RequestHeader(value = HEADER_STRING) String token, @PathVariable long id) {

        try {

            if (token.isEmpty()) {
                System.out.println("parazan token");
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            ResponseEntity<Boolean> res = UtilsMethods.checkAuthorization("http://localhost:8080/admin/checkAdmin", token);
            if (res.getBody() == null) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            ResponseEntity<Object> response = UtilsMethods.sendGet("http://localhost:8082/ticket/allTicketsForFlight/" + id);
            if (response.getBody() == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            ArrayList<LinkedHashMap<Object, Object>> list = (ArrayList<LinkedHashMap<Object, Object>>) response.getBody();
            for (LinkedHashMap<Object, Object> hashMap : list) {
                Gson gson = new Gson();
                String jsonString = gson.toJson(hashMap, Map.class);
                jmsTemplate.convertAndSend(userQueue, jsonString);
                //todo napravi da u ticket queue stavlja samo jednom zato sto sve karte imaju isti flight id
                jmsTemplate.convertAndSend(ticketQueue, jsonString);
            }

            //todo otkomentarisati
            //flightRepository.deleteById(id);

            return new ResponseEntity<>("successfully deleted", HttpStatus.ACCEPTED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/miles/{id}")
    public ResponseEntity<Integer> flightLength(@PathVariable long id) {
        try {
            int miles = flightRepository.getLengthForFlight(id);
            return new ResponseEntity<>(miles, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    //za GUI //todo
//    @GetMapping("/list")
//    public  String getFlights(Model theModel) {
//
//        try {
//            List<Flight> theFlights = flightRepository.findAll();
//
//            theModel.addAttribute("flights", theFlights);
//
//            return "flights/list-flights";
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

}

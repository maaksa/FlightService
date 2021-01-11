package sk_microservices.FlightService.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sipios.springsearch.anotation.SearchSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sk_microservices.FlightService.entites.Airplane;
import sk_microservices.FlightService.entites.Flight;
import sk_microservices.FlightService.forms.AddAirplaneForm;
import sk_microservices.FlightService.forms.AddFlightForm;
import sk_microservices.FlightService.forms.FlightForm;
import sk_microservices.FlightService.forms.ticketservice.TicketForm;
import sk_microservices.FlightService.repository.AirplaneRepository;
import sk_microservices.FlightService.repository.FlightRepository;
import sk_microservices.FlightService.service.FlightService;
import sk_microservices.FlightService.utils.UtilsMethods;

import javax.jms.Queue;
import java.awt.print.Book;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static sk_microservices.FlightService.utils.UtilsMethods.HEADER_STRING;


@Controller
@RequestMapping("/flight")
public class FlightController {

    private AirplaneRepository airplaneRepository;
    private FlightRepository flightRepository;
    private FlightService flightService;

    private JmsTemplate jmsTemplate;

    private Queue userQueue;

    private Queue ticketQueue;

    @Autowired
    public FlightController(JmsTemplate jmsTemplate, Queue userQueue, Queue ticketQueue,
                            AirplaneRepository airplaneRepository, FlightRepository flightRepository, FlightService flightService) {
        this.jmsTemplate = jmsTemplate;
        this.userQueue = userQueue;
        this.ticketQueue = ticketQueue;
        this.airplaneRepository = airplaneRepository;
        this.flightRepository = flightRepository;
        this.flightService = flightService;
    }

    @GetMapping("/searchFlight")
    public ResponseEntity<Page<Flight>> searchForFlight(@SearchSpec Specification<Flight> specs,
                                                        @RequestParam Optional<Integer> page,
                                                        @RequestParam Optional<Integer> size,
                                                        @RequestHeader(value = HEADER_STRING) String token) {

        if (token.isEmpty()) {
            System.out.println("parazan token");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        ResponseEntity<Boolean> response = UtilsMethods.checkAuthorization("http://localhost:8762/rest-user-service/user/checkUser", token);
        if (response.getBody() == null) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Pageable pageRequest = PageRequest.of(page.orElse(0), size.orElse(2));
        System.out.println(pageRequest);

        return new ResponseEntity<Page<Flight>>(flightRepository.findAll(Specification.where(specs), pageRequest), HttpStatus.OK);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Flight> getFlight(@RequestHeader(value = HEADER_STRING) String token, @PathVariable Long id) {

        if (token.isEmpty()) {
            System.out.println("prazan token");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        ResponseEntity<Boolean> response = UtilsMethods.checkAuthorization("http://localhost:8762/rest-user-service/user/checkUser", token);
        if (response.getBody() == null) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }


        System.out.println(id);
        return new ResponseEntity<Flight>(flightService.findById(id), HttpStatus.ACCEPTED);
    }

    @PostMapping("/admin/save")
    public ResponseEntity<String> addFlight(@RequestBody FlightForm addFlightForm, @RequestHeader(value = HEADER_STRING) String token) {

        try {
            if (token.isEmpty()) {
                System.out.println("parazan token");
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            ResponseEntity<Boolean> response = UtilsMethods.checkAuthorization("http://localhost:8762/rest-user-service/admin/checkAdmin", token);
            if (response.getBody() == null) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            AddAirplaneForm addAirplaneForm = addFlightForm.getAvion();
            Airplane avion = new Airplane(addAirplaneForm.getNaziv(), addAirplaneForm.getKapacitetPutnika());
            if(addAirplaneForm.getId() != null && addAirplaneForm.getId() != 0){
                avion.setId(addAirplaneForm.getId());
            }else{
                avion = airplaneRepository.save(avion);
            }

            Flight let = new Flight(avion, addFlightForm.getPocetnaDestinacija(), addFlightForm.getKrajnjaDestinacija(),
                    addFlightForm.getDuzinaLeta(), addFlightForm.getCena());
            if(addFlightForm.getId() != 0){
                let.setId(addFlightForm.getId());
            }

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
            System.out.println(capacity);
            return new ResponseEntity<>(capacity, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("admin/delete/{id}")
    public ResponseEntity<String> deleteFlight(@RequestHeader(value = HEADER_STRING) String token, @PathVariable long id) {

        try {

            if (token.isEmpty()) {
                System.out.println("parazan token");
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            ResponseEntity<Boolean> res = UtilsMethods.checkAuthorization("http://localhost:8762/rest-user-service/admin/checkAdmin", token);
            if (res.getBody() == null) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            ResponseEntity<Object> response = UtilsMethods.sendGet("http://localhost:8762/rest-ticket-service/ticket/allTicketsForFlight/" + id, token);
            if (response.getBody() == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            ArrayList<LinkedHashMap<Object, Object>> list = (ArrayList<LinkedHashMap<Object, Object>>) response.getBody();
            LinkedHashMap<Object, Object> miles = new LinkedHashMap<>();
            System.out.println(list);

            if(!list.isEmpty()) {
                list.get(0).put("miles", flightRepository.getLengthForFlight(id));
            }
            System.out.println(list);
            for (LinkedHashMap<Object, Object> hashMap : list) {
                Gson gson = new Gson();
                String jsonString = gson.toJson(hashMap, Map.class);
                jmsTemplate.convertAndSend(userQueue, jsonString);
                //todo napravi da u ticket queue stavlja samo jednom zato sto sve karte imaju isti flight id
                jmsTemplate.convertAndSend(ticketQueue, jsonString);
            }

            //todo otkomentarisati
            flightService.deleteById(id);

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

}

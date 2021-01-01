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
                                                        @RequestHeader(value = HEADER_STRING) String token) {

        if (token.isEmpty()) {
            System.out.println("parazan token");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        ResponseEntity<Boolean> response = UtilsMethods.checkAuthorization("http://localhost:8080/checkUser", token);
        if (response.getBody() == null) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Pageable pageRequest = PageRequest.of(page.orElse(0), 2);

        return new ResponseEntity<Page<Flight>>(flightRepository.findAll(Specification.where(specs), pageRequest), HttpStatus.OK);
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

    //todo bez GUI
//    @GetMapping("/list")
//    public ResponseEntity<Page<Flight>> getFlights(@RequestHeader(value = HEADER_STRING) String token, @RequestParam Optional<Integer> page) {
//
//        try {
//            if (token.isEmpty()) {
//                System.out.println("parazan token");
//                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
//            }
//
//            ResponseEntity<Boolean> response = UtilsMethods.checkAuthorization("http://localhost:8080/checkUser", token);
//            if (response.getBody() == null) {
//                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
//            }
//
//            Pageable pageRequest = PageRequest.of(page.orElse(0), 2);
//
//            Page<Flight> flights = flightRepository.findAllWithCapacity(pageRequest);
//
//            return new ResponseEntity<Page<Flight>>(flights, HttpStatus.ACCEPTED);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//        }
//    }

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

    //todo za GUI, dodati proveru kapaciteta
    @GetMapping("/list")
    public  String getFlights(Model theModel,  @RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size) {
        try {

//            Pageable pageRequest = PageRequest.of(page.orElse(0), 2);
//
//            Page<Flight> flights = flightRepository.findAllWithCapacity(pageRequest);

            int currentPage = page.orElse(1);
            int pageSize = size.orElse(2);

            Page<Flight> flightPage = flightService.findPaginated(PageRequest.of(currentPage - 1, pageSize));

            theModel.addAttribute("flightPage", flightPage);

            int totalPages = flightPage.getTotalPages();
            if (totalPages > 0) {
                List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                        .boxed()
                        .collect(Collectors.toList());
                theModel.addAttribute("pageNumbers", pageNumbers);
            }

            return "flights/list-flights";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    //todo za GUI
    @PostMapping("/admin/save")
    public String saveAdminFlight(@ModelAttribute("flight") Flight theFlight) {
        try {
            flightRepository.save(theFlight);

            return "redirect:/flight/admin/list";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/error";
        }
    }

    //todo za GUI
    @GetMapping("/admin/list")
    public  String getAdminFlights(Model theModel) {
        try {
            List<Flight> theFlights = flightRepository.findAll();

            theModel.addAttribute("flights", theFlights);

            return "flights/list-admin-flights";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //todo za GUI
    @GetMapping("/admin/delete")
    public String delete(@RequestParam("flightId") long theId) {
        try {
            flightRepository.deleteById(theId);

            return "redirect:/flight/admin/list";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/error";
        }
    }

    //todo za GUI
    @GetMapping("/admin/showFormForAdd")
    public String showFormForAdd(Model theModel) {
        try {

            theModel.addAttribute("flight", new AddFlightForm());
            theModel.addAttribute("airplanes", airplaneRepository.findAll());

            return "flights/flight-form";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/error";
        }
    }

}

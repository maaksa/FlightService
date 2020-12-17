package sk_microservices.FlightService.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sk_microservices.FlightService.entites.Airplane;
import sk_microservices.FlightService.entites.Flight;
import sk_microservices.FlightService.forms.AddFlightForm;
import sk_microservices.FlightService.repository.AirplaneRepository;
import sk_microservices.FlightService.repository.FlightRepository;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/flight")
public class FlightController {

    private AirplaneRepository airplaneRepository;
    private FlightRepository flightRepository;

    @Autowired
    public FlightController(AirplaneRepository airplaneRepository, FlightRepository flightRepository) {
        this.airplaneRepository = airplaneRepository;
        this.flightRepository = flightRepository;
    }

    @PostMapping("/save")
    public ResponseEntity<String> addFlight(@RequestBody AddFlightForm addFlightForm) {

        try {
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

    //samo sa slobodnim kapacitetom mesta za putnike
    @GetMapping("/list")
    public ResponseEntity<List<Flight>> getFlights() {

        try {
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
    public ResponseEntity<String> deleteFlight(@PathVariable long id) {

        try {
            flightRepository.deleteById(id);

            return new ResponseEntity<>("successfully deleted", HttpStatus.ACCEPTED);
        } catch (Exception e) {
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

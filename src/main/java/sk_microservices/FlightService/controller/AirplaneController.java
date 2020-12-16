package sk_microservices.FlightService.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sk_microservices.FlightService.entites.Airplane;
import sk_microservices.FlightService.entites.Flight;
import sk_microservices.FlightService.forms.AddAirplaneForm;
import sk_microservices.FlightService.forms.AddFlightForm;
import sk_microservices.FlightService.repository.AirplaneRepository;
import sk_microservices.FlightService.repository.FlightRepository;

import java.util.List;

@RestController
@RequestMapping("/airplane")
public class AirplaneController {

    public static final String HEADER_STRING = "Authorization";

    private AirplaneRepository airplaneRepository;
    private FlightRepository flightRepository;

    @Autowired
    public AirplaneController(AirplaneRepository airplaneRepository, FlightRepository flightRepository) {
        this.airplaneRepository = airplaneRepository;
        this.flightRepository = flightRepository;
    }

    //za GUI //todo
//    @GetMapping("/showFormForAdd")
//    public String showFormForAdd(Model theModel) {
//
//        System.out.println("dosao");
//
//
//        try {
//            Airplane theAirplane = new Airplane();
//
//            theModel.addAttribute("airplane", theAirplane);
//
//            System.out.println("dosao");
//
//            return "airplanes/airplane-form";
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    //za GUI //todo
//    @PostMapping("/save")
//    public String saveAirplane(@ModelAttribute("airplane") Airplane theAirplane) {
//
//        airplaneRepository.save(theAirplane);
//
//        return "redirect:/airplane/list";
//    }

    //dodavanje novoga aviona, poziva se iz admin controllera
    @PostMapping("/save")
    public ResponseEntity<String> addAirplane(@RequestBody AddAirplaneForm addAirplaneForm) {

        try {
            Airplane avion = new Airplane(addAirplaneForm.getNaziv(), addAirplaneForm.getKapacitetPutnika());

            airplaneRepository.save(avion);

            return new ResponseEntity<String>("successfully added", HttpStatus.ACCEPTED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<Airplane>> getFlights() {

        try {
            List<Airplane> airplaneList = airplaneRepository.findAll();

            return new ResponseEntity<List<Airplane>>(airplaneList, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteAirplane(@PathVariable long id) {

        try {
            airplaneRepository.deleteById(id);

            return new ResponseEntity<>("successfully deleted", HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    //za GUI //todo
//    @GetMapping("/list")
//    public String getFlights(/*@RequestHeader(value = HEADER_STRING, required = false) String token, */Model theModel) {
//
//        try {
//            List<Airplane> theAirplanes = airplaneRepository.findAll();
//
//            theModel.addAttribute("airplanes", theAirplanes);
//
//            //System.out.println(token);
//
//            return "airplanes/list-airplanes";
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

//    @GetMapping("/delete")
//    public String delete(@RequestParam("airplaneId") long theId) {
//
//        airplaneRepository.deleteById(theId);
//
//        return "redirect:/airplane/list";
//    }

}

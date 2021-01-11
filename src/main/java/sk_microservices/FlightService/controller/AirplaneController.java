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
import sk_microservices.FlightService.utils.UtilsMethods;

import java.util.List;

import static sk_microservices.FlightService.utils.UtilsMethods.HEADER_STRING;

@Controller
@RequestMapping("/airplane")
public class AirplaneController {

    private AirplaneRepository airplaneRepository;
    private FlightRepository flightRepository;

    @Autowired
    public AirplaneController(AirplaneRepository airplaneRepository, FlightRepository flightRepository) {
        this.airplaneRepository = airplaneRepository;
        this.flightRepository = flightRepository;
    }

    @PostMapping("/save")
    public ResponseEntity<String> addAirplane(@RequestHeader(value = HEADER_STRING) String token, @RequestBody AddAirplaneForm addAirplaneForm) {

        try {

            if (token.isEmpty()) {
                System.out.println("parazan token");
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            ResponseEntity<Boolean> res = UtilsMethods.checkAuthorization("http://localhost:8762/rest-user-service/admin/checkAdmin", token);
            if (res.getBody() == null) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            Airplane avion = new Airplane(addAirplaneForm.getNaziv(), addAirplaneForm.getKapacitetPutnika());
            airplaneRepository.save(avion);

            return new ResponseEntity<String>("successfully added", HttpStatus.ACCEPTED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteAirplane(@RequestHeader(value = HEADER_STRING) String token, @PathVariable long id) {

        try {

            if (token.isEmpty()) {
                System.out.println("parazan token");
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            ResponseEntity<Boolean> res = UtilsMethods.checkAuthorization("http://localhost:8762/rest-user-service/admin/checkAdmin", token);
            if (res.getBody() == null) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            if(flightRepository.existsByAvion_Id(id)){
                System.out.println("Postoji");
                return new ResponseEntity<>("Dodeljen letu", HttpStatus.BAD_REQUEST);
            }

            airplaneRepository.deleteById(id);

            return new ResponseEntity<>("successfully deleted", HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<Airplane>> getAirplanes() {

        try {
            List<Airplane> airplaneList = airplaneRepository.findAll();

            return new ResponseEntity<List<Airplane>>(airplaneList, HttpStatus.ACCEPTED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


}

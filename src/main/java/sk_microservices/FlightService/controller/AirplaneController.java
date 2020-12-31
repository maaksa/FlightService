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

    //todo bez GUI
    //dodavanje novoga aviona, poziva se iz admin controllera
//    @PostMapping("/save")
//    public ResponseEntity<String> addAirplane(@RequestHeader(value = HEADER_STRING) String token, @RequestBody AddAirplaneForm addAirplaneForm) {
//
//        try {
//
//            if (token.isEmpty()) {
//                System.out.println("parazan token");
//                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
//            }
//
//            ResponseEntity<Boolean> res = UtilsMethods.checkAuthorization("http://localhost:8080/admin/checkAdmin", token);
//            if (res.getBody() == null) {
//                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
//            }
//
//            Airplane avion = new Airplane(addAirplaneForm.getNaziv(), addAirplaneForm.getKapacitetPutnika());
//            airplaneRepository.save(avion);
//
//            return new ResponseEntity<String>("successfully added", HttpStatus.ACCEPTED);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//        }
//    }

    //todo bez GUI
//    @DeleteMapping("/delete/{id}")
//    public ResponseEntity<String> deleteAirplane(@RequestHeader(value = HEADER_STRING) String token, @PathVariable long id) {
//
//        try {
//
//            if (token.isEmpty()) {
//                System.out.println("parazan token");
//                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
//            }
//
//            ResponseEntity<Boolean> res = UtilsMethods.checkAuthorization("http://localhost:8080/admin/checkAdmin", token);
//            if (res.getBody() == null) {
//                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
//            }
//
//            if(flightRepository.existsByAvion_Id(id)){
//                System.out.println("Postoji");
//                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
//            }
//
//            airplaneRepository.deleteById(id);
//
//            return new ResponseEntity<>("successfully deleted", HttpStatus.ACCEPTED);
//        } catch (Exception e) {
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//        }
//    }

//    //todo za GUI
//    @GetMapping("/showFormForAdd")
//    public String showFormForAdd(Model theModel) {
//        try {
//            Airplane theAirplane = new Airplane();
//
//            theModel.addAttribute("airplane", theAirplane);
//
//            return "airplanes/airplane-form";
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "redirect:/error";
//        }
//    }


    //todo za GUI
    @GetMapping("/showFormForAdd")
    public String showFormForAdd(Model theModel) {
        try {

            theModel.addAttribute("airplane", new AddAirplaneForm());

            return "airplanes/airplane-form";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/error";
        }
    }

    //todo za GUI
    @PostMapping("/save")
    public String saveAirplane(@ModelAttribute("airplane") Airplane theAirplane) {
        try {
            airplaneRepository.save(theAirplane);

            return "redirect:/airplane/list";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/error";
        }
    }

    //todo za GUI
    @GetMapping("/delete")
    public String delete(@RequestParam("airplaneId") long theId) {
        try {
            if (flightRepository.existsByAvion_Id(theId)) {
                System.out.println("Postoji");
                return "redirect:/error";
                //return "redirect:/error-delete-airplane";
            }

            airplaneRepository.deleteById(theId);

            return "redirect:/airplane/list";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/error";
        }
    }

    //todo za GUI
    @GetMapping("/list")
    public String getFlights(Model theModel) {
        try {
            List<Airplane> theAirplanes = airplaneRepository.findAll();

            theModel.addAttribute("airplanes", theAirplanes);

            return "airplanes/list-airplanes";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/error";
        }
    }

    //todo bez GUI
//    @GetMapping("/list")
//    public ResponseEntity<List<Airplane>> getFlights() {
//
//        try {
//            List<Airplane> airplaneList = airplaneRepository.findAll();
//
//            return new ResponseEntity<List<Airplane>>(airplaneList, HttpStatus.ACCEPTED);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//        }
//    }


}

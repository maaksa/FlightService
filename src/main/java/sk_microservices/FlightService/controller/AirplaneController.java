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

    @GetMapping("/showFormForAdd")
    public String showFormForAdd(Model theModel) {

        try {
            Airplane theAirplane = new Airplane();

            theModel.addAttribute("airplane", theAirplane);

            return "airplanes/airplane-form";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @PostMapping("/save")
    public String saveAirplane(@ModelAttribute("airplane") Airplane theAirplane) {

        airplaneRepository.save(theAirplane);

        return "redirect:/airplane/list";
    }

    @GetMapping("/list")
    public String getFlights(Model theModel) {

        try {
            List<Airplane> theAirplanes = airplaneRepository.findAll();

            theModel.addAttribute("airplanes", theAirplanes);

            return "airplanes/list-airplanes";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/delete")
    public String delete(@RequestParam("airplaneId") long theId) {

        airplaneRepository.deleteById(theId);

        return "redirect:/airplane/list";
    }

}

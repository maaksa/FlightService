package sk_microservices.FlightService.forms;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FlightForm {
    private long id;
    private String pocetnaDestinacija;
    private String krajnjaDestinacija;
    private int duzinaLeta;
    private float cena;
    private AddAirplaneForm avion;
}

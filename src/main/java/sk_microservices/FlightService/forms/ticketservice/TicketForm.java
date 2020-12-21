package sk_microservices.FlightService.forms.ticketservice;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TicketForm {

    private Date datumKupovine;
    private long user_id;
    private long flight_id;

}


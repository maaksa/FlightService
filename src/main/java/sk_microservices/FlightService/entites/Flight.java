package sk_microservices.FlightService.entites;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    private Airplane avion;

    private String pocetnaDestinacija;

    private String krajnjaDestinacija;

    private int duzinaLeta;

    private float cena;

    public Flight(Airplane avion, String pocetnaDestinacija, String krajnjaDestinacija, int duzinaLeta, float cena) {
        this.avion = avion;
        this.pocetnaDestinacija = pocetnaDestinacija;
        this.krajnjaDestinacija = krajnjaDestinacija;
        this.duzinaLeta = duzinaLeta;
        this.cena = cena;
    }

}

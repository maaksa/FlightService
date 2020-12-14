package sk_microservices.FlightService.entites;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
public class Airplane {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String naziv;

    private int kapacitetPutnika;

    public Airplane(String naziv, int kapacitetPutnika) {
        this.naziv = naziv;
        this.kapacitetPutnika = kapacitetPutnika;
    }
}

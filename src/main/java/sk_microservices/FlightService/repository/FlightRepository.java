package sk_microservices.FlightService.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import sk_microservices.FlightService.entites.Flight;

import java.util.Optional;

@RepositoryRestResource
@Repository
public interface FlightRepository extends JpaRepository<Flight, Long>, JpaSpecificationExecutor<Flight> {

    boolean existsByAvion_Id(long id);

    @Query("select f.avion.kapacitetPutnika from Flight f where f.id = :id")
    int getCapacityForFlight(long id);

    @Query("select f.duzinaLeta from Flight f where f.id = :id")
    int getLengthForFlight(long id);

    @Query("select f from Flight f where f.avion.kapacitetPutnika > 0")
    Page<Flight> findAllWithCapacity(Pageable pageable);

    @Override
    @Query("select f from Flight f where f.id = :id")
    Optional<Flight> findById(Long id);

}

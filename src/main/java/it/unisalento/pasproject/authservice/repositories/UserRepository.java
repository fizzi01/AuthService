package it.unisalento.pasproject.authservice.repositories;


import it.unisalento.pasproject.authservice.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

//Il repository è un'interfaccia che estende MongoRepository, che è un'interfaccia che ci permette di fare operazioni CRUD
public interface UserRepository extends MongoRepository<User, String>{

    //Conta il nome dei metodi, Se il campo si chiama Surname bisogna rispettare la nomenclatura del domain
    List<User> findBySurname(String surname);

    List<User> findByNameAndSurname(String name, String surname);

    User findByEmail(String email);
}

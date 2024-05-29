package it.unisalento.pasproject.authservice.repositories;

import it.unisalento.pasproject.authservice.domain.CredentialsRestore;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CredentialsRestoreRepository extends MongoRepository<CredentialsRestore, String> {
    Optional<CredentialsRestore> findByToken(String token);
}

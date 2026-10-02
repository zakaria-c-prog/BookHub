package edu.njust.bookhub.dao;

import edu.njust.bookhub.model.Provider;

import java.util.List;
import java.util.Optional;

/** Data access for the {@code providers} table. */
public interface ProviderDao {

    /** All providers, each with the number of books it supplies. */
    List<Provider> findAll();

    Optional<Provider> findById(int providerId);

    boolean nameTaken(String companyName, Integer ignoreProviderId);

    int count();

    int insert(Provider provider);

    void update(Provider provider);

    boolean delete(int providerId);
}

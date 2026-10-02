package edu.njust.bookhub.service;

import edu.njust.bookhub.dao.ProviderDao;
import edu.njust.bookhub.model.Provider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProviderService {

    private final ProviderDao providerDao;

    public ProviderService(ProviderDao providerDao) {
        this.providerDao = providerDao;
    }

    public List<Provider> list() {
        return providerDao.findAll();
    }

    public Provider get(int providerId) {
        return providerDao.findById(providerId).orElseThrow(() -> new NotFoundException("Provider", providerId));
    }

    @Transactional
    public int create(Provider provider) {
        checkNameFree(provider.getCompanyName(), null);
        return providerDao.insert(provider);
    }

    @Transactional
    public void update(Provider provider) {
        get(provider.getProviderId());
        checkNameFree(provider.getCompanyName(), provider.getProviderId());
        providerDao.update(provider);
    }

    /** Removes the provider; its books remain but no longer have a provider. */
    @Transactional
    public void delete(int providerId) {
        if (!providerDao.delete(providerId)) {
            throw new NotFoundException("Provider", providerId);
        }
    }

    private void checkNameFree(String name, Integer ignoreId) {
        if (providerDao.nameTaken(name, ignoreId)) {
            throw new BusinessRuleException("companyName", "A provider called \"" + name.trim() + "\" already exists.");
        }
    }
}

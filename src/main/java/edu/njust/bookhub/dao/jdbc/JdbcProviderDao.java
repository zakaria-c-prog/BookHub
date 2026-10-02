package edu.njust.bookhub.dao.jdbc;

import edu.njust.bookhub.dao.ProviderDao;
import edu.njust.bookhub.model.Provider;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbcProviderDao implements ProviderDao {

    private static final RowMapper<Provider> PROVIDER_MAPPER = (rs, rowNum) -> {
        Provider p = new Provider();
        p.setProviderId(rs.getInt("provider_id"));
        p.setCompanyName(rs.getString("company_name"));
        p.setPhone(rs.getString("phone"));
        p.setEmail(rs.getString("email"));
        p.setCity(rs.getString("city"));
        return p;
    };

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcProviderDao(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Provider> findAll() {
        return jdbc.getJdbcTemplate().query("""
                SELECT p.*, COUNT(b.book_id) AS book_count
                FROM providers p
                LEFT JOIN books b ON b.provider_id = p.provider_id
                GROUP BY p.provider_id, p.company_name, p.phone, p.email, p.city
                ORDER BY p.company_name""", (rs, i) -> {
            Provider p = PROVIDER_MAPPER.mapRow(rs, i);
            p.setBookCount(rs.getInt("book_count"));
            return p;
        });
    }

    @Override
    public Optional<Provider> findById(int providerId) {
        return jdbc.query("SELECT * FROM providers WHERE provider_id = :id",
                new MapSqlParameterSource("id", providerId), PROVIDER_MAPPER).stream().findFirst();
    }

    @Override
    public boolean nameTaken(String companyName, Integer ignoreProviderId) {
        Integer n = jdbc.queryForObject("""
                        SELECT COUNT(*) FROM providers
                        WHERE LOWER(company_name) = LOWER(:name) AND provider_id <> :ignore""",
                new MapSqlParameterSource("name", companyName.trim())
                        .addValue("ignore", ignoreProviderId == null ? -1 : ignoreProviderId),
                Integer.class);
        return n != null && n > 0;
    }

    @Override
    public int count() {
        Integer n = jdbc.getJdbcTemplate().queryForObject("SELECT COUNT(*) FROM providers", Integer.class);
        return n == null ? 0 : n;
    }

    @Override
    public int insert(Provider provider) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update("""
                        INSERT INTO providers (company_name, phone, email, city)
                        VALUES (:companyName, :phone, :email, :city)""",
                params(provider), keys, new String[]{"provider_id"});
        int id = keys.getKey().intValue();
        provider.setProviderId(id);
        return id;
    }

    @Override
    public void update(Provider provider) {
        jdbc.update("""
                        UPDATE providers SET company_name = :companyName, phone = :phone,
                               email = :email, city = :city
                        WHERE provider_id = :id""",
                params(provider).addValue("id", provider.getProviderId()));
    }

    @Override
    public boolean delete(int providerId) {
        // Books keep existing; their provider_id becomes NULL (ON DELETE SET NULL)
        return jdbc.update("DELETE FROM providers WHERE provider_id = :id",
                new MapSqlParameterSource("id", providerId)) == 1;
    }

    private static MapSqlParameterSource params(Provider p) {
        return new MapSqlParameterSource()
                .addValue("companyName", p.getCompanyName())
                .addValue("phone", p.getPhone())
                .addValue("email", p.getEmail())
                .addValue("city", p.getCity());
    }
}

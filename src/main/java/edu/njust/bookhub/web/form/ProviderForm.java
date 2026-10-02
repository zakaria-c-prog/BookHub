package edu.njust.bookhub.web.form;

import edu.njust.bookhub.model.Provider;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import static edu.njust.bookhub.web.form.AuthorForm.blankToNull;

public class ProviderForm {

    @NotBlank(message = "Company name is required")
    @Size(max = 150)
    private String companyName;

    @Pattern(regexp = "^$|[0-9+()\\-\\s]{5,40}", message = "Phone may contain digits, spaces, +, - and brackets")
    private String phone;

    @Email(message = "Not a valid e-mail address")
    @Size(max = 120)
    private String email;

    @Size(max = 80)
    private String city;

    public static ProviderForm from(Provider p) {
        ProviderForm f = new ProviderForm();
        f.companyName = p.getCompanyName();
        f.phone = p.getPhone();
        f.email = p.getEmail();
        f.city = p.getCity();
        return f;
    }

    public Provider toProvider(Integer providerId) {
        Provider p = new Provider();
        p.setProviderId(providerId);
        p.setCompanyName(companyName.trim());
        p.setPhone(blankToNull(phone));
        p.setEmail(blankToNull(email));
        p.setCity(blankToNull(city));
        return p;
    }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
}

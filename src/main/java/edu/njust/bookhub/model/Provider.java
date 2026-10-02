package edu.njust.bookhub.model;

/** A company that supplies books to the store (row of table {@code providers}). */
public class Provider {

    private Integer providerId;
    private String companyName;
    private String phone;
    private String email;
    private String city;

    /** Number of books supplied; filled only by summary queries. */
    private int bookCount;

    public Integer getProviderId() { return providerId; }
    public void setProviderId(Integer providerId) { this.providerId = providerId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public int getBookCount() { return bookCount; }
    public void setBookCount(int bookCount) { this.bookCount = bookCount; }
}

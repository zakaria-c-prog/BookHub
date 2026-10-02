package edu.njust.bookhub.web.form;

import edu.njust.bookhub.model.Author;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthorForm {

    @NotBlank(message = "Name is required")
    @Size(max = 120)
    private String fullName;

    @Size(max = 60)
    private String nationality;

    @Email(message = "Not a valid e-mail address")
    @Size(max = 120)
    private String email;

    @Size(max = 1500, message = "Biography must be at most 1500 characters")
    private String biography;

    public static AuthorForm from(Author a) {
        AuthorForm f = new AuthorForm();
        f.fullName = a.getFullName();
        f.nationality = a.getNationality();
        f.email = a.getEmail();
        f.biography = a.getBiography();
        return f;
    }

    public Author toAuthor(Integer authorId) {
        Author a = new Author();
        a.setAuthorId(authorId);
        a.setFullName(fullName.trim());
        a.setNationality(blankToNull(nationality));
        a.setEmail(blankToNull(email));
        a.setBiography(blankToNull(biography));
        return a;
    }

    static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBiography() { return biography; }
    public void setBiography(String biography) { this.biography = biography; }
}

package edu.njust.bookhub.model;

/** A writer of one or more books (row of table {@code authors}). */
public class Author {

    private Integer authorId;
    private String fullName;
    private String nationality;
    private String email;
    private String biography;

    /** Number of books written; filled only by summary queries. */
    private int bookCount;

    public Integer getAuthorId() { return authorId; }
    public void setAuthorId(Integer authorId) { this.authorId = authorId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBiography() { return biography; }
    public void setBiography(String biography) { this.biography = biography; }

    public int getBookCount() { return bookCount; }
    public void setBookCount(int bookCount) { this.bookCount = bookCount; }

    /** Initials shown in the avatar badge, e.g. "Grace Hopkins" -> "GH". */
    public String getInitials() {
        if (fullName == null || fullName.isBlank()) {
            return "?";
        }
        StringBuilder sb = new StringBuilder();
        for (String part : fullName.trim().split("\\s+")) {
            sb.append(Character.toUpperCase(part.charAt(0)));
            if (sb.length() == 2) {
                break;
            }
        }
        return sb.toString();
    }
}

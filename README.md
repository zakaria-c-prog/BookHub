# BookHub — Online Bookstore

Software Design course project (NJUST). BookHub is a web application for
managing the catalogue of an online bookstore: its **books**, their
**authors** and the **providers** (suppliers) who deliver them.

| Layer        | Technology                                                        |
|--------------|-------------------------------------------------------------------|
| Presentation | Spring MVC controllers + Thymeleaf server-rendered pages, Bootstrap 5 |
| Business     | Service classes with transactions and catalogue rules            |
| Data access  | DAO interfaces + Spring `JdbcTemplate` implementations, hand-written SQL |
| Database     | MySQL 8 (H2 in-memory for the demo profile and tests)            |
| Deployment   | WAR file for Apache Tomcat 10.1+ (also runs stand-alone)          |

## Architecture

```
Browser ──HTTP──▶ Controller (web)  ──▶ Service (business rules) ──▶ DAO (JDBC/SQL) ──▶ MySQL
          ◀─HTML── Thymeleaf template ◀── model attributes
```

```
src/main/java/edu/njust/bookhub
├── BookHubApplication.java      entry point (+ SpringBootServletInitializer for Tomcat)
├── model/                       Book, Author, Provider, BookSearchCriteria, CatalogStats
├── dao/                         BookDao, AuthorDao, ProviderDao  (interfaces)
│   └── jdbc/                    JdbcBookDao, JdbcAuthorDao, JdbcProviderDao
├── service/                     CatalogService, AuthorService, ProviderService + exceptions
└── web/                         HomeController, BookController, AuthorController,
    └── form/                    ProviderController, ErrorAdvice; BookForm, AuthorForm, ProviderForm
src/main/resources
├── schema.sql / data.sql        DDL and sample data (run at start-up, idempotent)
├── templates/                   Thymeleaf pages (layout fragments, books/, authors/, providers/)
└── static/                      CSS, JS, favicon
```

Design decisions:

- **DAO pattern.** Each table group has an interface and a JDBC implementation,
  so services never see SQL and the storage could be swapped.
- **Query Object.** `BookSearchCriteria` holds every optional filter and the
  DAO builds the `WHERE` clause from whichever fields are set, so any
  combination of filters works in one query. Values are always bound as
  parameters; the sort column comes from a fixed enum.
- **No N+1 queries.** Book lists load their authors with one extra
  `... WHERE book_id IN (...)` query instead of one query per book.
- **Rules in the service layer.** Duplicate ISBN / provider name, not selling
  more copies than in stock (checked atomically in SQL), discount limits.
- **Post/Redirect/Get.** Every update redirects afterwards and shows a flash
  message, so refreshing never re-submits a form. HTML forms send real
  `PUT` / `DELETE` requests through Spring's hidden `_method` field.

## Database schema

```
providers (provider_id PK, company_name UNIQUE, phone, email, city)
authors   (author_id PK, full_name, nationality, email, biography)
books     (book_id PK, title, isbn UNIQUE, category, price ≥ 0, stock ≥ 0,
           publish_year, summary, provider_id FK → providers ON DELETE SET NULL,
           created_at)
book_authors (book_id FK → books ON DELETE CASCADE,
              author_id FK → authors ON DELETE CASCADE,
              author_order, PK(book_id, author_id))
```

- provider 1 ── * books: a book has at most one provider; deleting a provider
  keeps its books but sets `provider_id` to NULL.
- books * ── * authors via `book_authors`; `author_order` keeps the byline order.

The full DDL is in [`schema.sql`](src/main/resources/schema.sql). The sample
catalogue in [`data.sql`](src/main/resources/data.sql) has 3 providers,
6 authors and 12 books.

## Functions

**Lookups**

| # | Function | Where |
|---|----------|-------|
| L1 | Search books by title or ISBN keyword | Catalogue → *Title or ISBN* |
| L2 | Search books by author name | Catalogue → *Author* |
| L3 | Filter books by provider | Catalogue → *Provider*, or a provider's page |
| L4 | Filter by category and price range, in-stock only, 5 sort orders | Catalogue sidebar |
| L5 | Book details (authors, provider, ISBN, year, stock) | `/books/{id}` |
| L6 | Author profile with all their books and co-authors | `/authors/{id}` |
| L7 | Provider page with its books and total stock value | `/providers/{id}` |
| L8 | Low-stock report (≤ 5 copies) | `/books/low-stock`, dashboard |
| L9 | Dashboard statistics (counts, inventory value, titles per category) | `/` |

**Updates**

| # | Function | Where |
|---|----------|-------|
| U1 | Add a book (with validation, authors, provider) | *New book* |
| U2 | Edit a book | Book page → *Edit details* |
| U3 | Delete a book | Book page → *Delete* |
| U4 | Receive stock / record a sale | Book page → *Stock movement* |
| U5 | Set a new price or apply a % discount | Book page → *Pricing* |
| U6 | Add / edit / delete authors | Authors pages |
| U7 | Add / edit / delete providers | Providers pages |

## Running

Requires JDK 17 or newer. Maven does not need to be installed: `mvnw` /
`mvnw.cmd` downloads it into `.mvn/dist` on first use.

### Quick demo (no MySQL needed)

```bash
./mvnw clean package
java -jar target/bookhub.war --spring.profiles.active=demo
```

Open <http://localhost:8081>. Data lives in memory and resets on restart.

### With MySQL

The app creates the `bookhub_db` database and its tables automatically.
Credentials are read from environment variables:

```powershell
$env:BOOKHUB_DB_USER = "root"
$env:BOOKHUB_DB_PASS = "your-mysql-password"
java -jar target/bookhub.war
```

(bash: `BOOKHUB_DB_PASS=your-mysql-password java -jar target/bookhub.war`)

### Deploying to Apache Tomcat

1. Build: `./mvnw clean package` → `target/bookhub.war`
2. Set `BOOKHUB_DB_USER` / `BOOKHUB_DB_PASS` for the Tomcat process
   (e.g. in `bin/setenv.bat`: `set BOOKHUB_DB_PASS=...`).
3. Copy `bookhub.war` into Tomcat's `webapps/` folder and start Tomcat
   (Tomcat 10.1+ is required for Jakarta EE / Spring Boot 3).
4. Open <http://localhost:8080/bookhub/>.

### Tests

```bash
./mvnw test
```

`CatalogServiceTest` runs the service and DAO layers against the H2 demo
database. It covers the combined search, author lookup, duplicate ISBN,
author ordering, overselling, discounts and provider deletion.

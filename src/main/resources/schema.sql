-- BookHub database schema.
-- Executed on every start-up (spring.sql.init.mode=always); every statement
-- is idempotent so existing data is never touched.

CREATE TABLE IF NOT EXISTS providers (
    provider_id   INT          NOT NULL AUTO_INCREMENT,
    company_name  VARCHAR(150) NOT NULL,
    phone         VARCHAR(40),
    email         VARCHAR(120),
    city          VARCHAR(80),
    PRIMARY KEY (provider_id),
    CONSTRAINT uq_provider_name UNIQUE (company_name)
);

CREATE TABLE IF NOT EXISTS authors (
    author_id     INT          NOT NULL AUTO_INCREMENT,
    full_name     VARCHAR(120) NOT NULL,
    nationality   VARCHAR(60),
    email         VARCHAR(120),
    biography     VARCHAR(1500),
    PRIMARY KEY (author_id)
);

CREATE TABLE IF NOT EXISTS books (
    book_id       INT           NOT NULL AUTO_INCREMENT,
    title         VARCHAR(200)  NOT NULL,
    isbn          VARCHAR(20)   NOT NULL,
    category      VARCHAR(60)   NOT NULL,
    price         DECIMAL(8,2)  NOT NULL,
    stock         INT           NOT NULL DEFAULT 0,
    publish_year  INT,
    summary       VARCHAR(2000),
    provider_id   INT,
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (book_id),
    CONSTRAINT uq_book_isbn UNIQUE (isbn),
    CONSTRAINT ck_book_price CHECK (price >= 0),
    CONSTRAINT ck_book_stock CHECK (stock >= 0),
    CONSTRAINT fk_book_provider FOREIGN KEY (provider_id)
        REFERENCES providers (provider_id) ON DELETE SET NULL
);

-- Many-to-many between books and authors; author_order keeps the byline order
CREATE TABLE IF NOT EXISTS book_authors (
    book_id       INT NOT NULL,
    author_id     INT NOT NULL,
    author_order  INT NOT NULL DEFAULT 1,
    PRIMARY KEY (book_id, author_id),
    CONSTRAINT fk_ba_book   FOREIGN KEY (book_id)   REFERENCES books (book_id)     ON DELETE CASCADE,
    CONSTRAINT fk_ba_author FOREIGN KEY (author_id) REFERENCES authors (author_id) ON DELETE CASCADE
);

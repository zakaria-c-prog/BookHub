package edu.njust.bookhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Entry point. Extending {@link SpringBootServletInitializer} lets the same
 * build be dropped into Tomcat's webapps/ folder as a WAR, while
 * {@link #main} still allows running it stand-alone.
 */
@SpringBootApplication
public class BookHubApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(BookHubApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(BookHubApplication.class, args);
    }
}

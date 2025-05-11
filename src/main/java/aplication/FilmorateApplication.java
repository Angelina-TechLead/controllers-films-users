package aplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@ComponentScan(basePackages = {"aplication.controller",
                               "aplication.storage",
                               "aplication.service",
                                "aplication.validators",
                                "aplication.model",
                                "aplication.exception"})
public class FilmorateApplication {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure()
            .directory(System.getProperty("user.dir"))
            .load();

        System.setProperty("spring.datasource.url", dotenv.get("APP_DB_URL"));
        System.setProperty("spring.datasource.username", dotenv.get("PG_DB_USERNAME"));
        System.setProperty("spring.datasource.password", dotenv.get("PG_DB_PASSWORD"));

        SpringApplication.run(FilmorateApplication.class, args);
    }
}

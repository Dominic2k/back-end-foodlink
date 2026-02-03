package org.datpham.foodlink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class FoodlinkApplication {

    // TODO: Rename the application class and base package for your new project.
    public static void main(String[] args) {
        SpringApplication.run(FoodlinkApplication.class, args);
    }

    @Bean
    public CommandLineRunner testPassword(PasswordEncoder encoder) {
        return args -> {
            System.out.println("Password hash: " + encoder.encode("123456"));
        };
    }
}

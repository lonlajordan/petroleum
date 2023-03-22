package com.petroleum;

import com.petroleum.models.Product;
import com.petroleum.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;


@SpringBootApplication
public class PetroleumApplication extends SpringBootServletInitializer implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    public static void main(String[] args) {
        SpringApplication.run(PetroleumApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(PetroleumApplication.class);
    }

    @Override
    public void run(String... args) {
        if(productRepository.count() == 0){
            productRepository.save(new Product("SUPER"));
            productRepository.save(new Product("GAZOIL"));
        }
    }
}

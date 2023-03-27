package com.petroleum;

import com.petroleum.models.Depot;
import com.petroleum.models.Product;
import com.petroleum.repositories.DepotRepository;
import com.petroleum.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.util.Arrays;
import java.util.List;


@SpringBootApplication
public class PetroleumApplication extends SpringBootServletInitializer implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private DepotRepository depotRepository;

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
            Product product = new Product("SUPER");
            product.setPassage(9.5);
            product.setPassageTax(1.83);
            product.setRefinery(47.88);
            product.setSpecialTax(110);
            product.setTransport(46);
            product.setMarking(1.66);
            product.setMarkingTax(0.32);
            productRepository.save(product);

            product = new Product("PETROLE");
            product.setPassage(8);
            product.setPassageTax(1.54);
            product.setRefinery(47.88);
            product.setSpecialTax(0);
            product.setTransport(31);
            product.setMarking(1.66);
            product.setMarkingTax(0.32);
            productRepository.save(product);

            product = new Product("GAZOIL");
            product.setPassage(9.5);
            product.setPassageTax(1.83);
            product.setRefinery(47.88);
            product.setSpecialTax(65);
            product.setTransport(46);
            product.setMarking(1.66);
            product.setMarkingTax(0.32);
            productRepository.save(product);
        }

        if(depotRepository.count() == 0){
            List<Depot> depots = Arrays.asList(
              new Depot("DOUALA"),
              new Depot("YAOUNDE"),
              new Depot("BAFOUSSAM"),
              new Depot("GAROUA"),
              new Depot("BELABO"),
              new Depot("LIMBE")
            );
            depotRepository.saveAll(depots);
        }
    }
}

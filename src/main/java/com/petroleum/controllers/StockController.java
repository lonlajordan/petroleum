package com.petroleum.controllers;

import com.petroleum.models.Depot;
import com.petroleum.models.Product;
import com.petroleum.models.Stock;
import com.petroleum.repositories.DepotRepository;
import com.petroleum.repositories.ProductRepository;
import com.petroleum.repositories.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/stocks")
public class StockController {
    private final DepotRepository depotRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;

    @GetMapping
    public String getAll(Model model){
        List<Depot> depots = depotRepository.findAllByOrderByNameAsc();
        List<Product> products = productRepository.findAllByOrderByNameAsc();
        ArrayList<List<Double>> volumes = new ArrayList<>();
        for(Depot depot: depots){
            volumes.add(products.stream().map(product -> stockRepository.findFirstByDepotAndProduct(depot, product).orElse(new Stock()).getVolume()).collect(Collectors.toList()));
        }
        model.addAttribute("depots", depots);
        model.addAttribute("products", products);
        model.addAttribute("volumes", volumes);
        return "stocks";
    }
}

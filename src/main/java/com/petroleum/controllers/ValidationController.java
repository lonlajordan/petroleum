package com.petroleum.controllers;

import com.petroleum.models.Fuel;
import com.petroleum.repositories.FuelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ValidationController {
    private final FuelRepository fuelRepository;

    @GetMapping("/validate")
    public String validation(@RequestParam String code, @RequestParam int number, Model model) {
        Fuel fuel = fuelRepository.findByCodeAndNumber(code, number);
        model.addAttribute("fuel", fuel);
        return  "validation" ;
    }
}

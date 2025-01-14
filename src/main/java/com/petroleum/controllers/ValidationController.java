package com.petroleum.controllers;

import com.petroleum.enums.Role;
import com.petroleum.models.*;
import com.petroleum.repositories.FuelRepository;
import com.petroleum.repositories.ProductRepository;
import com.petroleum.repositories.StationRepository;
import com.petroleum.repositories.UserRepository;
import com.petroleum.services.EmailHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ValidationController {
    private final UserRepository userRepository;
    private final FuelRepository fuelRepository;
    private final StationRepository stationRepository;
    private final ProductRepository productRepository;

    @GetMapping("/validate")
    public String findFuelByCodeAndNumber(@RequestParam String code, @RequestParam int number, Model model) {
        Fuel fuel = fuelRepository.findByCodeAndNumber(code, number);
        List<Station> stations = stationRepository.findAllByOrderByNameAsc();
        List<Product> products = productRepository.findAllByOrderByNameAsc();
        model.addAttribute("fuel", fuel);
        model.addAttribute("stations", stations);
        model.addAttribute("products", products);
        model.addAttribute("number", number);
        return  "validation" ;
    }

    @PostMapping("/validate")
    public String validateFuel(@NonNull Validation validation, Model model) {
        Fuel fuel = fuelRepository.findById(validation.getFuelId()).orElse(null);
        Station station = stationRepository.findById(validation.getStationId()).orElse(null);
        Product product = productRepository.findById(validation.getProductId()).orElse(null);
        Notification notification = new Notification("error", "Une erreur est survenue lors de cette opération.");
        if(fuel != null && station != null && product != null) {
            if(station.getCode().equals(validation.getCode())) {
                fuel.setStation(station);
                fuel.setProduct(product);
                fuel.setMatriculation(validation.getMatriculation().trim());
                fuel.setEnabled(false);
                fuel = fuelRepository.save(fuel);
                notification.setType("success");
                notification.setMessage("Opération terminée avec succès.");
                String receiver = userRepository.findAll().stream().filter(user -> !Role.ROLE_DISPATCHER.equals(user.getRole())).map(User::getEmail).collect(Collectors.joining(","));
                String body = "<div style='line-height: 1.6'>Bonjour Mr/Mme,<br>" +
                        "Un bon de carburant a été validé.<br>" +
                        "<ul>" +
                        "<li><b>Numéro</b> : " + fuel.getNumber() + "</li>" +
                        "<li><b>Montant</b> : " + fuel.getAmount() + " FCFA</li>" +
                        "<li><b>Produit</b> : " + product.getName() + "</li>" +
                        "<li><b>Station</b> : " + station.getName() + "</li>" +
                        "<li><b>Immatriculation du véhicule</b> : " + validation.getMatriculation() + "</li>" +
                        "</ul>" +
                        "Cordialement.</div>";
                EmailHelper.sendMail(receiver, "", "Validation bon de carburant", body);
            } else {
                notification.setMessage("Code de validation invalide");
            }
        }
        List<Station> stations = stationRepository.findAllByOrderByNameAsc();
        List<Product> products = productRepository.findAllByOrderByNameAsc();
        model.addAttribute("fuel", fuel);
        model.addAttribute("stations", stations);
        model.addAttribute("products", products);
        model.addAttribute("notification", notification);

        model.addAttribute("fuelId", validation.getFuelId());
        model.addAttribute("stationId", validation.getStationId());
        model.addAttribute("productId", validation.getProductId());
        model.addAttribute("matriculation", validation.getMatriculation());
        model.addAttribute("code", validation.getCode());
        model.addAttribute("number", Optional.ofNullable(fuel).map(Fuel::getNumber).orElse(null));
        return  "validation" ;
    }
}

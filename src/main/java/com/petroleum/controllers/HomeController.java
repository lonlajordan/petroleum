package com.petroleum.controllers;

import com.petroleum.enums.Role;
import com.petroleum.models.Invoice;
import com.petroleum.models.User;
import com.petroleum.repositories.DepotRepository;
import com.petroleum.repositories.InvoiceRepository;
import com.petroleum.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final DepotRepository depotRepository;
    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;

    @GetMapping("/home")
    public String getInvoices(@RequestParam(required = false, defaultValue = "1") int p, Model model, HttpSession session){
        Pageable pageable = PageRequest.of(p  - 1, 1000);
        Page<Invoice> invoices = invoiceRepository.findAllByOrderByDateDesc(pageable);
        model.addAttribute("invoices", invoices.get().collect(Collectors.toList()));
        model.addAttribute("totalPages", invoices.getTotalPages());
        model.addAttribute("currentPage", p);
        model.addAttribute("filtered", false);
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("depots", depotRepository.findAll());
        User user = (User) session.getAttribute("user");
        model.addAttribute("isDirector", Role.ROLE_DIRECTOR.equals(user.getRole()));
        return "home";
    }
}

package com.petroleum.controllers;

import com.petroleum.mappers.SupplyMapper;
import com.petroleum.models.*;
import com.petroleum.repositories.DepotRepository;
import com.petroleum.repositories.ProductRepository;
import com.petroleum.repositories.StockRepository;
import com.petroleum.repositories.SupplyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/supplies")
public class SupplyController {

    private final DepotRepository depotRepository;
    private final SupplyRepository supplyRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;

    private final SupplyMapper supplyMapper;

    @PersistenceContext
    private EntityManager em;

    @GetMapping
    public String getSupplies(@RequestParam(required = false, defaultValue = "1") int p, @RequestParam(required = false) Long productId, Model model){
        Pageable pageable = PageRequest.of(p  - 1, 1000);
        Page<Supply> supplies = productId == null ? supplyRepository.findAllByOrderByDateDesc(pageable) : supplyRepository.findAllByProductIdOrderByDateDesc(productId, pageable);
        model.addAttribute("supplies", supplies.get().collect(Collectors.toList()));
        model.addAttribute("totalPages", supplies.getTotalPages());
        model.addAttribute("currentPage", p);
        model.addAttribute("products", productRepository.findAllByOrderByNameAsc());
        model.addAttribute("depots", depotRepository.findAllByOrderByNameAsc());
        return "supplies";
    }

    @PostMapping
    public String save(@NonNull Supply form, @RequestParam long depotId, @RequestParam long productId, RedirectAttributes attributes){
        Supply supply = form;
        if(supply.getId() != null){
            supply = supplyRepository.findById(supply.getId()).orElse(form);
            supplyMapper.update(supply, form);
        }
        supply.setDepot(em.getReference(Depot.class, depotId));
        supply.setProduct(em.getReference(Product.class, productId));
        Notification notification = new Notification();
        try {
            supply = supplyRepository.save(supply);
            Stock stock = stockRepository.findFirstByDepotAndProduct(supply.getDepot(), supply.getProduct()).orElse(new Stock(supply.getDepot(), supply.getProduct()));
            stock.setVolume(stock.getVolume() + supply.getVolume());
            stock.setUpdatedAt(LocalDateTime.now());
            stockRepository.save(stock);
            notification.setType("success");
            notification.setMessage("L'approvisionnement a été enregistré.");
        } catch (Exception e){
            log.error("Error while saving supply", e);
            notification.setType("error");
            notification.setMessage("Erreur lors de l'enregistrement de l'approvisionnement.");
        }

        attributes.addFlashAttribute("notification", notification);
        return "redirect:/supplies";
    }

    @PostMapping(value="delete")
    public String delete(@RequestParam ArrayList<Long> ids, RedirectAttributes attributes){
        try {
            supplyRepository.deleteAllById(ids);
            attributes.addFlashAttribute("notification", new Notification("success", "Opération terminée avec succès."));
        }catch (Exception e){
            attributes.addFlashAttribute("notification", new Notification("error", "Une erreur est survenue lors de cette opération."));
        }
        return "redirect:/supplies";
    }
}

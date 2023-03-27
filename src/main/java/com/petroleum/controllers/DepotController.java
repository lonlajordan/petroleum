package com.petroleum.controllers;

import com.petroleum.mappers.DepotMapper;
import com.petroleum.models.Depot;
import com.petroleum.models.Notification;
import com.petroleum.repositories.DepotRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/depots")
public class DepotController {
    private final DepotRepository depotRepository;
    private final DepotMapper depotMapper;

    @GetMapping
    public String getAll(Model model){
        List<Depot> depots = depotRepository.findAllByOrderByNameAsc();
        model.addAttribute("depots", depots);
        return "depots";
    }

    @PostMapping
    public String save(@NonNull Depot depotDto, RedirectAttributes attributes){
        Depot depot = depotDto;
        if(depotDto.getId() != null){
            depot = depotRepository.findById(depot.getId()).orElse(depotDto);
            depotMapper.update(depot, depotDto);
        }
        depot.normalize();
        Notification notification = new Notification();
        try {
            depot = depotRepository.save(depot);
            notification.setType("success");
            notification.setMessage("<b>" + depot.getName() +"</b> a été enregistré.");
        } catch (Exception e){
            e.printStackTrace();
            notification.setType("error");
            if(ExceptionUtils.getStackTrace(e).toLowerCase().contains("duplicate entry")){
                notification.setMessage("Le dépôt <b>" + depot.getName() + "</b> existe déjà.");
            }else{
                notification.setMessage("Erreur lors de l'enregistrement du dépôt <b>[ " + depot.getName() + " ]</b>.");
            }
        }

        attributes.addFlashAttribute("notification", notification);
        return "redirect:/depots";
    }

    @PostMapping(value="delete")
    public String delete(@RequestParam ArrayList<Long> ids, RedirectAttributes attributes){
        try {
            depotRepository.deleteAllById(ids);
            attributes.addFlashAttribute("notification", new Notification("success", "Opération terminée avec succès."));
        }catch (Exception e){
            attributes.addFlashAttribute("notification", new Notification("error", "Une erreur est survenue lors de cette opération."));
        }
        return "redirect:/depots";
    }
}

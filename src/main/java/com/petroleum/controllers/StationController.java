package com.petroleum.controllers;

import com.petroleum.mappers.StationMapper;
import com.petroleum.models.Station;
import com.petroleum.models.Notification;
import com.petroleum.repositories.StationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/stations")
public class StationController {
    private final StationRepository stationRepository;
    private final StationMapper stationMapper;

    @GetMapping
    public String getAll(Model model){
        List<Station> stations = stationRepository.findAllByOrderByNameAsc();
        model.addAttribute("stations", stations);
        return "stations";
    }

    @PostMapping
    public String save(@NonNull Station stationDto, RedirectAttributes attributes){
        Station station = stationDto;
        if(stationDto.getId() != null){
            station = stationRepository.findById(station.getId()).orElse(stationDto);
            stationMapper.update(station, stationDto);
        }
        Notification notification = new Notification();
        try {
            station = stationRepository.save(station);
            notification.setType("success");
            notification.setMessage("<b>" + station.getName() +"</b> a été enregistré.");
        } catch (Exception e){
            log.error("Error while saving station", e);
            notification.setType("error");
            notification.setMessage("Erreur lors de l'enregistrement du dépôt <b>[ " + station.getName() + " ]</b>.");
        }

        attributes.addFlashAttribute("notification", notification);
        return "redirect:/stations";
    }

    @PostMapping(value="delete")
    public String delete(@RequestParam ArrayList<Long> ids, RedirectAttributes attributes){
        try {
            stationRepository.deleteAllById(ids);
            attributes.addFlashAttribute("notification", new Notification("success", "Opération terminée avec succès."));
        }catch (Exception e){
            attributes.addFlashAttribute("notification", new Notification("error", "Une erreur est survenue lors de cette opération."));
        }
        return "redirect:/stations";
    }
}

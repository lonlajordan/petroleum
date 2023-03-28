package com.petroleum.controllers;

import com.petroleum.enums.Role;
import com.petroleum.enums.Status;
import com.petroleum.enums.Step;
import com.petroleum.mappers.TransferMapper;
import com.petroleum.models.*;
import com.petroleum.repositories.*;
import com.petroleum.services.EmailHelper;
import com.petroleum.services.PrintHelper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/transfers")
public class TransferController {

    private final TransferRepository transferRepository;
    private final ProductRepository productRepository;
    private final DepotRepository depotRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final TransferMapper transferMapper;

    @Value("${transfer.location}")
    private String transferLocation;
    @PersistenceContext
    private EntityManager em;

    @GetMapping
    public String getInvoices(@RequestParam(required = false, defaultValue = "1") int p, Model model, HttpSession session){
        Pageable pageable = PageRequest.of(p  - 1, 1000);
        Page<Transfer> transfers = transferRepository.findAllByOrderByDateDesc(pageable);
        model.addAttribute("transfers", transfers.get().collect(Collectors.toList()));
        model.addAttribute("totalPages", transfers.getTotalPages());
        model.addAttribute("currentPage", p);
        model.addAttribute("filtered", false);
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("depots", depotRepository.findAll());
        User user = (User) session.getAttribute("user");
        model.addAttribute("isDirector", Role.ROLE_DIRECTOR.equals(user.getRole()));
        return "transfers";
    }

    @GetMapping("{id}")
    public void downloadFile(@PathVariable long id, HttpServletResponse response) {
        try {
            Transfer transfer = transferRepository.findById(id).orElse(null);
            if(transfer != null){
                File directory = new File(transferLocation);
                if (!directory.exists() && !directory.mkdirs()) throw new SecurityException("Error while creating transfer pdf folder");
                File output = new File(directory.getAbsolutePath() + File.separator + "bon_transfert_" + id + ".pdf");
                PrintHelper.print(transfer, output);
                InputStream inputStream = new InputStreamResource(new FileInputStream(output)).getInputStream();
                response.setContentType(String.valueOf(MediaType.APPLICATION_OCTET_STREAM));
                response.setHeader("Content-Transfer-Encoding", "binary");
                response.setHeader("Content-Disposition", "attachment; filename=" + output.getName());
                IOUtils.copy(inputStream, response.getOutputStream());
                inputStream.close();
                response.flushBuffer();
                response.setStatus(HttpServletResponse.SC_OK);
            }else{
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    @GetMapping(value="{id}/{action}")
    public String changeStatus(@PathVariable long id, @PathVariable String action, RedirectAttributes attributes){
        Notification notification = new Notification("error", "bon introuvable.");
        try {
            Transfer transfer = transferRepository.findById(id).orElse(null);
            String to = "";
            String message = "";
            if(transfer != null){
                if("submit".equals(action)){
                    transfer.setStep(Step.OPERATING_OFFICER);
                    transfer.setStatus(Status.PENDING);
                    transfer.setReason("");
                    to = userRepository.findFirstByRole(Role.ROLE_OPERATING_OFFICER).orElse(new User()).getEmail();
                    message = "Le bon de transfert ID-" + transfer.getId() + " est en attente d'approbation.";
                }else if("approve".equals(action)){
                    if(Step.DIRECTOR.equals(transfer.getStep())){
                        transfer.setStatus(Status.APPROVED);
                        to = userRepository.findFirstByRole(Role.ROLE_DISPATCHER).orElse(new User()).getEmail();
                        message = "Le bon de transftert ID-" + transfer.getId() + " a été approuvé par la Direction Générale. Vous pouvez procéder à l'impression.";
                    }else{
                        double volume = transfer.getVolume();
                        double available = stockRepository.findFirstByDepotAndProduct(transfer.getLoadingDepot(), transfer.getProduct()).orElse(new Stock(transfer.getLoadingDepot(), transfer.getProduct())).getVolume();
                        if(available < volume){
                            transfer.setStatus(Status.WAITING);
                            transfer.setStep(Step.DIRECTOR);
                            to = userRepository.findFirstByRole(Role.ROLE_DIRECTOR).orElse(new User()).getEmail();
                            message = "Le bon de transfert ID-" + transfer.getId() + " est en attente d'approbation.";
                        }else{
                            transfer.setStatus(Status.APPROVED);
                            to = userRepository.findFirstByRole(Role.ROLE_DISPATCHER).orElse(new User()).getEmail();
                            message = "Le bon de transfert ID-" + transfer.getId() + " a été approuvé par le Chef d'Exploitation. Vous pouvez procéder à l'impression.";
                        }
                    }
                    StringBuilder body = new StringBuilder("<div style='line-height: 1.6'>Bonjour Mr/Mme,<br>")
                            .append(message).append("<br>")
                            .append("Cordialement.</div>");
                    EmailHelper.sendMail(to, "","Processus de validation d'un bon de transfert", body.toString());
                }
                transfer = transferRepository.save(transfer);
                if(Status.APPROVED.equals(transfer.getStatus())){
                    Stock stock = stockRepository.findFirstByDepotAndProduct(transfer.getLoadingDepot(), transfer.getProduct()).orElse(new Stock(transfer.getLoadingDepot(), transfer.getProduct()));
                    stock.setVolume(stock.getVolume() - transfer.getVolume());
                    stock.setUpdatedAt(LocalDateTime.now());
                    stockRepository.save(stock);
                    stock = stockRepository.findFirstByDepotAndProduct(transfer.getDeliveryPlace(), transfer.getProduct()).orElse(new Stock(transfer.getDeliveryPlace(), transfer.getProduct()));
                    stock.setVolume(stock.getVolume() + transfer.getVolume());
                    stock.setUpdatedAt(LocalDateTime.now());
                    stockRepository.save(stock);
                }
                notification.setType("success");
                notification.setMessage("L'opération a été effectuée avec succès.");
            }
        }catch (Exception e){
            notification.setType("error");
            notification.setMessage("Erreur lors de cette opération.");
        }
        attributes.addFlashAttribute("notification", notification);
        return "redirect:/transfers";
    }

    @PostMapping(value="reject")
    public String reject(@RequestParam long id, @RequestParam String reason, RedirectAttributes attributes){
        Notification notification = new Notification("error", "bon introuvable.");
        try {
            Transfer transfer = transferRepository.findById(id).orElse(null);
            if(transfer != null){
                transfer.setStep(Step.DISPATCHER);
                transfer.setStatus(Status.REJECTED);
                transfer.setReason(reason);
                transferRepository.save(transfer);
                String to = userRepository.findFirstByRole(Role.ROLE_DISPATCHER).orElse(new User()).getEmail();
                if(StringUtils.isNotBlank(to)){
                    StringBuilder body = new StringBuilder("<div style='line-height: 1.6'>Bonjour Mr/Mme,<br>")
                            .append("Le bon de transfert ID-").append(transfer.getId()).append(" a été rejété pour le motif suivant : <b>").append(reason).append("</b>").append("<br>")
                            .append("Veuillez apporter les corrections nécessaires et soumettre à nouveau.<br>")
                            .append("Cordialement.</div>");
                    EmailHelper.sendMail(to, "","Processus de validation d'un bon de transfert", body.toString());
                }
                notification.setType("success");
                notification.setMessage("L'opération a été effectuée avec succès.");
            }
        }catch (Exception e){
            notification.setType("error");
            notification.setMessage("Erreur lors de cette opération.");
        }
        attributes.addFlashAttribute("notification", notification);
        return "redirect:/transfers";
    }

    @PostMapping
    public String save(@NonNull Transfer transferDto, @RequestParam long productId, @RequestParam long loadingDepotId, @RequestParam long deliveryPlaceId, RedirectAttributes attributes){
        Transfer transfer = transferDto;
        boolean creation = true;
        if(transfer.getId() != null){
            transfer = transferRepository.findById(transfer.getId()).orElse(transferDto);
            transferMapper.update(transfer, transferDto);
            creation = false;
        }
        transfer.setProduct(em.getReference(Product.class, productId));
        transfer.setLoadingDepot(em.getReference(Depot.class, loadingDepotId));
        transfer.setDeliveryPlace(em.getReference(Depot.class, deliveryPlaceId));
        transfer.normalize();
        Notification notification = new Notification();
        try {
            transfer = transferRepository.save(transfer);
            if(creation){
                String to = userRepository.findFirstByRole(Role.ROLE_OPERATING_OFFICER).orElse(new User()).getEmail();
                if(StringUtils.isNotBlank(to)){
                    StringBuilder body = new StringBuilder("<div style='line-height: 1.6'>Bonjour Mr/Mme,<br>")
                            .append("Le bon de transfert ID-").append(transfer.getId()).append(" est en attente d'approbation.<br>")
                            .append("Cordialement.</div>");
                    EmailHelper.sendMail(to, "","Processus de validation d'un bon de transfert", body.toString());
                }
            }
            notification.setType("success");
            notification.setMessage("Le bon de transfert a été enregistré.");
        } catch (Exception e){
            e.printStackTrace();
            notification.setType("error");
            notification.setMessage("Erreur lors de l'enregistrement du bon de transfert.");
        }

        attributes.addFlashAttribute("notification", notification);
        return "redirect:/transfers";
    }

    @PostMapping(value="delete")
    public String delete(@RequestParam ArrayList<Long> ids, RedirectAttributes attributes){
        try {
            transferRepository.deleteAllByIdInAndStatusNot(ids, Status.APPROVED);
            attributes.addFlashAttribute("notification", new Notification("success", "Opération terminée avec succès."));
        }catch (Exception e){
            attributes.addFlashAttribute("notification", new Notification("error", "Une erreur est survenue lors de cette opération."));
        }
        return "redirect:/transfers";
    }
}

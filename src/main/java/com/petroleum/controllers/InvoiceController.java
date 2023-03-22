package com.petroleum.controllers;

import com.petroleum.enums.Role;
import com.petroleum.enums.Status;
import com.petroleum.enums.Step;
import com.petroleum.mappers.InvoiceMapper;
import com.petroleum.models.Invoice;
import com.petroleum.models.Notification;
import com.petroleum.models.Product;
import com.petroleum.models.User;
import com.petroleum.repositories.InvoiceRepository;
import com.petroleum.repositories.SupplyRepository;
import com.petroleum.repositories.UserRepository;
import com.petroleum.services.EmailHelper;
import com.petroleum.services.PrintHelper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

@Controller
@RequiredArgsConstructor
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final SupplyRepository supplyRepository;
    private final InvoiceMapper invoiceMapper;

    @Value("${invoice.location}")
    private String invoiceLocation;
    @PersistenceContext
    private EntityManager em;

    @GetMapping("{id}")
    public void downloadFile(@PathVariable long id, HttpServletResponse response) {
        try {
            Invoice invoice = invoiceRepository.findById(id).orElse(null);
            if(invoice != null){
                File directory = new File(invoiceLocation);
                if (!directory.exists() && !directory.mkdirs()) throw new SecurityException("Error while creating ticket excel folder");
                File output = new File(directory.getAbsolutePath() + File.separator + "bon_enlevement_" + id + ".pdf");
                PrintHelper.print(invoice, output);
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
            Invoice invoice = invoiceRepository.findById(id).orElse(null);
            String to = "";
            String message = "";
            if(invoice != null){
                if("submit".equals(action)){
                    invoice.setStep(Step.OPERATING_OFFICER);
                    invoice.setStatus(Status.PENDING);
                    invoice.setReason("");
                    to = userRepository.findFirstByRole(Role.ROLE_OPERATING_OFFICER).orElse(new User()).getEmail();
                    message = "Le bon d'enlèvement ID-" + invoice.getId() + " est en attente d'approbation.";
                }else if("approve".equals(action)){
                    if(Step.DIRECTOR.equals(invoice.getStep())){
                        invoice.setStatus(Status.APPROVED);
                        to = userRepository.findFirstByRole(Role.ROLE_DISPATCHER).orElse(new User()).getEmail();
                        message = "Le bon d'enlèvement ID-" + invoice.getId() + " a été approuvé par la Direction Générale. Vous pouvez procéder à l'impression.";
                    }else{
                        int volume = invoice.getVolume();
                        Product product = invoice.getProduct();
                        int available = supplyRepository.sumProductVolume(product.getId()) -  invoiceRepository.sumProductVolumeByStatus(product.getId(), Status.APPROVED);
                        if(available < volume){
                            invoice.setStatus(Status.WAITING);
                            invoice.setStep(Step.DIRECTOR);
                            to = userRepository.findFirstByRole(Role.ROLE_DIRECTOR).orElse(new User()).getEmail();
                            message = "Le bon d'enlèvement ID-" + invoice.getId() + " est en attente d'approbation.";
                        }else{
                            invoice.setStatus(Status.APPROVED);
                            to = userRepository.findFirstByRole(Role.ROLE_DISPATCHER).orElse(new User()).getEmail();
                            message = "Le bon d'enlèvement ID-" + invoice.getId() + " a été approuvé par le Chef d'Exploitation. Vous pouvez procéder à l'impression.";
                        }
                    }
                    StringBuilder body = new StringBuilder("<div style='line-height: 1.6'>Bonjour Mr/Mme,<br>")
                            .append(message).append("<br>")
                            .append("Cordialement.</div>");
                    EmailHelper.sendMail(to, "","Processus de validation d'un bon d'enlèvement", body.toString());
                }
                invoiceRepository.save(invoice);
                notification.setType("success");
                notification.setMessage("L'opération a été effectuée avec succès.");
            }
        }catch (Exception e){
            notification.setType("error");
            notification.setMessage("Erreur lors de cette opération.");
        }
        attributes.addFlashAttribute("notification", notification);
        return "redirect:/home";
    }

    @PostMapping(value="reject")
    public String reject(@RequestParam long id, @RequestParam String reason, RedirectAttributes attributes){
        Notification notification = new Notification("error", "bon introuvable.");
        try {
            Invoice invoice = invoiceRepository.findById(id).orElse(null);
            if(invoice != null){
                invoice.setStep(Step.DISPATCHER);
                invoice.setStatus(Status.REJECTED);
                invoice.setReason(reason);
                invoiceRepository.save(invoice);
                String to = userRepository.findFirstByRole(Role.ROLE_DISPATCHER).orElse(new User()).getEmail();
                if(StringUtils.isNotBlank(to)){
                    StringBuilder body = new StringBuilder("<div style='line-height: 1.6'>Bonjour Mr/Mme,<br>")
                            .append("Le bon d'enlèvement ID-").append(invoice.getId()).append(" a été rejété pour le motif suivant : <b>").append(reason).append("</b>").append("<br>")
                            .append("Veuillez apporter les corrections nécessaires et soumettre à nouveau.<br>")
                            .append("Cordialement.</div>");
                    EmailHelper.sendMail(to, "","Processus de validation d'un bon d'enlèvement", body.toString());
                }
                notification.setType("success");
                notification.setMessage("L'opération a été effectuée avec succès.");
            }
        }catch (Exception e){
            notification.setType("error");
            notification.setMessage("Erreur lors de cette opération.");
        }
        attributes.addFlashAttribute("notification", notification);
        return "redirect:/home";
    }

    @PostMapping
    public String save(@NonNull Invoice invoiceDto, @RequestParam long productId, RedirectAttributes attributes){
        Invoice invoice = invoiceDto;
        boolean creation = true;
        if(invoice.getId() != null){
            invoice = invoiceRepository.findById(invoice.getId()).orElse(invoiceDto);
            invoiceMapper.update(invoice, invoiceDto);
            creation = false;
        }
        invoice.setProduct(em.getReference(Product.class, productId));
        invoice.normalize();
        Notification notification = new Notification();
        try {
            invoice = invoiceRepository.save(invoice);
            if(creation){
                String to = userRepository.findFirstByRole(Role.ROLE_OPERATING_OFFICER).orElse(new User()).getEmail();
                if(StringUtils.isNotBlank(to)){
                    StringBuilder body = new StringBuilder("<div style='line-height: 1.6'>Bonjour Mr/Mme,<br>")
                            .append("Le bon d'enlèvement ID-").append(invoice.getId()).append(" est en attente d'approbation.<br>")
                            .append("Cordialement.</div>");
                    EmailHelper.sendMail(to, "","Processus de validation d'un bon d'enlèvement", body.toString());
                }
            }
            notification.setType("success");
            notification.setMessage("Le bon d'enlèvement a été enregistré.");
        } catch (Exception e){
            e.printStackTrace();
            notification.setType("error");
            notification.setMessage("Erreur lors de l'enregistrement du bon d'enlèvement.");
        }

        attributes.addFlashAttribute("notification", notification);
        return "redirect:/home";
    }

    @PostMapping(value="delete")
    public String delete(@RequestParam ArrayList<Long> ids, RedirectAttributes attributes){
        try {
            invoiceRepository.deleteAllByIdInAndStatusNot(ids, Status.APPROVED);
            attributes.addFlashAttribute("notification", new Notification("success", "Opération terminée avec succès."));
        }catch (Exception e){
            attributes.addFlashAttribute("notification", new Notification("error", "Une erreur est survenue lors de cette opération."));
        }
        return "redirect:/home";
    }
}

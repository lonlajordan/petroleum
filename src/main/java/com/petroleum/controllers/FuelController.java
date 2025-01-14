package com.petroleum.controllers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.petroleum.mappers.FuelMapper;
import com.petroleum.models.Fuel;
import com.petroleum.models.Notification;
import com.petroleum.repositories.FuelRepository;
import com.petroleum.utils.TextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/fuels")
public class FuelController {
    private final FuelRepository fuelRepository;
    private final ServletContext servletContext;
    private final FuelMapper fuelMapper;

    @PersistenceContext
    private EntityManager em;

    @Value("${qr.code.location}")
    private String qrCodeLocation;
    @Value("${application.url}")
    private String applicationUrl;

    @GetMapping
    public String getFuels(@RequestParam(required = false, defaultValue = "1") int p, Model model){
        Pageable pageable = PageRequest.of(p  - 1, 500);
        Page<Fuel> fuels = fuelRepository.findAllByOrderByDateDesc(pageable);
        model.addAttribute("fuels", fuels.get().collect(Collectors.toList()));
        model.addAttribute("totalPages", fuels.getTotalPages());
        model.addAttribute("currentPage", p);
        return "fuels";
    }

    @GetMapping("generate")
    public String generate(@RequestParam int amount, @RequestParam int number){
        List<Fuel> fuels = new ArrayList<>();
        for(int i = number; i < 5000 + number; i++){
            Fuel fuel = new Fuel();
            fuel.setAmount(amount);
            fuel.setNumber(i);
            fuel.setCode(TextUtils.generateType1UUID().toString());
            fuels.add(fuel);
        }
        fuelRepository.saveAll(fuels);
        return "redirect:/fuels";
    }

    @PostMapping
    public String save(@NonNull Fuel form, RedirectAttributes attributes){
        Fuel fuel = form;
        Notification notification = new Notification();
        if(form.getId() != null){
            fuel = fuelRepository.findById(form.getId()).orElse(form);
            fuelMapper.update(fuel, form);
        }
        try {
            if(StringUtils.isBlank(fuel.getCode())) fuel.setCode(TextUtils.generateType1UUID().toString());
            fuelRepository.save(fuel);
            notification.setType("success");
            notification.setMessage("Le bon de carburant a été enregistré.");
        } catch (Exception e){
            log.error("Error while saving fuel ticket", e);
            notification.setType("error");
            notification.setMessage("Erreur lors de l'enregistrement.");
        }
        attributes.addFlashAttribute("notification", notification);
        return "redirect:/fuels";
    }

    @PostMapping(value="delete")
    public String delete(@RequestParam ArrayList<Long> ids, RedirectAttributes attributes){
        try {
            fuelRepository.deleteAllById(ids);
            attributes.addFlashAttribute("notification", new Notification("success", "Opération terminée avec succès."));
        }catch (Exception e){
            log.error("Error while deleting fuel ticket", e);
            attributes.addFlashAttribute("notification", new Notification("error", "Une erreur est survenue lors de cette opération."));
        }
        return "redirect:/fuels";
    }

    @GetMapping(value="{id}/toggle")
    public String toggleFuel(@PathVariable long id, RedirectAttributes attributes){
        Notification notification = new Notification("error", "bon de carburant introuvable.");
        try {
            Fuel fuel = fuelRepository.findById(id).orElse(null);
            if(fuel != null){
                fuel.setEnabled(!fuel.isEnabled());
                if(fuel.isEnabled()) {
                    fuel.setProduct(null);
                    fuel.setStation(null);
                    fuel.setMatriculation("");
                }
                fuelRepository.save(fuel);
                notification.setType("success");
                notification.setMessage("Le bon de carburant a été " + (fuel.isEnabled() ? "activé" : "désactivé") + " avec succès.");
            }
        }catch (Exception e){
            log.error("Error while changing fuel ticket status", e);
            notification.setType("error");
            notification.setMessage("Erreur lors du changement de statut du bon de carburant d'identifiant <b>" + id + "</b>.");
        }
        attributes.addFlashAttribute("notification", notification);
        return "redirect:/fuels";
    }

    @GetMapping("print")
    public void printQRCodes(@RequestParam int amount, @RequestParam int number) {
        try {
            List<Fuel> fuels = fuelRepository.findAllByAmountAndNumberGreaterThanOrderByNumberAsc(amount, number);
            for(Fuel fuel: fuels){
                File directory = new File(qrCodeLocation+"/" + amount);
                if (!directory.exists() && !directory.mkdirs()) throw new SecurityException("Error while creating qr codes folder");
                File output = new File(directory.getAbsolutePath() + File.separator + fuel.getNumber() + ".png");
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
                hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
                hints.put(EncodeHintType.MARGIN, 1);
                hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
                BitMatrix mat = new QRCodeWriter().encode(applicationUrl + servletContext.getContextPath() + "/validate?number=" + fuel.getNumber() + "&code=" + fuel.getCode(), BarcodeFormat.QR_CODE,100,100, hints);
                BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(mat);
                BufferedImage overly = ImageIO.read(new File("qr_logo.png"));

                Image tmp = overly.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                BufferedImage resized = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = resized.createGraphics();
                g2d.drawImage(tmp, 0, 0, null);
                g2d.dispose();
                overly = resized;

                int deltaHeight = qrImage.getHeight() - overly.getHeight();
                int deltaWidth = qrImage.getWidth() - overly.getWidth();
                BufferedImage combined = new BufferedImage(qrImage.getHeight(), qrImage.getWidth(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = (Graphics2D) combined.getGraphics();
                g.drawImage(qrImage, 0, 0, new Color(0xFF40BAD0),null);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                g.drawImage(overly, deltaWidth / 2, deltaHeight / 2, null);
                ImageIO.write(combined, "png", os);
                Files.copy( new ByteArrayInputStream(os.toByteArray()), output.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Generated Ticket : amount = " + fuel.getAmount() + " number = " + fuel.getNumber());
            }
        } catch (Exception e) {
            log.error("Error while printing QR-Code", e);
        }
    }

    @GetMapping("download/{id}")
    public void downloadReport(@PathVariable long id, HttpServletResponse response) {
        try {
            Fuel fuel = fuelRepository.findById(id).orElse(null);
            if(fuel != null){
                int amount = (int) fuel.getAmount();
                File directory = new File(qrCodeLocation + File.separator  + amount);
                if (!directory.exists() && !directory.mkdirs()) throw new SecurityException("Error while creating qr codes folder");
                File output = new File(directory.getAbsolutePath() + File.separator + fuel.getNumber() + ".png");
                if(!output.exists()) printQRCodes((int) fuel.getAmount(), fuel.getNumber());
                InputStream inputStream = new InputStreamResource(Files.newInputStream(output.toPath())).getInputStream();
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
            log.error("Error while downloading monthly tax report", e);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    @ResponseBody
    @GetMapping(value = "view/{amount}/{number}", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] viewFuel(@PathVariable int amount, @PathVariable int number) {
        Fuel fuel = fuelRepository.findByAmountAndNumber(amount, number);
        if(fuel != null){
            File directory = new File(qrCodeLocation + "/" + amount);
            File output = new File(directory.getAbsolutePath() + File.separator + number + ".png");
            try {
                InputStream inputStream = new InputStreamResource(Files.newInputStream(output.toPath())).getInputStream();
                return IOUtils.toByteArray(inputStream);
            } catch (IOException e) {
                log.error("Error while downloading fuel ticket QR-Code", e);
            }
        }
        return new byte[]{};
    }

    @PostMapping(value="search")
    public String search(@RequestParam(required = false, defaultValue = "1") int page, @RequestParam(required = false) Double amount, @RequestParam(required = false) Double number, @RequestParam(required = false, defaultValue = "-1") int status, Model model){
        int pageSize = 500;
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Fuel> cq = cb.createQuery(Fuel.class);
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Fuel> fuel = cq.from(Fuel.class);
        List<Predicate> predicates = new ArrayList<>();
        if(amount != null && amount > 0) predicates.add(cb.equal(fuel.get("amount"), amount));
        if(number != null && number > 0) predicates.add(cb.equal(fuel.get("number"), number));
        if(status > -1) predicates.add(cb.equal(fuel.get("enabled"), status == 1));
        cq.where(predicates.toArray(new Predicate[0]));
        countQuery.select(cb.count(countQuery.where(predicates.toArray(new Predicate[0])).from(Fuel.class)));
        TypedQuery<Fuel> query = em.createQuery(cq).setMaxResults(pageSize).setFirstResult((page - 1) * pageSize);
        List<Fuel> fuels = query.getResultList();
        long count = em.createQuery(countQuery).getSingleResult();
        int totalPages = ((int) count / pageSize) + (count % pageSize == 0 ? 0 : 1);
        model.addAttribute("fuels", fuels);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);
        model.addAttribute("filtered", true);
        model.addAttribute("amount", amount);
        model.addAttribute("number", number);
        model.addAttribute("status", status);
        return "fuels";
    }
}

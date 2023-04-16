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
import com.petroleum.models.Product;
import com.petroleum.repositories.FuelRepository;
import com.petroleum.repositories.ProductRepository;
import com.petroleum.utils.TextUtils;
import lombok.RequiredArgsConstructor;
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
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/fuels")
public class FuelController {
    private final FuelRepository fuelRepository;
    private final ProductRepository productRepository;
    private final ServletContext servletContext;
    private final FuelMapper fuelMapper;

    @Value("${qr.code.location}")
    private String qrCodeLocation;
    @Value("${application.url}")
    private String applicationUrl;

    @PersistenceContext
    private EntityManager em;

    @GetMapping
    public String getSupplies(@RequestParam(required = false, defaultValue = "1") int p, @RequestParam(required = false) Long productId, Model model){
        Pageable pageable = PageRequest.of(p  - 1, 1000);
        Page<Fuel> fuels = productId == null ? fuelRepository.findAllByOrderByDateDesc(pageable) : fuelRepository.findAllByProductIdOrderByDateDesc(productId, pageable);
        model.addAttribute("fuels", fuels.get().collect(Collectors.toList()));
        model.addAttribute("totalPages", fuels.getTotalPages());
        model.addAttribute("currentPage", p);
        model.addAttribute("products", productRepository.findAllByOrderByNameAsc());
        return "fuels";
    }

    @PostMapping
    public String save(@NonNull Fuel fuelDto, @RequestParam long productId, RedirectAttributes attributes){
        Fuel fuel = fuelDto;
        if(fuel.getId() != null){
            fuel = fuelRepository.findById(fuel.getId()).orElse(fuelDto);
            fuelMapper.update(fuel, fuelDto);
        }
        if(StringUtils.isBlank(fuel.getCode())) fuel.setCode(TextUtils.generateType1UUID().toString());
        fuel.setProduct(em.getReference(Product.class, productId));
        Notification notification = new Notification();
        try {
            fuelRepository.save(fuel);
            notification.setType("success");
            notification.setMessage("Le bon de carburant a été enregistré.");
        } catch (Exception e){
            notification.setType("error");
            notification.setMessage("Erreur lors de l'enregistrement du bon de carburant.");
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
                fuelRepository.save(fuel);
                notification.setType("success");
                notification.setMessage("Le bon de carburant a été " + (fuel.isEnabled() ? "activé" : "désactivé") + " avec succès.");
            }
        }catch (Exception e){
            notification.setType("error");
            notification.setMessage("Erreur lors du changement de statut du bon de carburant d'identifiant <b>" + id + "</b>.");
        }
        attributes.addFlashAttribute("notification", notification);
        return "redirect:/fuels";
    }

    @GetMapping("download/{id}")
    public void downloadReport(@PathVariable long id, HttpServletResponse response) {
        try {
            Fuel fuel = fuelRepository.findById(id).orElse(null);
            if(fuel != null){
                File directory = new File(qrCodeLocation);
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
}

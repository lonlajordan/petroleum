package com.petroleum.controllers;

import com.petroleum.enums.Status;
import com.petroleum.mappers.ProductMapper;
import com.petroleum.models.Notification;
import com.petroleum.models.Product;
import com.petroleum.repositories.InvoiceRepository;
import com.petroleum.repositories.ProductRepository;
import com.petroleum.repositories.SupplyRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {
    private final ProductRepository productRepository;
    private final InvoiceRepository invoiceRepository;
    private final SupplyRepository supplyRepository;
    private final ProductMapper productMapper;

    @GetMapping
    public String getAll(Model model){
        List<Product> products = productRepository.findAllByOrderByNameAsc();
        //products.forEach(product -> product.setVolume(supplyRepository.sumProductVolume(product.getId()) -  invoiceRepository.sumProductVolumeByStatus(product.getId(), Status.APPROVED)));
        model.addAttribute("products", products);
        return "products";
    }

    @PostMapping
    public String save(@NonNull Product productDto, RedirectAttributes attributes){
        Product product = productDto;
        if(productDto.getId() != null){
            product = productRepository.findById(product.getId()).orElse(productDto);
            productMapper.update(product, productDto);
        }
        product.normalize();
        Notification notification = new Notification();
        try {
            product = productRepository.save(product);
            notification.setType("success");
            notification.setMessage("<b>" + product.getName() +"</b> a été enregistré.");
        } catch (Exception e){
            e.printStackTrace();
            notification.setType("error");
            if(ExceptionUtils.getStackTrace(e).toLowerCase().contains("duplicate entry")){
                notification.setMessage("Le produit <b>" + product.getName() + "</b> existe déjà.");
            }else{
                notification.setMessage("Erreur lors de l'enregistrement du produit <b>[ " + product.getName() + " ]</b>.");
            }
        }

        attributes.addFlashAttribute("notification", notification);
        return "redirect:/products";
    }
}

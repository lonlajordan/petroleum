package com.petroleum.controllers;


import com.petroleum.models.Notification;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/")
    public String login() {
        return  isAuthenticated() ? "redirect:home" : "login" ;
    }

    @PostMapping("/")
    public String login(
            @RequestParam(required = false, defaultValue = "") String error,
            @RequestParam(required = false, defaultValue = "") String email,
            @RequestParam(required = false, defaultValue = "") String password,
            Model model) {
        if(StringUtils.isNotBlank(error)){
            String message = "Une erreur s'est produite. Réessayez plutard.";
            if("1".equalsIgnoreCase(error)){
                message = "utilisateur introuvable";
            }else if("2".equalsIgnoreCase(error)){
                message = "mot de passe incorrect";
            }else if("3".equalsIgnoreCase(error)){
                message = "votre compte est désactivé";
            }
            Notification notification = new Notification();
            notification.setType("error");
            notification.setMessage(message);
            model.addAttribute("notification", notification);
            model.addAttribute("email", email);
            model.addAttribute("password", password);
            return "login";
        }
        return "redirect:home";
    }

    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || AnonymousAuthenticationToken.class.isAssignableFrom(authentication.getClass())) return false;
        return authentication.isAuthenticated();
    }
}

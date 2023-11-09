package com.petroleum.controllers;

import com.petroleum.mappers.UserMapper;
import com.petroleum.models.Notification;
import com.petroleum.models.User;
import com.petroleum.repositories.UserRepository;
import com.petroleum.services.EmailHelper;
import com.petroleum.utils.TextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Value("${application.url}")
    private String applicationUrl;

    @GetMapping
    public String getAll(Model model){
        List<User> users = userRepository.findAllByOrderByLastLoginDesc();
        model.addAttribute("users", users);
        return "users";
    }

    @PostMapping(value="delete")
    public String deleteUsers(@RequestParam Long[] ids, RedirectAttributes attributes){
        try {
            userRepository.deleteAllById(Arrays.asList(ids));
            attributes.addFlashAttribute("notification", new Notification("success", "Opération terminée avec succès."));
        }catch (Exception e){
            log.error("Error while deleting users", e);
            attributes.addFlashAttribute("notification", new Notification("error", "Une erreur est survenue lors de cette opération."));
        }
        return "redirect:/users";
    }

    @GetMapping(value="{id}/toggle")
    public String toggleUser(@PathVariable long id, RedirectAttributes attributes){
        Notification notification = new Notification("error", "utilisateur introuvable.");
        try {
            User user = userRepository.findById(id).orElse(null);
            if(user != null){
                user.setEnabled(!user.isEnabled());
                user = userRepository.save(user);
                notification.setType("success");
                notification.setMessage("<b>" + user.getName() + "</b> a été " + (user.isEnabled() ? "activé" : "désactivé") + " avec succès.");
            }
        }catch (Exception e){
            log.error("Error while deleting user status", e);
            notification.setType("error");
            notification.setMessage("Erreur lors du changement de statut de l'utilisateur d'identifiant <b>" + id + "</b>.");
        }
        attributes.addFlashAttribute("notification", notification);
        return "redirect:/users";
    }

    @GetMapping(value="{id}/reset")
    public String resetUser(@PathVariable long id, RedirectAttributes attributes){
        Notification notification = new Notification("error", "utilisateur introuvable.");
        try {
            User user = userRepository.findById(id).orElse(null);
            if(user != null){
                String password = TextUtils.generatePassword();
                user.setPassword(new BCryptPasswordEncoder().encode(password));
                user = userRepository.save(user);
                String body = "<div style='line-height: 1.6'>Cher client,<br>" +
                        "Votre nouveau mot de passe est : <b> " + password + "</b><br>" +
                        "<a href='" + applicationUrl + "'>Ouvrir l'application</a><br>" +
                        "Cordialement.</div>";
                EmailHelper.sendMail(user.getEmail(), "","Changement de mot de passe", body);
                notification.setType("success");
                notification.setMessage("Le mot de passe de l'utilisateur <b>" + user.getName() + "</b> a été changé avec succès.");
            }
        }catch (Exception e){
            log.error("Error while changing user password", e);
            notification.setType("error");
            notification.setMessage("Erreur lors du changement du mot de passe de l'utilisateur d'identifiant <b>" + id + "</b>.");
        }
        attributes.addFlashAttribute("notification", notification);
        return "redirect:/users";
    }

    @PostMapping
    public String saveUser(@NonNull User userDto, RedirectAttributes attributes, HttpSession session){
        User user = userDto;
        String password;
        if(userDto.getId() != null){
            user = userRepository.findById(user.getId()).orElse(userDto);
            userMapper.update(user, userDto);
        } else {
            password = TextUtils.generatePassword();
            user.setPassword(new BCryptPasswordEncoder().encode(password));
            String body = "<div style='line-height: 1.6'>Cher client,<br>" +
                    "Vos paramètres de connexion sont : <br>" +
                    "<ol>" +
                    "<li><b>Adresse email : <b> " + user.getEmail() + "</li>" +
                    "<li><b>Mot de passe : <b> " + password + "</li>" +
                    "</ol><br>" +
                    "<a href='" + applicationUrl + "'>Ouvrir l'application</a><br>" +
                    "Cordialement.</div>";
            EmailHelper.sendMail(user.getEmail(), "","Nouveau Compte", body);
        }
        user.normalize();
        Notification notification = new Notification();
        try {
            user = userRepository.save(user);
            notification.setType("success");
            notification.setMessage("<b>" + user.getName() +"</b> a été enregistré.");
            userDto = (User) session.getAttribute("user");
            if(userDto != null && userDto.getId().equals(user.getId())){
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                Collection<SimpleGrantedAuthority> authorities$ = Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()));
                auth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), authorities$);
                SecurityContextHolder.getContext().setAuthentication(auth);
                session.setAttribute("user", user);
            }
        } catch (Exception e){
            log.error("Error while saving user", e);
            notification.setType("error");
            if(ExceptionUtils.getStackTrace(e).toLowerCase().contains("duplicate entry")){
                notification.setMessage("L'adresse e-mail <b>[ " + user.getEmail() + " ]</b> existe déjà.");
            }else{
                notification.setMessage("Erreur lors de l'enregistrement de l'utilisateur <b>[ " + user.getName() + " ]</b>.");
            }
        }

        attributes.addFlashAttribute("notification", notification);
        return "redirect:/users";
    }
}

package com.example.Controller;

import com.example.Dto.security.RegistrationDto;
import com.example.Model.Project;
import com.example.Model.Security.UserEntity;

import com.example.Repository.Security.RoleRepository;
import com.example.Security.SecurityUtil;
import com.example.Service.Security.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AuthController { // for Security
    private UserService userService;private RoleRepository roleRepository;
    @Autowired
    public AuthController(UserService userService, RoleRepository roleRepository) {
        this.userService = userService;this.roleRepository = roleRepository;
    }
    //Registration
    @GetMapping("/register")
    public String getRegisterForm(Model model)
    {
        RegistrationDto user = new RegistrationDto();
        model.addAttribute("user", user); // we add empty object into a View ,
        // but if we don`t do it --> we will get an error
        return"register";
    }
    @PostMapping("/register/save")
    public String register(@Valid @ModelAttribute("user")RegistrationDto user,
                           BindingResult result, Model model) {
        // Check by email
        UserEntity existingUserEmail = userService.findByEmail(user.getEmail());

        if(existingUserEmail != null && existingUserEmail.getEmail() != null && !existingUserEmail.getEmail().isEmpty()) {
            model.addAttribute("user", user);
            return "redirect:/register?fail";
        }

        // Check by existing usernames
        UserEntity existingUserUsername = userService.findByUsername(user.getUsername());
        if(existingUserUsername != null && existingUserUsername.getUsername() != null && !existingUserUsername.getUsername().isEmpty()) {
            model.addAttribute("user", user);
            return "redirect:/register?fail";
        }

        if(result.hasErrors()) {
            // Add user object to model to preserve form data
            model.addAttribute("user", user);
            return "register";
        }

        userService.saveUser(user);
        return "redirect:/home?success";
    }
    //Login
    @GetMapping("/login")
    public String loginPage()
    {
        return "login";
    }





}

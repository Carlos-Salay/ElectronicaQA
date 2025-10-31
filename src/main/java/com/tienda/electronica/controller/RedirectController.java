package com.tienda.electronica.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import io.swagger.v3.oas.annotations.Hidden;

@Controller
@Hidden // Oculta este controlador de la documentación de Swagger
public class RedirectController {
    @GetMapping("/")
    public String redirectToSwagger() {
        return "redirect:/swagger-ui.html";
    }
}
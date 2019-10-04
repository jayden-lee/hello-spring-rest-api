package com.jayden.tutorial.springrestapi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping(value = "/docs")
    public String docs() {
        return "redirect:/docs/index.html";
    }
}

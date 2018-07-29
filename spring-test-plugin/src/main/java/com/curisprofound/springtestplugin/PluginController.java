package com.curisprofound.springtestplugin;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/plugin-mvc-controller")
public class PluginController {
    @GetMapping
    public ResponseEntity<String> greetMVC(){
        return ResponseEntity.ok().body("An endpoint defined by annotation in plugin");
    }
}

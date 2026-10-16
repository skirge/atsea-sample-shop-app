package com.docker.atsea.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.ws.Response;
import java.nio.file.Path;

@RestController
@RequestMapping("/api")
public class GroovyScriptController {


    @RequestMapping("validate")
    public Response validateScript(@PathVariable("script") String script) {
        return null;
    }

}

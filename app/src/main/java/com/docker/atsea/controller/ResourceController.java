package com.docker.atsea.controller;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import com.docker.atsea.util.CustomErrorType;

@RestController
@RequestMapping("/api")
public class ResourceController {

    // from original CVE-2026-21589 application code
    public static String unescapeSlashes(String string) {
	    return string.replaceAll("::", "/");
    }   

    @RequestMapping(path = "/resource", method = GET)
    public ResponseEntity<byte[]> getResource(@RequestParam("name") String name) throws IOException {
        // Validation happens before the custom slash decoding, allowing ..:: to bypass it.
        if (name.isEmpty() || name.startsWith("/") || name.startsWith("\\")
                || name.contains("../") || name.contains("..\\")) {
            return new ResponseEntity(new CustomErrorType("Attack detected!"),HttpStatus.BAD_REQUEST);
        }

        String resourceName = unescapeSlashes(name);
        
        // does not resolve arbitrary host filesystem paths
        ClassPathResource resource = new ClassPathResource("static/images/" + resourceName);
        if (!resource.exists() || resource.getURL().getPath().endsWith("/")) {
            return new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND);
        }

        try (InputStream input = resource.getInputStream()) {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(IOUtils.toByteArray(input));
        }
    }
}

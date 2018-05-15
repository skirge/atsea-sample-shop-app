package com.docker.atsea.controller;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/api")
public class ImageController {
    public static final Logger logger = LoggerFactory.getLogger(ImageController.class);

    // TODO: SSRF, LFI and also Content Sniffing possible
    @RequestMapping(path="/image", method = GET)
    @Produces("image/png")
    public ResponseEntity<byte[]> getImage(@QueryParam("url") String url, UriComponentsBuilder ucBuilder) throws IOException {
        logger.info("Trying to get image from url = " + url);

        if(StringUtils.isNotEmpty(url) && url.startsWith("/")) {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setLocation(URI.create(ucBuilder.path(url).toUriString()));
            return new ResponseEntity<byte[]>(null, httpHeaders, HttpStatus.PERMANENT_REDIRECT);
        }
        byte[] imageBytes = IOUtils.toByteArray(new URL(url));
        return new ResponseEntity<byte[]>(imageBytes,HttpStatus.OK);
    }
}

package com.docker.atsea.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping(value = "/api/")
public class CardController {
    private static final List<String> CARDS = new ArrayList<>(Arrays.asList(
            "4481216339439291",
            "4481216339439292",
            "4481216339439293"
    ));

    public static final Logger logger = LoggerFactory.getLogger(CardController.class);

    @RequestMapping(value = "/card/", method = RequestMethod.GET)
    public ResponseEntity<String> getAllCards(@RequestHeader(name = "X-Forwarded-For", required = false) String forwarderFor) {
        if (StringUtils.isEmpty(forwarderFor)) {
            return new ResponseEntity("Only whitelisted IP can use this endpoint",HttpStatus.FORBIDDEN);
        } else  if(StringUtils.equals("127.0.0.1", forwarderFor) || StringUtils.equals("185.4.44.68", forwarderFor)) {
            return ResponseEntity.ok("4481216339439293");
        } else {
            return new ResponseEntity("Invalid IP!", HttpStatus.FORBIDDEN);
        }
    }

    @RequestMapping(value = "/card/{cardId}", method = RequestMethod.GET)
    public ResponseEntity<String> getCard(@PathVariable("cardId") int cardId) {
        return ResponseEntity.ok(CARDS.get(cardId));
    }

}

package com.docker.atsea.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.smartcardio.Card;
import javax.ws.rs.Consumes;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

@RestController
@RequestMapping(value = "/api/")
public class CardController {
    public static final Logger logger = LoggerFactory.getLogger(CardController.class);

    @RequestMapping(value = "/card/", method = RequestMethod.GET)
    public ResponseEntity<String> getAllCards() {

        return ResponseEntity.ok("4481216339439293");
    }



}

package com.docker.atsea.controller;

import com.docker.atsea.model.Product;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

public class ProductHandler extends DefaultHandler {
    private List<Product> products = null;

    private Product product;
    private boolean isDescription;
    private boolean isName;
    private boolean isImage;
    private boolean isPrice;

    public ProductHandler() {
        products = new ArrayList<Product>();
    }

    public List<Product> getProducts() {
        return products;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if(qName.equalsIgnoreCase("Product")) {
        product = new Product();
        } else if (qName.equalsIgnoreCase("description")) {
            isDescription = true;
        } else if (qName.equalsIgnoreCase("name")) {
            isName = true;
        } else if (qName.equalsIgnoreCase("image")) {
            isImage = true;
        } else if(qName.equalsIgnoreCase("price")) {
            isPrice = true;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(qName.equalsIgnoreCase("Product")) {
            products.add(product);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if(isName) {
            product.setName(new String(ch,start,length));
            isName=false;
        } else if(isDescription) {
            product.setDescription(new String(ch,start,length));
            isDescription=false;
        } else if(isImage) {
            product.setImage(new String(ch, start,length));
            isImage=false;
        } else if(isPrice) {
            // TODO: Spel Injection
            SpelExpressionParser spel = new SpelExpressionParser();
            String exp = new String(ch, start, length);
            product.setPrice((Double) spel.parseRaw(exp).getValue());
            isPrice = false;
        }
    }
}

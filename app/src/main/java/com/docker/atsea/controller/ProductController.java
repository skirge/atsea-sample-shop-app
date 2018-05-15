package com.docker.atsea.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.web.bind.annotation.*;

import com.docker.atsea.model.Product;
import com.docker.atsea.service.ProductService;
import com.docker.atsea.util.CustomErrorType;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.ws.rs.Consumes;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

@RestController
@RequestMapping("/api")
public class ProductController {
	public static final Logger logger = LoggerFactory.getLogger(ProductController.class);
	
	@Autowired
	ProductService productService;
	
	// -------------------------------------------------------------------
	//                   Product methods
	//--------------------------------------------------------------------


	// -------------------Retrieve All Products---------------------------------------------

	@RequestMapping(value = "/product/", method = RequestMethod.GET)
	public ResponseEntity<List<Product>> listAllProducts() {
		List<Product> products = productService.findAllProducts();
		if (products.isEmpty()) {
			return new ResponseEntity<List<Product>>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<Product>>(products, HttpStatus.OK);
	}

	@RequestMapping(value = "/product/", method = RequestMethod.POST)
	public ResponseEntity<Product> createProduct(@RequestBody Product newProduct)
	{
		logger.info("Creating new product = [" + newProduct + "]");
		// TODO: actual order creation not done

		return new ResponseEntity<Product>(newProduct, HttpStatus.OK);
	}

	@RequestMapping(value = "/product/", method = RequestMethod.PATCH, headers = {"content-type=application/xml"})
	public ResponseEntity<List<Product>> patchProductXML(@RequestBody String newProduct) throws IOException, SAXException, ParserConfigurationException {
		logger.info("Creating new product from XML = [" + newProduct + "]");
		ProductHandler ph = new ProductHandler();
		receiveXMLStream(new StringBufferInputStream(newProduct), ph);
		return new ResponseEntity<List<Product>>(ph.getProducts(), HttpStatus.OK);
	}

	private static void receiveXMLStream(InputStream inStream,
										 DefaultHandler defaultHandler)
			throws ParserConfigurationException, SAXException, IOException, org.xml.sax.SAXException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		saxParser.parse(inStream, defaultHandler);
	}

	// -------------------Retrieve Single Product By Id------------------------------------------

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping(value = "/product/{productId}", method = RequestMethod.GET)
	public ResponseEntity<?> getProduct(@PathVariable("productId") long productId) {
		logger.info("Fetching Product with id {}", productId);
		Product product = productService.findById(productId);
		if (product == null) {
			logger.error("Product with id {} not found.", productId);
			return new ResponseEntity(new CustomErrorType("Product with id " + productId 
					+ " not found"), HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Product>(product, HttpStatus.OK);
	}
}

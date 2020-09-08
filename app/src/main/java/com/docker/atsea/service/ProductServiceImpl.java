package com.docker.atsea.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.docker.atsea.model.Product;
import com.docker.atsea.repositories.ProductRepository;

@Service("productService")
@Transactional
public class ProductServiceImpl implements ProductService {

	@Autowired
	private ProductRepository productRepository;

	public List<Product> findAllProducts() {
		return productRepository.findAll();
	}

	public Product findByName(String name) {
		return productRepository.findByName(name);
	}

	public Product findById(Long productId) {
		Optional<Product> product = productRepository.findById(productId);
		return product.orElse(null);
	}

	@Override
	public Product createProduct(Product product) {
		Product newProduct = productRepository.save(product);
		productRepository.flush();
		return newProduct;
	}
}

package dev.agasen.core.product.commands;

import dev.agasen.api.product.product.ProductCreationDetails;
import dev.agasen.common.cqrs.Command;
import dev.agasen.common.validations.BeanValidator;
import dev.agasen.common.validations.Validatable;
import dev.agasen.common.validations.ValidationResult;
import dev.agasen.core.product.mapper.ProductMapper;
import dev.agasen.core.product.persistence.ProductRepository;
import dev.agasen.core.product.persistence.entity.Product;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateProductCommand implements Command< Product >, Validatable {

   private final ProductRepository productRepository;
   private final ProductCreationDetails productCreationDetails;
   private static final ProductMapper productMapper = ProductMapper.INSTANCE;
   private static final BeanValidator beanValidator = new BeanValidator();

   @Override
   public Product execute() {
      Product mapped = productMapper.toEntity( productCreationDetails );
      Product saved = productRepository.save( mapped );
      return saved;
   }

   @Override
   public ValidationResult validate() {


      return null;
   }
}

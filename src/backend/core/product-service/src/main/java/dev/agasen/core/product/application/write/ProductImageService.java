package dev.agasen.core.product.application.write;

import dev.agasen.platform.core.storage.FileStoragePort;
import dev.agasen.platform.core.storage.UploadDirective;
import dev.agasen.core.product.domain.product.Product;
import dev.agasen.core.product.domain.product.ProductRepository;
import dev.agasen.core.product.infrastructure.s3.ProductImageBucket;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class ProductImageService {

   private static final Duration UPLOAD_URL_EXPIRY = Duration.ofMinutes( 15 );

   private final ProductRepository productRepository;
   private final FileStoragePort< ProductImageBucket > productImageFileStoragePort;

   @PreAuthorize( "hasAuthority('SCOPE_product:write-create') or hasAuthority('SCOPE_openid')" )
   public UploadDirective prepareImageUpload( Long productId, String filename ) {
      if ( !productRepository.existsById( productId ) ) {
         throw new ResponseStatusException( HttpStatus.NOT_FOUND, "Product not found: " + productId );
      }
      return productImageFileStoragePort.prepareUpload( filename, UPLOAD_URL_EXPIRY );
   }

   @PreAuthorize( "hasAuthority('SCOPE_product:write-create') or hasAuthority('SCOPE_openid')" )
   public void saveImageKey( Long productId, String imageKey ) {
      Product product = productRepository.findById( productId )
         .orElseThrow( () -> new ResponseStatusException( HttpStatus.NOT_FOUND, "Product not found: " + productId ) );
      product.setImageKey( imageKey );
      productRepository.save( product );
   }

}

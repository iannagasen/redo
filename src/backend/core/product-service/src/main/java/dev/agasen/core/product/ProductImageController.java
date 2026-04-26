package dev.agasen.core.product;

import dev.agasen.platform.core.storage.UploadDirective;
import dev.agasen.core.product.application.write.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping( "/api/v1/products/{productId}/image" )
public class ProductImageController {

   private final ProductImageService productImageService;

   /**
    * Step 1 of image upload: returns a pre-signed S3 PUT URL.
    * The client uploads directly to S3 using the returned URL, then calls PATCH to confirm.
    */
   @PostMapping( "/upload-url" )
   public UploadDirective prepareUpload( @PathVariable Long productId, @RequestParam( "filename" ) String filename ) {
      return productImageService.prepareImageUpload( productId, filename );
   }

   /**
    * Step 2 of image upload: saves the S3 object key after the client has uploaded the file.
    */
   @PatchMapping
   public ResponseEntity< Void > saveImageKey( @PathVariable Long productId, @RequestBody SaveImageKeyRequest request ) {
      productImageService.saveImageKey( productId, request.imageKey() );
      return ResponseEntity.noContent().build();
   }

   record SaveImageKeyRequest( String imageKey ) {}
}

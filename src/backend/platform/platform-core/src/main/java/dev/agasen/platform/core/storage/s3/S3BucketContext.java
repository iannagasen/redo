package dev.agasen.platform.core.storage.s3;

import dev.agasen.platform.core.storage.StorageNamespace;

import java.util.Optional;

public interface S3BucketContext extends StorageNamespace {

   String bucketName();

   String region();

   // public-facing URL base, e.g. http://localhost:4566/shopbuddy-products
   String baseUrl();

   // null for real AWS, set to http://localstack:4566 for LocalStack
   Optional< String > endpointOverride();

   @Override
   default String type() {
      return "s3";
   }
}

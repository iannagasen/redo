package dev.agasen.core.product.infrastructure.s3;

import dev.agasen.common.file.FileStoragePort;
import dev.agasen.common.integrations.s3.S3FileStorageAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class S3Config {

   @Bean
   public S3Client s3Client( ProductImageBucket productImageBucket ) {
      S3ClientBuilder builder = S3Client.builder()
         .region( Region.of( productImageBucket.region() ) )
         .forcePathStyle( true );// required for LocalStack, works fine with real AWS

      if ( productImageBucket.endpointOverride().isPresent() ) {
         builder.endpointOverride( URI.create( productImageBucket.endpointOverride().get() ) )
            .credentialsProvider( StaticCredentialsProvider.create(
               AwsBasicCredentials.create( "test", "test" ) // why is this hardcoded
            ) );
      }

      return builder.build();
   }


   @Bean
   public S3Presigner s3Presigner( ProductImageBucket productImageBucket ) {
      var builder = S3Presigner.builder()
         .region( Region.of( productImageBucket.region() ) )
         .serviceConfiguration( S3Configuration.builder()
            .pathStyleAccessEnabled( true )
            .build()
         );

      if ( productImageBucket.endpointOverride().isPresent() ) {
         builder.endpointOverride( URI.create( productImageBucket.endpointOverride().get() ) )
            .credentialsProvider( StaticCredentialsProvider.create(
               AwsBasicCredentials.create( "test", "test" ) // why is this hardcoded
            ) );
      }

      return builder.build();
   }

   @Bean
   public FileStoragePort< ProductImageBucket > productImageFileStoragePort( S3Client s3Client, S3Presigner s3Presigner,
                                                                             ProductImageBucket productImageBucket ) {
      return new S3FileStorageAdapter<>( s3Client, s3Presigner, productImageBucket );
   }
}

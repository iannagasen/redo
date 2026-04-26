package dev.agasen.core.product.infrastructure.s3;

import dev.agasen.platform.core.storage.s3.S3BucketContext;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Optional;

@ConfigurationProperties( prefix = "storage.s3" )
public record ProductImageBucket(
   String bucketName,
   String region,
   String baseUrl,
   Optional< String > endpointOverride
) implements S3BucketContext {
}

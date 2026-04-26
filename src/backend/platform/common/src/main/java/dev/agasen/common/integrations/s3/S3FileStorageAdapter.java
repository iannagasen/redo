package dev.agasen.common.integrations.s3;

import dev.agasen.common.file.FileReference;
import dev.agasen.common.file.FileStoragePort;
import dev.agasen.common.file.UploadDirective;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

public class S3FileStorageAdapter< T extends S3BucketContext > implements FileStoragePort< T > {

   private final T bucketContext;
   private final S3Client s3Client;
   private final S3Presigner s3Presigner;

   public S3FileStorageAdapter( S3Client s3Client, S3Presigner s3Presigner, T bucketContext ) {
      this.s3Client = s3Client;
      this.s3Presigner = s3Presigner;
      this.bucketContext = bucketContext;
   }

   @Override
   public UploadDirective prepareUpload( String filename, Duration expiry ) {
      String key = UUID.randomUUID() + "/" + filename;

      var putRequest = PutObjectRequest.builder()
         .bucket( bucketContext.bucketName() )
         .key( key )
         .build();

      var presigned = s3Presigner.presignPutObject( r ->
         r.putObjectRequest( putRequest )
            .signatureDuration( expiry )
      );

      try {
         return new UploadDirective(
            new FileReference( key ),
            presigned.url().toURI(),
            UploadDirective.FileUploadMethod.PUT,
            Map.of( "Content-Type", "application/octet-stream" )
         );
      } catch ( URISyntaxException e ) {
         throw new RuntimeException( e );
      }
   }

   @Override
   public URI getAccessUri( FileReference fileReference ) {
      return URI.create( bucketContext.baseUrl() + "/" + fileReference.path() );
   }

   @Override
   public void delete( FileReference fileReference ) {
      s3Client.deleteObject( DeleteObjectRequest.builder()
         .bucket( bucketContext.bucketName() )
         .key( fileReference.path() )
         .build() );
   }

}

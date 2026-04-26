package dev.agasen.common.file;

import java.net.URI;
import java.time.Duration;

public interface FileStoragePort< T extends StorageNamespace > {

   UploadDirective prepareUpload( String filename, Duration expiry );

   URI getAccessUri( FileReference fileReference );

   void delete( FileReference fileReference );

}

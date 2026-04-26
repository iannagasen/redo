package dev.agasen.common.file;

import java.net.URI;
import java.util.Map;

public record UploadDirective(
   FileReference fileReference,
   URI destination,
   FileUploadMethod uploadMethod,
   Map< String, String > metadata
) {

   public enum FileUploadMethod {
      POST, PUT
   }
}

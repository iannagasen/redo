package dev.agasen.common.auth;

public interface HasAuthority {
   /**
    * @return the required authority (role/permission) for this command.
    * Example: "SCOPE_write_products"
    */
   String requiredAuthority();
}
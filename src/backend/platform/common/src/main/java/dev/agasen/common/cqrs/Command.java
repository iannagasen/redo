package dev.agasen.common.cqrs;

public interface Command< T > {

   T execute();
   
}

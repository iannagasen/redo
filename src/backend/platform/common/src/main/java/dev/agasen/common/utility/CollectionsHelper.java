package dev.agasen.common.utility;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CollectionsHelper {

   public static boolean isNullOrEmpty( final Collection< ? > collection ) {
      return collection == null || collection.isEmpty();
   }

   public static < T > List< T > listOrEmpty( final List< T > list ) {
      return list == null ? Collections.emptyList() : list;
   }
}

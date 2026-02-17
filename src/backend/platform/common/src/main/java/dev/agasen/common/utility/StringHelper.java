package dev.agasen.common.utility;

public class StringHelper {

   public static boolean isNullOrEmpty( String str ) {
      return str == null || str.isEmpty();
   }

   public static String blankCoalescing( String s1, String s2 ) {
      return isNullOrEmpty( s1 ) ? s2 : s1;
   }
}

package dev.agasen.common.pagination;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public class PagedResult< T > {
   private List< T > content;
   private int pageNumber;
   private int pageSize;
   private long totalElements;

   public List< T > getContent() {
      return content;
   }

   public void setContent( List< T > content ) {
      this.content = content;
   }

   public int getPageNumber() {
      return pageNumber;
   }

   public void setPageNumber( int pageNumber ) {
      this.pageNumber = pageNumber;
   }

   public int getPageSize() {
      return pageSize;
   }

   public void setPageSize( int pageSize ) {
      this.pageSize = pageSize;
   }

   public long getTotalElements() {
      return totalElements;
   }

   public void setTotalElements( long totalElements ) {
      this.totalElements = totalElements;
   }

   public Page< T > toPage() {
      return new PageImpl<>( content, PageRequest.of( pageNumber, pageSize ), totalElements );
   }

   public static < T > PagedResult< T > from( Page< T > page ) {
      PagedResult< T > dto = new PagedResult<>();
      dto.setContent( page.getContent() );
      dto.setPageNumber( page.getNumber() );
      dto.setPageSize( page.getSize() );
      dto.setTotalElements( page.getTotalElements() );
      return dto;
   }
}
package dev.agasen.api.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
   @NotEmpty
   @Valid
   private List<OrderItemRequest> items;
}

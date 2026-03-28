package dev.agasen.api.core.order.write;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
   @NotBlank
   private String status;
}

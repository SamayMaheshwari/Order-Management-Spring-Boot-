package practice.samay.ordermanagementsystem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "OrderResponse", description = "Order details returned in API responses")
public class OrderResponse {

    @Schema(description = "Unique order ID", example = "1")
    private Long id;

    @Schema(description = "Auto-generated order number", example = "ORD-1718000000000-4231")
    private String orderNumber;

}

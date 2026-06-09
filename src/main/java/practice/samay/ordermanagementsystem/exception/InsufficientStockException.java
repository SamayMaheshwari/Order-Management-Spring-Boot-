package practice.samay.ordermanagementsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.CONFLICT)
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String productCode, int available, int requested) {
        super(String.format(
                "Insufficient stock for product '%s'. Available: %d, Requested: %d",
                productCode, available, requested));
    }
}

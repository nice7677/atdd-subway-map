package subway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionController {

    @ExceptionHandler(SubwayException.class)
    public ResponseEntity<ErrorDTO> handleRuntimeException(SubwayException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(new ErrorDTO(exception.getMessage()));
    }
}
package bizz.addonai.users.msuserspoc.exceptions;

import graphql.GraphQLError;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@Component
@ControllerAdvice
public class GlobalExceptionHandler extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        log.error("Exception occurred: {}", ex.getMessage(), ex);

        // Validation failure (DTO constraints via @Valid)
        if (ex instanceof ConstraintViolationException cve) {
            String message = cve.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));
            return buildError(message, ErrorType.BAD_REQUEST, env);
        }

        // Controller layer exceptions
        if (ex instanceof NotFoundException) {
            return buildError(ex.getMessage(), ErrorType.NOT_FOUND, env);
        }
        if (ex instanceof ConflictException) {
            return buildError(ex.getMessage(), ErrorType.BAD_REQUEST, env);
        }
        if (ex instanceof BadRequestException) {
            return buildError(ex.getMessage(), ErrorType.BAD_REQUEST, env);
        }
        if (ex instanceof BadGatewayException) {
            return buildError("Error de comunicación con un servicio externo: " + ex.getMessage(), ErrorType.INTERNAL_ERROR, env);
        }
        if (ex instanceof InvalidUserTypeException) {
            return buildError(ex.getMessage(), ErrorType.BAD_REQUEST, env);
        }

        if (ex instanceof InternalServerErrorException) {
            return buildError("Error interno del servidor: " + ex.getMessage(), ErrorType.INTERNAL_ERROR, env);
        }

        return buildError("An unexpected error occurred: " + ex.getMessage(), ErrorType.INTERNAL_ERROR, env);
    }

    private GraphQLError buildError(String message, ErrorType errorType, DataFetchingEnvironment env) {
        return GraphQLError.newError()
                .errorType(errorType)
                .message(message)
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
    }
}

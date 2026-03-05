package bizz.addonai.users.msuserspoc.exceptions;

import graphql.GraphQLError;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Slf4j
@Component
@ControllerAdvice
public class GlobalExceptionHandler extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        log.error("Exception occurred: {}", ex.getMessage(), ex);

        if (ex instanceof UserNotFoundException) {
            return this.buildError(ex.getMessage(), ErrorType.NOT_FOUND, env);
        }

        if (ex instanceof InvalidUserTypeException) {
            return this.buildError(ex.getMessage(), ErrorType.BAD_REQUEST, env);
        }

        return this.buildError("An unexpected error occurred: " + ex.getMessage(),
                ErrorType.INTERNAL_ERROR, env);
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
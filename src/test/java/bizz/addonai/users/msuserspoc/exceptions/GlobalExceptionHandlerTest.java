package bizz.addonai.users.msuserspoc.exceptions;

import graphql.GraphQLError;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.ResultPath;
import graphql.language.Field;
import graphql.language.SourceLocation;
import graphql.schema.DataFetchingEnvironment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.graphql.execution.ErrorType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @Mock
    private DataFetchingEnvironment env;

    @Mock
    private ExecutionStepInfo stepInfo;

    @Mock
    private Field field;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();

        ResultPath path = ResultPath.parse("/userById");
        SourceLocation location = new SourceLocation(1, 1);

        when(env.getExecutionStepInfo()).thenReturn(stepInfo);
        when(stepInfo.getPath()).thenReturn(path);
        when(env.getField()).thenReturn(field);
        when(field.getSourceLocation()).thenReturn(location);
    }

    @Test
    void resolveToSingleError_userNotFound_returnsNOT_FOUND() {
        UserNotFoundException ex = new UserNotFoundException("User not found");

        GraphQLError error = handler.resolveToSingleError(ex, env);

        assertThat(error).isNotNull();
        assertThat(error.getMessage()).isEqualTo("User not found");
        assertThat(error.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
    }

    @Test
    void resolveToSingleError_invalidUserType_returnsBAD_REQUEST() {
        InvalidUserTypeException ex = new InvalidUserTypeException("Unknown user type: GUEST");

        GraphQLError error = handler.resolveToSingleError(ex, env);

        assertThat(error).isNotNull();
        assertThat(error.getMessage()).isEqualTo("Unknown user type: GUEST");
        assertThat(error.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @Test
    void resolveToSingleError_unexpectedException_returnsINTERNAL_ERROR() {
        RuntimeException ex = new RuntimeException("Something went wrong");

        GraphQLError error = handler.resolveToSingleError(ex, env);

        assertThat(error).isNotNull();
        assertThat(error.getMessage()).contains("An unexpected error occurred");
        assertThat(error.getErrorType()).isEqualTo(ErrorType.INTERNAL_ERROR);
    }

    @Test
    void resolveToSingleError_illegalArgument_returnsINTERNAL_ERROR() {
        IllegalArgumentException ex = new IllegalArgumentException("Bad input");

        GraphQLError error = handler.resolveToSingleError(ex, env);

        assertThat(error.getErrorType()).isEqualTo(ErrorType.INTERNAL_ERROR);
    }

    @Test
    void resolveToSingleError_errorContainsPath() {
        UserNotFoundException ex = new UserNotFoundException("not found");

        GraphQLError error = handler.resolveToSingleError(ex, env);

        assertThat(error.getPath()).isNotNull();
    }

    @Test
    void resolveToSingleError_errorContainsLocations() {
        UserNotFoundException ex = new UserNotFoundException("not found");

        GraphQLError error = handler.resolveToSingleError(ex, env);

        assertThat(error.getLocations()).isNotEmpty();
    }
}

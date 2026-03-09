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
        GraphQLError error = handler.resolveToSingleError(new NotFoundException("User not found"), env);

        assertThat(error.getMessage()).isEqualTo("User not found");
        assertThat(error.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
    }

    @Test
    void resolveToSingleError_notFoundBase_returnsNOT_FOUND() {
        GraphQLError error = handler.resolveToSingleError(new NotFoundException("Resource not found"), env);

        assertThat(error.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
    }

    @Test
    void resolveToSingleError_conflictException_returnsBAD_REQUEST() {
        GraphQLError error = handler.resolveToSingleError(new ConflictException("Email already registered"), env);

        assertThat(error.getMessage()).isEqualTo("Email already registered");
        assertThat(error.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @Test
    void resolveToSingleError_invalidUserType_returnsBAD_REQUEST() {
        GraphQLError error = handler.resolveToSingleError(new InvalidUserTypeException("Unknown user type: GUEST"), env);

        assertThat(error.getMessage()).isEqualTo("Unknown user type: GUEST");
        assertThat(error.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @Test
    void resolveToSingleError_badRequestBase_returnsBAD_REQUEST() {
        GraphQLError error = handler.resolveToSingleError(new BadRequestException("Invalid input"), env);

        assertThat(error.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @Test
    void resolveToSingleError_badGatewayException_returnsINTERNAL_ERROR() {
        GraphQLError error = handler.resolveToSingleError(
                new BadGatewayException("DB unreachable", new RuntimeException("timeout")), env);

        assertThat(error.getMessage()).contains("Error de comunicación con un servicio externo");
        assertThat(error.getErrorType()).isEqualTo(ErrorType.INTERNAL_ERROR);
    }

    @Test
    void resolveToSingleError_internalServerError_returnsINTERNAL_ERROR() {
        GraphQLError error = handler.resolveToSingleError(
                new InternalServerErrorException("Unexpected failure", new RuntimeException("npe")), env);

        assertThat(error.getMessage()).contains("Error interno del servidor");
        assertThat(error.getErrorType()).isEqualTo(ErrorType.INTERNAL_ERROR);
    }

    @Test
    void resolveToSingleError_unknownException_returnsINTERNAL_ERROR() {
        GraphQLError error = handler.resolveToSingleError(new RuntimeException("Something went wrong"), env);

        assertThat(error.getMessage()).contains("An unexpected error occurred");
        assertThat(error.getErrorType()).isEqualTo(ErrorType.INTERNAL_ERROR);
    }

    @Test
    void resolveToSingleError_errorContainsPath() {
        GraphQLError error = handler.resolveToSingleError(new NotFoundException("not found"), env);
        assertThat(error.getPath()).isNotNull();
    }

    @Test
    void resolveToSingleError_errorContainsLocations() {
        GraphQLError error = handler.resolveToSingleError(new NotFoundException("not found"), env);
        assertThat(error.getLocations()).isNotEmpty();
    }
}

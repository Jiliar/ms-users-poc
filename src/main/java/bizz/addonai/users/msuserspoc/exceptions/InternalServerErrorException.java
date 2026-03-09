package bizz.addonai.users.msuserspoc.exceptions;

public class InternalServerErrorException extends RuntimeException {
    public InternalServerErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}

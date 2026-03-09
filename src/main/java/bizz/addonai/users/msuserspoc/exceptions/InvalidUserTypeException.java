package bizz.addonai.users.msuserspoc.exceptions;

public class InvalidUserTypeException extends BadRequestException {
    public InvalidUserTypeException(String message) {
        super(message);
    }
}

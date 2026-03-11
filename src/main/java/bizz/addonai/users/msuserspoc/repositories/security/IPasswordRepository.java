package bizz.addonai.users.msuserspoc.repositories.security;

public interface IPasswordRepository {

    String encryptPassword(String plainPassword);
    boolean verifyPassword(String plainPassword, String storedHash);
    String generateTemporaryPassword(int length);
    boolean needsRehash(String storedHash);
}

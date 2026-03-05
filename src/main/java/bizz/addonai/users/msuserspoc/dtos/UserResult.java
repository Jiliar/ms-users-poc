package bizz.addonai.users.msuserspoc.dtos;

/**
 * Interface marker para la union GraphQL: union UserResult = AdminUser | RegularUser
 *
 * Permite que Spring GraphQL resuelva el tipo concreto en runtime
 * basándose en los campos solicitados por el cliente.
 */
public interface UserResult {

}
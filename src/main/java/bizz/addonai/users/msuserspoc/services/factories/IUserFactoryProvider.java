package bizz.addonai.users.msuserspoc.services.factories;

import bizz.addonai.users.msuserspoc.models.enums.UserType;
import bizz.addonai.users.msuserspoc.services.factories.users.IUserFactory;

public interface IUserFactoryProvider {

    IUserFactory getFactory(UserType userType);
    boolean supports(UserType userType);
}

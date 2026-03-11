package bizz.addonai.users.msuserspoc.services.factories;

import bizz.addonai.users.msuserspoc.models.enums.UserType;
import bizz.addonai.users.msuserspoc.services.factories.impl.UserFactory;

public interface IUserFactoryProvider {

    UserFactory getFactory(UserType userType);
    boolean supports(UserType userType);
}

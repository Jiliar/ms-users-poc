package bizz.addonai.users.msuserspoc.services.factories;

import bizz.addonai.users.msuserspoc.services.factories.impl.UserFactory;

public interface IUserFactoryProvider {

    UserFactory getFactory(String userType);
    boolean supports(String userType);
}

package bizz.addonai.users.msuserspoc.services.factories.impl;

import bizz.addonai.users.msuserspoc.exceptions.InvalidUserTypeException;
import bizz.addonai.users.msuserspoc.services.factories.IUserFactoryProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UserFactoryProviderImpl implements IUserFactoryProvider {

    private final Map<String, UserFactory> factories;

    public UserFactoryProviderImpl(List<UserFactory> factoryList) {
        this.factories = factoryList.stream()
                .collect(Collectors.toMap(
                        UserFactory::getFactoryType,
                        factory -> factory
                ));
    }

    /**
     * Obtiene la fábrica apropiada según el tipo de usuario
     * Este es el punto de entrada del patrón Abstract Factory
     */
    public UserFactory getFactory(String userType) {
        UserFactory factory = factories.get(userType.toUpperCase());
        if (factory == null) {
            throw new InvalidUserTypeException("Unknown user type: " + userType);
        }
        return factory;
    }

    /**
     * Verifica si existe una fábrica para el tipo dado
     */
    public boolean supports(String userType) {
        return factories.containsKey(userType.toUpperCase());
    }
}
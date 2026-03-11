package bizz.addonai.users.msuserspoc.services.factories.impl;

import bizz.addonai.users.msuserspoc.exceptions.InvalidUserTypeException;
import bizz.addonai.users.msuserspoc.models.enums.UserType;
import bizz.addonai.users.msuserspoc.services.factories.IUserFactoryProvider;
import bizz.addonai.users.msuserspoc.services.factories.users.IUserFactory;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UserFactoryProviderImpl implements IUserFactoryProvider {

    private final Map<String, IUserFactory> factories;

    public UserFactoryProviderImpl(List<IUserFactory> factoryList) {
        this.factories = factoryList.stream()
                .collect(Collectors.toMap(
                        IUserFactory::getFactoryType,
                        factory -> factory
                ));
    }

    /**
     * Obtiene la fábrica apropiada según el tipo de usuario
     * Este es el punto de entrada del patrón Abstract Factory
     */
    public IUserFactory getFactory(UserType userType) {
        IUserFactory factory = factories.get(userType.toString().toUpperCase());
        if (factory == null) {
            throw new InvalidUserTypeException("Unknown user type: " + userType);
        }
        return factory;
    }

    /**
     * Verifica si existe una fábrica para el tipo dado
     */
    public boolean supports(UserType userType) {
        return factories.containsKey(userType.toString().toUpperCase());
    }
}
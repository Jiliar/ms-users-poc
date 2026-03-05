package bizz.addonai.users.msuserspoc.config;

import graphql.scalars.ExtendedScalars;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Configuration
public class GraphQLConfig {

    /**
     * Registra el scalar UUID para que GraphQL pueda parsear/serializar
     * valores UUID entre Java y el schema
     */
    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
                .scalar(ExtendedScalars.UUID)  // UUID scalar
                .build();
    }
}
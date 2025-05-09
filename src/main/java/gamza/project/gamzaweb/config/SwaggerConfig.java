package gamza.project.gamzaweb.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfig {

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme().type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }

    private OpenApiCustomizer createOpenApiCustomizer(String title, String version) {
        return openApi -> {
            openApi.info(new Info().title(title).version(version));
            openApi.schemaRequirement("bearerAuth", createAPIKeyScheme());
            openApi.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
            openApi.addServersItem(new Server().url("https://api.gamza.club").description("Production Server"));
            openApi.addServersItem(new Server().url("https://gamzaweb.shop/").description("TestServer Server"));
        };
    }

    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("all")
                .pathsToMatch("/**")
                .displayName("All API")
                .addOpenApiCustomizer(createOpenApiCustomizer("모든 API", "v0.5"))
                .build();
    }
}
package practice.samay.ordermanagementsystem.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI orderManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Management System API")
                        .description("RESTful API for managing the complete e-commerce order lifecycle.\n\n" +
                                "**Lifecycle:** Create Order -> Process Payment -> Update Inventory -> Ship Order -> Deliver Order\n\n" +
                                "**Modules:**\n" +
                                "- **Orders** - Create and manage customer orders with inventory validation\n" +
                                "- **Payments** - Process and track payments with multiple payment methods\n" +
                                "- **Shipments** - Create and update shipments with carrier tracking\n" +
                                "- **Tracking** - Add real-time tracking events for shipments")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Samay")
                                .email("samay@ordermanagement.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .externalDocs(new ExternalDocumentation()
                        .description("Order Management System – Full Documentation")
                        .url("https://github.com/samay/order-management-system"));
    }
}

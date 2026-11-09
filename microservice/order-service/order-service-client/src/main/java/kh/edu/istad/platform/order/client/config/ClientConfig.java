package kh.edu.istad.platform.order.client.config;

import kh.edu.istad.platform.order.client.exception.DownStreamServiceException;
import kh.edu.istad.platform.order.client.httpclient.BusinessHttpClient;
import kh.edu.istad.platform.order.client.httpclient.CustomerHttpClient;
import kh.edu.istad.platform.order.domain.entity.Business;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.reactive.function.client.support.WebClientHttpServiceGroupConfigurer;
import org.springframework.web.service.invoker.HttpExchangeAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.service.registry.HttpServiceGroup;
import org.springframework.web.service.registry.ImportHttpServices;


@Configuration
@ImportHttpServices(
        group = "customer-service",
        types = {CustomerHttpClient.class},
        clientType = HttpServiceGroup.ClientType.WEB_CLIENT
)
@ImportHttpServices(
        group = "business-service",
        types = {BusinessHttpClient.class},
        clientType = HttpServiceGroup.ClientType.WEB_CLIENT
)

public class ClientConfig {

    @Bean
    public WebClientHttpServiceGroupConfigurer webClientHttpServiceGroupConfigurer(ExchangeFilterFunction customerOAuth){
        return groups -> {
            groups.filterByName("customer-service")
                    .forEachClient(
                            (group, clientBuilder) ->
                                    clientBuilder
                                            .baseUrl("http://localhost:9090")
                                            .filter(customerOAuth)
                                            .defaultHeader("User-Agent", "Web Client")
                                            .defaultStatusHandler(
                                                    HttpStatusCode::isError,
                                                    response -> {
                                                        return response
                                                                .createException()
                                                                .map(
                                                                        ex ->
                                                                                new DownStreamServiceException(
                                                                                        ex.getMessage(),
                                                                                        "customer-service"
                                                                                )

                                                        );
                                                    }
                                            )
                    );
        };
    }

































}

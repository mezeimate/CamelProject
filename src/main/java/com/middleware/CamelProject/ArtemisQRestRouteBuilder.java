package com.middleware.CamelProject;

import com.middleware.CamelProject.model.RequestUser;
import com.middleware.CamelProject.model.SecurityUser;
import com.middleware.CamelProject.model.User;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class ArtemisQRestRouteBuilder extends RouteBuilder {

    @Value("${message.inputQueue}")
    private String inputQueue;

    @Value("${message.outputQueue}")
    private String outputQueue;

    @Value("${backendService.url}")
    private String backendUrl;

    @Override
    public void configure() {
        from("jms:queue:" + inputQueue ).routeId("generate-route")
                .streamCaching()

                .log("Input JSON Message: ${body}")

                .unmarshal().json(JsonLibrary.Jackson, RequestUser.class)

                .process(exchange -> {
                    RequestUser request = exchange.getIn().getBody(RequestUser.class);

                    User backendRequest = new User();
                    backendRequest.setFirstName(request.getFirstName());
                    backendRequest.setLastName(request.getLastName());
                    backendRequest.setAge(request.getAge());

                    SecurityUser securityUser = new SecurityUser();
                    securityUser.setUsername(request.getUsername());
                    securityUser.setPassword(request.getPassword());
                    backendRequest.setSecurityUser(securityUser);

                    exchange.getIn().setBody(backendRequest);
                })

                .log("Transformed JSON Message: ${body}")

                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE))
                .convertBodyTo(String.class)

                .to(backendUrl)

                .log("HTTP Response Message: ${body}")

                .to("jms:queue:" + outputQueue);


        from("jms:queue:" + outputQueue ).routeId("receive-route")
                .log("OUT Queue message: ${body}");

    }
}

package com.middleware.CamelProject;

import com.middleware.CamelProject.model.RequestUser;
import com.middleware.CamelProject.model.SecurityUser;
import com.middleware.CamelProject.model.User;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
public class ArtemisQRestRouteBuilder extends RouteBuilder {

    @Override
    public void configure() {
        from("jms:queue:TEST_IN").routeId("generate-route")
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

                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .convertBodyTo(String.class)

                .to("http://localhost:8091/accountcreation?httpMethod=POST")

                .log("HTTP Response Message: ${body}")

                .to("jms:queue:TEST_OUT");


        from("jms:queue:TEST_OUT").routeId("receive-route")
                .log("OUT Queue message: ${body}");

    }
}

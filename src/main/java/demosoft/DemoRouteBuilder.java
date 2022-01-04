package demosoft;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

import javax.enterprise.context.ApplicationScoped;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.amqp;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.timer;

@ApplicationScoped
public class DemoRouteBuilder extends RouteBuilder {

    @Override
    public void configure() {

        from(timer("demo").repeatCount(1))
                .routeId("timer-dsl-route")
                .setBody(simple("Message #${exchangeProperty.CamelTimerCounter}"))
                .log("${body}")
                .to("amqp:queue:{{queue.name}}")
                .log("message sent to queue ${body}")
        ;

        from(amqp("queue:{{queue.name}}").transacted(true).getUri())
                .routeId("amqp-redelivery-route")
                .to("log:info?showHeaders=true")
                .choice()
                .when(e -> Integer.parseInt(e.getIn().getHeader("JMSXDeliveryCount").toString()) < 2)
                .throwException(new RuntimeException("nopes:" + System.nanoTime())).endChoice()
                .otherwise()
                .process(e -> {
                    final Long jmsTimestamp = e.getIn().getHeader("JMSTimestamp", Long.class);
                    final long processTimeInMillis = System.currentTimeMillis() - jmsTimestamp;
                    e.getIn().setHeader("processTime", processTimeInMillis);
                })
                .log(LoggingLevel.ERROR, "got from amqp: ${body}, took ${headers.processTime} ms")
        ;
    }
}

package demosoft;

import org.apache.camel.CamelContext;
import org.apache.camel.component.amqp.AMQPComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.qpid.jms.JmsConnectionFactory;

import javax.jms.ConnectionFactory;

public final class CamelMain {

    public static void main(String[] args) throws Exception {
        try (CamelContext camel = new DefaultCamelContext()) {
            camel.getPropertiesComponent().setLocation("classpath:application.properties");

            String amqpUri = camel.resolvePropertyPlaceholders("{{quarkus.qpid-jms.url}}");
            ConnectionFactory factory = new JmsConnectionFactory(amqpUri);
            camel.addComponent("amqp", new AMQPComponent(factory));
            camel.addRoutes(new DemoRouteBuilder());

            // start is not blocking
            camel.start();

            // so run for 7 seconds
            Thread.sleep(7_000);

            // and then stop nicely
            camel.stop();
        }
    }
}
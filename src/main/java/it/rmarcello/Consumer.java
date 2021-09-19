package it.rmarcello;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

/**
 * A bean consuming prices from the JMS queue.
 */
@ApplicationScoped
public class Consumer implements Runnable {

    private static final Logger LOG = Logger.getLogger(Consumer.class);

    @ConfigProperty(name = "pusher.consumerEnabled") 
    boolean consumerEnabled;

    @ConfigProperty(name = "pusher.queue") 
    String queueName;

    @ConfigProperty(name = "pusher.initial-delay")
    long initialDelay;

    @ConfigProperty(name = "pusher.delay")
    long delay;

    @Inject
    ConnectionFactory connectionFactory;

    private final ExecutorService scheduler = Executors.newSingleThreadExecutor();


    void onStart(@Observes StartupEvent ev) {
        if(consumerEnabled) {
            LOG.info("Consumer enabled="+consumerEnabled + " on queue: " + queueName);
            scheduler.submit(this);
        } else {
            LOG.info("Consumer is not enabled");
        }
    }

    void onStop(@Observes ShutdownEvent ev) {
        scheduler.shutdown();
    }

    @Override
    public void run() {
        try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
            JMSConsumer consumer = context.createConsumer(context.createQueue(queueName));
            while (true) {
                try {
                    LOG.debug("Consumer reading...");
                    Message message = consumer.receive();
                    if (message == null) return;
                    String msg = message.getBody(String.class);
                    LOG.info("Consumer: received: " + msg);
                } catch (JMSException e) {
                    LOG.error(e.getMessage(),e);
                }
            }
        }        
    }
}

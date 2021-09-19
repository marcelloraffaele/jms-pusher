package it.rmarcello;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.Session;

import com.github.javafaker.Faker;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

/**
 * A bean producing random prices every 5 seconds and sending them to the prices JMS queue.
 */
@ApplicationScoped
public class Pusher implements Runnable {

    private static final Logger LOG = Logger.getLogger(Pusher.class);

    @ConfigProperty(name = "pusher.producerEnabled") 
    boolean producerEnabled;

    @ConfigProperty(name = "pusher.queue") 
    String queueName;

    @ConfigProperty(name = "pusher.initial-delay")
    long initialDelay;

    @ConfigProperty(name = "pusher.delay")
    long delay;

    @ConfigProperty(name = "pusher.messageLenght")
    int messageLenght;

    @Inject
    ConnectionFactory connectionFactory;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    void onStart(@Observes StartupEvent ev) {
        if(producerEnabled) {
            LOG.info("Pusher is starting on queue: "+queueName+", initialDelay:"+initialDelay+", delay:"+delay);
            scheduler.scheduleWithFixedDelay(this, initialDelay, delay, TimeUnit.MILLISECONDS);
        } else {
            LOG.info("Producer is not enabled");
        }
    }

    void onStop(@Observes ShutdownEvent ev) {
        scheduler.shutdown();
    }

    @Override
    public void run() {
        try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
            final String msg = createRandomString(messageLenght) ;
            context.createProducer().send(context.createQueue( queueName ), msg );
            LOG.info("sent a message: " + msg);
        }
    }

    public String createRandomString(int size) {
        Faker faker = new Faker(new Locale("it"));
        return faker.lorem().fixedString(size);
    }
}
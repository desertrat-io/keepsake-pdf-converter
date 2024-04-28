import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

@ApplicationScoped
public class KafkaResource {

    @Incoming("convert-pdf-to-jpeg")
    public void receive(String message) {
        System.out.println("Received message: " + message);
    }
}

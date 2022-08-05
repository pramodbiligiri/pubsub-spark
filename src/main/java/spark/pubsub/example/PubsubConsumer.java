package spark.pubsub.example;

import com.google.api.core.ApiService;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PubsubConsumer {

    public static void main(String... args) throws Exception {
        String projectId = "redacted";
        String subscriptionId = "redacted";
        subscribeAsyncExample(projectId, subscriptionId);
    }

    public static void subscribeAsyncExample(String projectId, String subscriptionId) {
        ProjectSubscriptionName subscriptionName =
                ProjectSubscriptionName.of(projectId, subscriptionId);

        // Instantiate an asynchronous message receiver.
        MessageReceiver receiver =
                (PubsubMessage message, AckReplyConsumer consumer) -> {
                    // Handle incoming message, then ack the received message.
                    System.out.println("message id: " + message.getMessageId());
                    System.out.println("message data: " + message.getData().toStringUtf8());
//                        consumer.ack();
                };

        Subscriber subscriber = null;

        try {
            subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();

            // Start the subscriber.
            ApiService apiService = subscriber.startAsync();
            apiService.awaitRunning();
            System.out.printf("Listening for messages on %s:\n", subscriptionName.toString());

            // Log the state of subscriber every 30 secs
            Executors.newScheduledThreadPool(1).scheduleAtFixedRate(
                    () -> System.out.println("subscriber state: " + apiService.state()),
                    10, 30, TimeUnit.SECONDS);

            subscriber.awaitTerminated(30, TimeUnit.MINUTES);
        } catch (TimeoutException timeoutException) {
            timeoutException.printStackTrace();
            subscriber.stopAsync();
        }
    }
}

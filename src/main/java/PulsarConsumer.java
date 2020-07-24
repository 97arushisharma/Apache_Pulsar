import org.apache.pulsar.client.api.*;
import org.apache.pulsar.client.impl.auth.AuthenticationTls;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

// Command to run consumer:
// $ java -cp pulsar-1.0-SNAPSHOT-jar-with-dependencies.jar PulsarConsumer topic-name subscription-name service-url


public class PulsarConsumer {



    static String pulsarBrokerRootUrl = "pulsar+ssl://127.0.0.1:6651";
    static String topic = "my-topic"; // from above
    static String subscription = "my-subscription";
    static String props_file = null;



    public static void read() throws PulsarClientException {

        Properties props = new Properties();
        try {
            FileReader reader = new FileReader(props_file);
            props.load(reader);
            reader.close();
        } catch (Exception e){
            e.printStackTrace();
        }

        Map<String, String> authParams = new HashMap<>();
        authParams.put("tlsCertFile", props.getProperty("tlsCertFile"));
        authParams.put("tlsKeyFile", props.getProperty("tlsKeyFile"));

        Authentication tlsAuth = AuthenticationFactory
                .create(AuthenticationTls.class.getName(), authParams);

        PulsarClient client = PulsarClient.builder()
                .serviceUrl(props.getProperty("serviceUrl"))
                .enableTls(true)
                .tlsTrustCertsFilePath("/home/sqlstream/Downloads/my-ca/certs/ca.cert.pem")
                .authentication(tlsAuth)
                .build();
        //(pulsarBrokerRootUrl);

        Consumer consumer = client.newConsumer()
                .topic(props.getProperty("topicName"))
                .subscriptionName(subscription)
                .cryptoKeyReader(new RawKeyFile(props.getProperty("privateKeyFile"), props.getProperty("publicKeyFile")))
                .subscribe();

        while (true) {
            // Wait for a message
            Message msg = consumer.receive();

            try {
                // Do something with the message
                System.out.printf("\nMessage received: %s", new String(msg.getData()));
                System.out.println("\nMessage Timestamp: "+ (long)msg.getEventTime());
                System.out.println("\nMessage SequenceId: "+ (long)msg.getSequenceId());
                System.out.println("\nMessage Redelivery Count: "+ msg.getRedeliveryCount());
                System.out.println("\nMessage MessageId: "+ msg.getMessageId());



                // Acknowledge the message so that it can be deleted by the message broker
                consumer.acknowledge(msg);
            } catch (Exception e) {
                // Message failed to process, redeliver later
                consumer.negativeAcknowledge(msg);
            }
        }
    }

    public static void main(String args[]) throws PulsarClientException {

        int len = args.length;
        if(len > 0 ){
            props_file = args[0];
        } else {
            throw new NullPointerException("No property file");
        }

        read();
    }
}

import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

// Command to run producer:
// $ java -cp pulsar-1.0-SNAPSHOT-jar-with-dependencies.jar PulsarProducer

public class PulsarProducer {

    public static Producer<byte[]> producer;
    public static void main(String args[]){

        try {
            PulsarClient client = PulsarClient.builder()
                    .serviceUrl("pulsar://localhost:6650")
                    .build();

            producer = client.newProducer()
                    .topic("my-topic")
                    .create();

            for(int i=0;i<10;i++){
                producer.send(("test_"+i).getBytes());
            }

            producer.close();
            client.close();
        }
        catch(PulsarClientException e){
            System.out.println("Exception "+ e.toString());
        }
        // You can then send messages to the broker and topic you specified:


    }
}

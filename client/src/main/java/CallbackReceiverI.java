import Demo.Callback;
import Demo.Response;
import com.zeroc.Ice.Current;

public class CallbackReceiverI implements Callback {
    @Override
    public void reportResponse(Response response, Current __current) {
        System.out.println("Received message from server: " + response.value);
    }
}
import Demo.Callback;
import Demo.Response;
import com.zeroc.Ice.Current;

public class CallbackReceiverI implements Callback {
    @Override
    public void reportResponse(Response response, Current current) {
        System.out.println("Received callback: " + response.value);
    }
}
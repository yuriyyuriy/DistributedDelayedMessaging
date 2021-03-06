import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

/**
 * Thread class for managing the different connections that come in. Takes a socket and starts reading the commands from it
 * Which then resends that command to all other nodes
 */
public class SequencerThread implements Runnable{


    protected Socket socket = null;
    protected ConfigurationFile config = null;
    protected MessageGenerator generator = null;
    protected DelayQueue<DelayedServerMessage> queue = null;

    public SequencerThread(Socket s, ConfigurationFile con, MessageGenerator g, DelayQueue<DelayedServerMessage> q) {
        socket = s;
        config = con;
        generator = g;
        queue = q;
    }

    public void run() {
        try {

            BufferedReader _in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String fullMessage = _in.readLine();
            String message = fullMessage.split("::")[0];
            Command command = new Command(fullMessage.split("::")[0]);
            long timestamp = command.getTimestamp();
            int maxDelay = config.findInfoByIdentifier(command.getOrigin()).getPortDelay();
            System.out.println("Received '" + command.toString() + "' from " + command.getOrigin() + ", Max delay is " + maxDelay + " s, system time is " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));

            ArrayList<DelayedServerMessage> messages = generator.GenerateMessageFromCommand(command);
            queue.addAll(messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

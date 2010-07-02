package expectj;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Accepts connections on a port and closes them immediately.
 *
 * @author johan.walles@gmail.com
 */
class ConnectionsDropper {
    /**
     * The server socket we're listening on.
     */
    private ServerSocket listener;

    /**
     * Create a new connections dropper.
     *
     * @throws IOException If we're unable to open a port to listen to.
     */
    public ConnectionsDropper() throws IOException {
        listener = new ServerSocket(0);

        String threadName =
            "Connections dropper , (port " + listener.getLocalPort() + ")";
        new Thread(new Runnable() {
            public void run() {
                try {
                    while (true) {
                        Socket incoming = listener.accept();
                        incoming.close();
                    }
                } catch (IOException e) {
                    // Just quit listening if we fail, it probably means
                    // somebody closed our socket on purpose.
                }
            }
        }, threadName).start();
    }

    /**
     * Which port are we listening to?
     *
     * @return The local port we're listening to.
     */
    public int getListeningPort() {
        return listener.getLocalPort();
    }

    /**
     * Stop listening for connections.
     *
     * @throws IOException if we can't stop listening
     */
    public void close() throws IOException {
        listener.close();
    }
}
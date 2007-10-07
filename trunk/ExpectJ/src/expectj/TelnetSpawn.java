package expectj;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * A Spawnable for controlling a telnet session using ExpectJ.
 * @author Johan Walles
 */
public class TelnetSpawn implements Spawnable
{
    private String m_remoteHostName;
    private InetAddress m_remoteHost;
    private int m_remotePort;
    private Socket m_socket;
    private InputStream m_input;
    private OutputStream m_output;
    
    /**
     * Construct a new telnet spawn.
     * @param remoteHost The remote host to connect to.
     * @param remotePort The remote port to connect to.
     * @throws UnknownHostException If the name of the remote host cannot be looked up
     */
    public TelnetSpawn(String remoteHost, int remotePort) throws UnknownHostException {
        m_remoteHostName = remoteHost;
        m_remotePort = remotePort;
        
        m_remoteHost = InetAddress.getByName(m_remoteHostName);
    }
    
    public void start()
    throws IOException
    {
        m_socket = new Socket(m_remoteHost, m_remotePort);
        m_input = m_socket.getInputStream();
        m_output = m_socket.getOutputStream();
    }

    public InputStream getInputStream() {
        return m_input;
    }

    public OutputStream getOutputStream() {
        return m_output;
    }

    public InputStream getErrorStream()
    {
        return null;
    }

    public boolean isClosed() {
        if (m_socket != null) {
            if (m_socket.isClosed()) {
                // We've been disconnected, shut down
                stop();
            }
        }
        return m_socket == null;
    }

    public int getExitValue() {
        return 0;
    }

    public void stop() {
        if (m_socket == null) {
            return;
        }
        
        try {
            m_socket.close();
        } catch (IOException ignored) {
            // Failure: When your best just isn't good enough.
        }
        m_socket = null;
        m_input = null;
        m_output = null;
    }
}

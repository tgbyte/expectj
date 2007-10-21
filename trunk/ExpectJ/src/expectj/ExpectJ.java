package expectj;

import java.net.UnknownHostException;

/**
 * This Class is the starting point of the ExpectJ Utility. This class
 * acts as factory for all Spawns.
 *
 * @author	Sachin Shekar Shetty  
 */
public class ExpectJ {

    /** Default timeout, -1 indicating wait for indefinite time */
    private long m_lDefaultTimeOutSeconds = -1;

    /**
     * @param sLogFile Location of the log file, or null for no logging
     * @param lDefaultTimeOutSeconds default time out in seconds for the expect commands
     * on the spawned process.  -1 default time out indicates indefinite
     * timeout
     */
    public ExpectJ(String sLogFile, long lDefaultTimeOutSeconds) {
        if (sLogFile != null) {
            new Debugger(sLogFile, ExpectJ.class);
        }
        
        m_lDefaultTimeOutSeconds = lDefaultTimeOutSeconds;                
    }

    /**
     * This method spawns a spawnable and returns a object representing
     * the SpawnedProcess. Further expect commands can be invoked on the
     * SpawnedProcess Object. 
     *
     * @param spawnable spawnable to be executed
     * @return The newly spawned process
     * @throws Exception if the spawning fails
     */
    public SpawnedProcess spawn(Spawnable spawnable) throws Exception {
        return new SpawnedProcess(spawnable, m_lDefaultTimeOutSeconds);
    }
    
    /**
     * This method spawns a process and returns a object representing
     * the SpawnedProcess. Further expect commands can be invoked on the
     * SpawnedProcess Object. 
     *
     * @param sCommand command to be executed
     * @return The newly spawned process
     * @throws Exception if the process spawning fails
     * @see Runtime#exec(String)
     */
    public SpawnedProcess spawn(String sCommand) throws Exception {
        return spawn(new ProcessSpawn(sCommand));
    }

    /**
     * This method spawns a process and returns a object representing
     * the SpawnedProcess. Further expect commands can be invoked on the
     * SpawnedProcess Object. 
     *
     * @param sCommandLine command to be executed
     * @return The newly spawned process
     * @throws Exception if the process spawning fails
     * @see Runtime#exec(String[])
     */
    public SpawnedProcess spawn(String sCommandLine[]) throws Exception {
        return spawn(new ProcessSpawn(sCommandLine));
    }
    
    /**
     * This method spawns a telnet connection to the given host and port number.
     * @param hostName The name of the host to connect to.
     * @param port The remote port to connect to.
     * @return The newly spawned telnet session.
     * @throws Exception if the telnet spawning fails
     * @throws UnknownHostException if you specify a bogus host name
     */
    public SpawnedProcess spawn(String hostName, int port)
    throws Exception, UnknownHostException
    {
        return spawn(new TelnetSpawn(hostName, port));
    }
}

package expectj;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
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
     * @throws FileNotFoundException if the log file cannot be created
     */
    public ExpectJ(String sLogFile, long lDefaultTimeOutSeconds) 
    throws FileNotFoundException
    {
        if (sLogFile != null) {
            Debugger.initialize(new PrintWriter(sLogFile));
        }
        
        m_lDefaultTimeOutSeconds = lDefaultTimeOutSeconds;                
    }

    /**
     * @param logWriter Log messages will be written to here, or null for no logging
     * @param lDefaultTimeOutSeconds default time out in seconds for the expect commands
     * on the spawned process.  -1 default time out indicates indefinite
     * timeout
     * @throws FileNotFoundException if the log file cannot be created
     */
    public ExpectJ(PrintWriter logWriter, long lDefaultTimeOutSeconds) 
    throws FileNotFoundException
    {
        if (logWriter != null) {
            Debugger.initialize(logWriter);
        }
        
        m_lDefaultTimeOutSeconds = lDefaultTimeOutSeconds;                
    }
    
    /**
     * Create a new ExpectJ with no logging and infinite timeout.
     */
    public ExpectJ() {
        // This constructor intentionally left blank
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
    public SpawnedProcess spawn(final String sCommand) throws Exception {
        return spawn(new ProcessSpawn(new Executor() {
            public Process execute() 
            throws IOException
            {
                return Runtime.getRuntime().exec(sCommand);
            }
            
            public String toString() {
                return sCommand;
            }
        }));
    }

    /**
     * This method spawns a process and returns a object representing
     * the SpawnedProcess. Further expect commands can be invoked on the
     * SpawnedProcess Object. 
     *
     * @param executor Will be called upon to start the new process
     * @return The newly spawned process
     * @throws Exception if the process spawning fails
     * @see Runtime#exec(String[])
     */
    public SpawnedProcess spawn(Executor executor)
    throws Exception
    {
        return spawn(new ProcessSpawn(executor));
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

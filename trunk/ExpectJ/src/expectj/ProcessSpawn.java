package expectj;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

/**
 * This class spawns a process that ExpectJ can control. 
 *
 * @author Sachin Shekar Shetty  
 * @author Johan Walles
 */
class ProcessSpawn 
implements Spawnable 
{
    /**
     * The spawned process.
     */
    private ProcessThread processThread = null;

    private Debugger debug = new Debugger(ProcessSpawn.class, true);
    
    /**
     * This constructor allows to run a process with indefinate time-out
     * @param commandLine process command to be executed 
     */
    ProcessSpawn (String commandLine) {
        if (commandLine == null || commandLine.trim().equals("")) {
            throw new IllegalArgumentException("Command is null/empty");
        }

        // Initialise the process thread.
        processThread = new ProcessThread(commandLine);
    }

    /**
     * This constructor allows to run a process with indefinate time-out
     * @param commandLine process command to be executed 
     */
    ProcessSpawn (String commandLine[]) {
        if (commandLine == null || commandLine.length == 0) {
            throw new IllegalArgumentException("Command is null/empty");
        }

        // Initialise the process thread.
        processThread = new ProcessThread(commandLine);
    }
    
    /**
     * This method stops the spawned process.
     */
    public void stop() {
        processThread.stop();
    }

    /**
     * This method executes the given command within the specified time
     * limit. It starts the process thread and also the timer when
     * enabled. It starts the piped streams to enable copying of process
     * stream contents to standard streams.
     * @throws IOException on trouble launching the process
     */
    public void start() throws IOException {
        // Start the process
        processThread.start();
    }

    /**
     * @return the input stream of the process.
     */
    public InputStream getInputStream() {
        return processThread.process.getInputStream();
    }

    /**
     * @return the output stream of the process.
     */
    public OutputStream getOutputStream() {
        return processThread.process.getOutputStream();
    }

    /**
     * @return the error stream of the process.
     */
    public InputStream getErrorStream() {
        return processThread.process.getErrorStream();
    }

    /**
     * @return true if the process has exited.
     */
    public boolean isClosed() {
        return processThread.isClosed;
    }

    /**
     * If the process representes by this object has already exited, it
     * returns the exit code. isClosed() should be used in conjunction
     * with this method.
     * @return The exit code of the finished process.
     * @throws ExpectJException if the process is still running.
     */
    public int getExitValue() 
    throws ExpectJException
    {
        if (!isClosed()) {
            throw new ExpectJException("Process is still running");
        }
        return processThread.exitValue;
    }

    /**
     * This class is responsible for executing the process in a seperate
     * thread.
     */
    class ProcessThread implements Runnable {
        /**
         * The command to be executed
         */
        private String commandLineString = null;

        /**
         * The command to be executed
         */
        private String commandLineArray[] = null;
        
        /**
         * Process object for execution of the commandLine
         */
        private Process process = null;

        /** 
         * Thread object to run this file
         */
        private Thread thread = null;

        /**
         * true if the process is done executing
         */
        private volatile boolean isClosed = false;

        /**
         * The exit value of the process if it is done executing
         */
        private int exitValue;
        
        /**
         * Create a new process thread with the given command line.
         * @param commandLine The process' command line.
         */
        ProcessThread(String commandLine) {
            if (commandLine == null) {
                throw new NullPointerException("Command line must not be null");
            }
            this.commandLineString = commandLine;
        }

        /**
         * Create a new process thread with the given command line.
         * @param commandLine The process' command line.
         */
        ProcessThread(String commandLine[]) {
            if (commandLine == null) {
                throw new NullPointerException("Command line must not be null");
            }
            if (commandLine.length == 0) {
                throw new IllegalArgumentException("Command line must not be empty");
            }
            this.commandLineArray = commandLine;
        }
        
        /**
         * This method spawns the thread and runs the process within the
         * thread
         * @throws IOException if process spawning fails
         */
        public void start() throws IOException {
            debug.print("Process Started at:" + new Date());
            thread = new Thread(this); 
            if (commandLineString != null) {
                process = Runtime.getRuntime().exec(commandLineString);
            } else {
                process = Runtime.getRuntime().exec(commandLineArray);
            }
            thread.start();
        }

        /**
         * Wait for the process to finish
         */
        public void run() {
            try {
                process.waitFor();
                exitValue = process.exitValue();
                isClosed = true;
            } catch (Exception e) {
                e.printStackTrace(); 
            } 
        }

        /**
         * This method interrupts and stops the thread.
         */
        public void stop() {
            String processDescription;
            if (commandLineString != null) {
                processDescription = commandLineString;
            } else {
                processDescription = commandLineArray[0];
            }
            debug.print("Process '" + processDescription + "' Killed at:" 
                    + new Date());
            process.destroy();
            thread.interrupt();
        }
    }
}

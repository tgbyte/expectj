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
     * This constructor allows to run a process with indefinite time-out
     * @param executor Will be called upon to create the new process
     */
    ProcessSpawn (Executor executor) {
        if (executor == null) {
            throw new NullPointerException("Executor is null, must get something to run");
        }

        // Initialise the process thread.
        processThread = new ProcessThread(executor);
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
         * This is what we use to create our process.
         */
        private Executor executor;
        
        /**
         * Prepare for starting a process through the given executor.
         * <p>
         * Call {@link #start()} to actually start running the process.
         * 
         * @param executor Will be called upon to start the new process.
         */
        public ProcessThread(Executor executor) {
            this.executor = executor;
        }
        
        /**
         * This method spawns the thread and runs the process within the
         * thread
         * @throws IOException if process spawning fails
         */
        public void start() throws IOException {
            debug.print("Process Started at:" + new Date());
            thread = new Thread(this); 
            process = executor.execute();
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
            debug.print("Process '" + executor + "' Killed at:" 
                    + new Date());
            process.destroy();
            thread.interrupt();
        }
    }
}

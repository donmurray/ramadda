/*
* Copyright 2008-2014 Geode Systems LLC
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this 
* software and associated documentation files (the "Software"), to deal in the Software 
* without restriction, including without limitation the rights to use, copy, modify, 
* merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
* permit persons to whom the Software is furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies 
* or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
* PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
* OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
* DEALINGS IN THE SOFTWARE.
*/

package org.ramadda.util;


import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import ucar.unidata.util.Trace;

import java.io.*;

import java.util.concurrent.*;


/**
 * A utility for running a process
 */
public class ProcessRunner extends Thread {

    /** process killed exit code */
    public static final int PROCESS_KILLED = -143;

    /** the process */
    ProcessBuilder processBuilder;

    /** _more_          */
    Process process;

    /** a flag for whether the process is finished */
    private boolean finished = false;

    /** _more_ */
    private boolean processTimedOut = false;

    /** the process exit code */
    private int exitCode = 0;

    /** timeout */
    private long timeoutSeconds = 0;

    /** _more_          */
    private PrintWriter stdOutPrintWriter;

    /** _more_          */
    private PrintWriter stdErrPrintWriter;

    /** _more_          */
    private StreamEater isg;

    /** _more_          */
    private StreamEater esg;


    /**
     * _more_
     *
     * @param processBuilder _more_
     * @param timeoutSeconds _more_
     * @param stdOutPrintWriter _more_
     * @param stdErrPrintWriter _more_
     */
    public ProcessRunner(ProcessBuilder processBuilder, long timeoutSeconds,
                         PrintWriter stdOutPrintWriter,
                         PrintWriter stdErrPrintWriter) {

        this.processBuilder    = processBuilder;
        this.timeoutSeconds    = timeoutSeconds;
        this.stdOutPrintWriter = stdOutPrintWriter;
        this.stdErrPrintWriter = stdErrPrintWriter;
    }

    /**
     * _more_
     *
     * @throws Exception _more_
     */
    private void eatUpTheStreams() throws Exception {
        int cnt = 0;
        while (esg.getRunning() && (cnt++ < 100)) {
            //            Trace.msg("esg.sleep");
            Misc.sleep(100);
        }

        cnt = 0;
        while (isg.getRunning() && (cnt++ < 100)) {
            //            Trace.msg("isg.sleep");
            Misc.sleep(100);
        }
    }

    /**
     * Run this thread
     */
    public void run() {
        try {
            //            Trace.call1("ProcessRunner.run");
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            // Ignore
        } finally {
            finished = true;
            //            Trace.call2("ProcessRunner.run");
        }
        synchronized (this) {
            notifyAll();
        }
    }

    /**
     * Wait for or kill the process
     *
     * @return  the process exit code
     *
     * @throws Exception _more_
     */
    public int runProcess() throws Exception {
        //Create the process
        process = processBuilder.start();
        esg     = new StreamEater(process.getErrorStream(),
                                  stdErrPrintWriter);
        isg     = new StreamEater(process.getInputStream(),
                                  stdOutPrintWriter);

        esg.start();
        isg.start();

        Thread thread = new Thread(this);
        thread.start();
        waitFor();
        eatUpTheStreams();

        return this.exitCode;
    }


    /**
     * Wait for or kill the process
     *
     * @throws Exception _more_
     */
    private void waitFor() throws Exception {
        //If we don't have a time out then we just want to wait until we're done
        long timeWaiting   = 0;
        long sleepTime     = 100;
        int  timeoutMillis = (int) TimeUnit.SECONDS.toMillis(timeoutSeconds);
        while ( !finished) {
            try {
                synchronized (this) {
                    wait(sleepTime);
                }
            } catch (InterruptedException e) {
                //                Trace.msg("***** interrupted ");
                return;
            }
            timeWaiting += sleepTime;
            if ( !finished && (timeoutMillis > 0)
                    && (timeWaiting > timeoutMillis)) {
                //                Trace.msg("***** Timed out");
                processTimedOut = true;
                process.destroy();
                return;
            }
        }
    }

    /**
     * Get the exit code of the process
     * @return process
     */
    public int getExitCode() {
        return exitCode;
    }


    /**
     *  Get the ProcessTimedOut property.
     *
     *  @return The ProcessTimedOut
     */
    public boolean getProcessTimedOut() {
        return processTimedOut;
    }



}

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


import java.io.*;


/**
 * Class to eat a stream
 */
public class StreamEater extends Thread {

    /** the input stream */
    private InputStream in;

    /** The place to write the lines to */
    private PrintWriter pw;

    /** The type name (for debugging) */
    private String type;

    /**
     * A class for reading lines from an input stream in a thread
     *
     * @param in  InputStream
     * @param pw  the writer for the output
     */
    public StreamEater(InputStream in, PrintWriter pw) {
        this(in, pw, "StreamEater");
    }

    /**
     * A class for reading lines from an input stream in a thread
     *
     * @param in  InputStream
     * @param pw  the writer for the output
     * @param type a string for debugging
     */
    public StreamEater(InputStream in, PrintWriter pw, String type) {
        this.in   = in;
        this.pw   = pw;
        this.type = type;
    }

    /**
     * Run the eater
     */
    @Override
    public void run() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(in));
            String line = null;
            while ((line = br.readLine()) != null) {
                pw.println(line);
                //System.out.println(line);
            }
            //System.out.println("Done reading " + type);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

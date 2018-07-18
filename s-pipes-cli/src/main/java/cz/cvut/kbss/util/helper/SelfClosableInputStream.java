/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.kbss.util.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;


/**
 * TODO do i need it ?
 * @author blaskmir
 */
public final class SelfClosableInputStream extends InputStream {
    private static Logger LOG = LoggerFactory.getLogger(SelfClosableInputStream.class);
    
    InputStream inputStream;
    boolean wasAutomaticalyClosed = false;
    
    public SelfClosableInputStream(InputStream is) {
        this.inputStream = is;
    }

    @Override
    public int read() throws IOException {
        int ret = inputStream.read();
        if (ret == -1) {            
            closeManagedInputStream();
        }
        return ret;
        
    }
   
    @Override
    public int read(byte b[]) throws IOException {
	return read(b);
    }


    @Override
    public int read(byte b[], int off, int len) throws IOException {
        return inputStream.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return inputStream.skip(n);
    }

    @Override
    public int available() throws IOException {
	return inputStream.available();
    }

    @Override
    public void close() throws IOException {
        if (wasAutomaticalyClosed) {
            return;            
        }
        inputStream.close();
    }

  
    @Override
    public synchronized void mark(int readlimit) {}


    @Override
    public synchronized void reset() throws IOException {
	throw new IOException("mark/reset not supported");
    }


    @Override
    public boolean markSupported() {
	return false;
    }

    private void closeManagedInputStream() {
        try {
            inputStream.close();
        } catch (IOException ex) {
            LOG.error("Could not automaticaly close managed input stream " + inputStream + " : ", ex);
        }
    }
    
}

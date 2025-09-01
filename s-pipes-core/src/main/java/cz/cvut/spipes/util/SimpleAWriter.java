package cz.cvut.spipes.util;

import org.apache.jena.atlas.io.AWriter;

import java.io.UncheckedIOException;
import java.io.Writer;
import java.io.IOException;
import java.util.Locale;

public class SimpleAWriter implements AWriter {
    private final Writer out;
    private boolean closed = false;

    public SimpleAWriter(Writer writer) {
        this.out = writer;
    }

    // write*
    @Override
    public void write(char ch) {
        try { out.write(ch); } catch (IOException e) { throw new UncheckedIOException(e); }
    }

    @Override
    public void write(char[] cbuf) {
        try { out.write(cbuf); } catch (IOException e) { throw new UncheckedIOException(e); }
    }

    @Override
    public void write(String s) {
        try { out.write(s); } catch (IOException e) { throw new UncheckedIOException(e); }
    }

    // print*
    @Override
    public void print(char ch) { write(ch); }

    @Override
    public void print(char[] cbuf) { write(cbuf); }

    @Override
    public void print(String s) { write(s); }

    @Override
    public void printf(String fmt, Object... arg) {
        write(String.format(Locale.ROOT, fmt, arg));
    }

    // println
    @Override
    public void println(String s) {
        print(s);
        println();
    }

    @Override
    public void println() {
        print(System.lineSeparator());
        flush();
    }

    @Override
    public void flush() {
        try { out.flush(); } catch (IOException e) { throw new UncheckedIOException(e); }
    }

    @Override
    public void close() {
        if (closed) return;
        try { out.close(); closed = true; } catch (IOException e) { throw new UncheckedIOException(e); }
    }
}

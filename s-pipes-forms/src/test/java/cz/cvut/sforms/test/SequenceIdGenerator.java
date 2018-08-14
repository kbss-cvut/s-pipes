package cz.cvut.sforms.test;

public class SequenceIdGenerator implements IdGenerator {

    private long current = 0;

    @Override
    public String nextString() {
        if (current == Long.MAX_VALUE) {
            throw new IllegalStateException("Maximum generated number exceeded.");
        }
        return Long.toUnsignedString(++current);
    }
}

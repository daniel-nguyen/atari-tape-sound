package pl.dantech.retro.atari;

public class AtariTapeStreamImpl implements AtariTapeStream {
    private static final byte[] masks;

    private byte[] bytes;
    private int currentPos;
    private int currentBit; // -1 is start bit, 0-7 is data bit, 8 stop bit

    static {
        masks = new byte[8];
        for (int i = 0; i < masks.length; i++) {
            masks[i] = (byte)(1 << i);
        }
    }

    public AtariTapeStreamImpl(byte[] bytes) {
        rewind(bytes);
    }

    @Override
    public boolean hasNext() {
        return currentPos < bytes.length;
    }

    @Override
    public boolean next() {
        if (currentBit==-1) {
            currentBit++;
            return false;
        } else if (currentBit==8) {
            currentPos++;
            currentBit = -1;
            return true;
        } else {
            return (bytes[currentPos] & masks[currentBit++]) != 0;
        }
    }

    @Override
    public void rewind(byte[] bytes) {
        currentPos = 0;
        currentBit = -1;
        this.bytes = bytes;
    }
}

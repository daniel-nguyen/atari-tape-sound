package pl.dantech.retro.atari;

public interface AtariTapeStream  {
    boolean hasNext();

    boolean next();

    void rewind(byte[] bytes);
}

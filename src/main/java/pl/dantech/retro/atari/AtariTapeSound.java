package pl.dantech.retro.atari;

import javax.sound.sampled.*;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AtariTapeSound {
    public static void main(String[] args) throws LineUnavailableException {


        final int sampleRate = 44100;
        final int bytesPerSample = 2;
        final int channels = 1;
        final int bufferInSeconds = 5;
        final int oneSecondBuffer = channels * bytesPerSample * sampleRate;
        final ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
        final int baudRate = 600;
        final int samplesPerBit = sampleRate / baudRate;
        final int markFreq = 5327;  //real one
        final int spaceFreq = 3995; //real one
//        final int markFreq = 1000; //testing one
//        final int spaceFreq = 500; //testing one
        final int samplesInBuffer = sampleRate;

        final int scale = 1000;
        final short[] sinBuffer = new short[scale];
        final double shortAmp = ((1<<(bytesPerSample*8-1))-1);
        for (int i = 0; i < scale; i++) {
            double alpha = 2.0d * Math.PI * i / scale;
            sinBuffer[i] = BigDecimal.valueOf(Math.round(Math.sin(alpha) * shortAmp)).shortValueExact();
        }

        final int markMultiplied = sampleRate * scale / markFreq;
        final int spaceMultiplied = sampleRate * scale / spaceFreq;

//        44100 * 1000 / 2;
//
//        22050 * 1000 * 1000
//        ------------
//        22050 * 1000
//
//

        AudioFormat audioFormat = new AudioFormat(sampleRate,  8*bytesPerSample, channels, true, ByteOrder.BIG_ENDIAN.equals(byteOrder));
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat, bufferInSeconds * oneSecondBuffer);
        SourceDataLine line = (SourceDataLine)AudioSystem.getLine(info);

        ByteBuffer byteBuffer = ByteBuffer.allocate(samplesInBuffer * bytesPerSample).order(byteOrder);
        byte[] array = byteBuffer.array();

        try {
            line.open();
            line.start();

            long time = 0L;
            int bufPos = 0;
            while(time<sampleRate*10) {
                int index = (int) ((time * scale * scale / spaceMultiplied) % scale);
                byteBuffer.putShort(sinBuffer[index]);

                time++;
                bufPos++;
                if (bufPos==samplesInBuffer) {
                    System.err.println("flush");
                    int written = line.write(array, 0, array.length);
                    if (written!=array.length) {
                        System.err.println(String.format("written %d < %d", written,  array.length));
                    }

                    byteBuffer.rewind();
                    bufPos = 0;

                    if (!line.isRunning()) {
                        line.start();
                    }
                }
            }



        } finally {
            line.drain();
            line.stop();
            line.close();
        }
    }
}

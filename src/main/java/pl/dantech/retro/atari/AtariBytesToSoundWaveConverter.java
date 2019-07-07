package pl.dantech.retro.atari;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.mutable.MutableLong;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;

import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

public class AtariBytesToSoundWaveConverter {
//    http://home.planet.nl/~ernest/atarixle.html
//    http://a8cas.sourceforge.net/format-cas.html
//    https://www.atariarchives.org/dere/chaptC.php
//    https://www.atariarchives.org/mapping/memorymap.php

    private static final int SCALE = 1000;
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int SAMPLE_SIZE_IN_BYTES = SAMPLE_SIZE_IN_BITS / 8;
    private static final short[] WAVE_BUFFER = new short[SCALE];

    static {
        final double shortAmp = ((1<<(SAMPLE_SIZE_IN_BITS-1))-1);
        initAsSinusoide(shortAmp);
//        initAsSquare(shortAmp);
    }

    private static void initAsSinusoide(double shortAmp) {
        for (int i = 0; i < SCALE; i++) {
            double alpha = 2.0d * Math.PI * i / SCALE;
            WAVE_BUFFER[i] = BigDecimal.valueOf(Math.round(Math.sin(alpha) * shortAmp)).shortValueExact();
        }
    }

    private static void initAsSquare(double shortAmp) {
        int half = SCALE / 2;

        short minus = BigDecimal.valueOf(-shortAmp).shortValueExact();
        for (int i = 0; i < half; i++) {
            WAVE_BUFFER[i] = minus;
        }

        short plus = BigDecimal.valueOf(shortAmp).shortValueExact();
        for (int i = half+1; i < SCALE; i++) {
            WAVE_BUFFER[i] = plus;
        }
    }


    public static byte[] convert(AudioFormat audioFormat, AtariTapeFile file, int baudRate) {

        final int sampleRate = (int) audioFormat.getSampleRate();
        Validate.isTrue(audioFormat.getSampleSizeInBits()==SAMPLE_SIZE_IN_BITS, "only %d-bits samples supported", SAMPLE_SIZE_IN_BITS);

        final int markMultiplied = sampleRate * SCALE / AtariConstants.MARK_FREQ;
        final int spaceMultiplied = sampleRate * SCALE / AtariConstants.SPACE_FREQ;
        final int samplesPerBit = sampleRate / baudRate;

        ByteArrayOutputStream output = new ByteArrayOutputStream(calcInitialBufferSize(file, sampleRate, samplesPerBit));
        ByteBuffer byteBuffer = ByteBuffer.allocate(SAMPLE_SIZE_IN_BYTES).order(audioFormat.isBigEndian() ? BIG_ENDIAN : LITTLE_ENDIAN);

        MutableLong time = new MutableLong(); // position in output      - sample number, from 0 to ...

        AtariTapeStream tapeStream = new AtariTapeStreamImpl(new byte[0]);
        for (AtariTapeRecord record : file.getRecords()) {
            tapeStream.rewind(record.getRecordData());

            // writing pre-record-write-tone
            int preRecordWriteToneSamples = calcPreRecordWriteToneSamples(record, sampleRate);
            for (int i = 0; i < preRecordWriteToneSamples; i++) {
                writeNewSample(output, byteBuffer, time, markMultiplied);
            }

            // writing record
            while (tapeStream.hasNext()) {
                boolean currentBit = tapeStream.next();
                int divider = currentBit ? markMultiplied : spaceMultiplied;

                for (int bitPos = 0; bitPos < samplesPerBit; bitPos++) {
                    writeNewSample(output, byteBuffer, time, divider);
                }
            }

            // writing post-record-gap
            int postRecordGapSamples = calcPostRecordGapSamples(record, sampleRate);
            for (int i = 0; i < postRecordGapSamples; i++) {
                writeNewSample(output, byteBuffer, time, markMultiplied);
            }
        }

        return output.toByteArray();
    }

    public static int write(byte[] dataToWrite, AudioFormat audioFormat, AudioFileFormat.Type fileType, OutputStream outputStream) throws IOException {
        AudioInputStream audioInputStream = new AudioInputStream(new ByteArrayInputStream(dataToWrite), audioFormat, dataToWrite.length / audioFormat.getFrameSize());
        return AudioSystem.write(audioInputStream, fileType, outputStream);
    }

    private static int calcInitialBufferSize(AtariTapeFile file, int sampleRate, int samplesPerBit) {
        int size = 0;

        for (AtariTapeRecord record : file.getRecords()) {
            size += calcPreRecordWriteToneSamples(record, sampleRate);
            size += 132*10*samplesPerBit;
            size += calcPostRecordGapSamples(record, sampleRate);
        }

        return size;
    }

    private static int calcPostRecordGapSamples(AtariTapeRecord record, int sampleRate) {
        return record.getPostRecordGapMillis() * sampleRate / 1000;
    }

    private static int calcPreRecordWriteToneSamples(AtariTapeRecord record, int sampleRate) {
        return record.getPreRecordWriteToneMillis() * sampleRate / 1000;
    }

    private static void writeNewSample(ByteArrayOutputStream output, ByteBuffer byteBuffer, MutableLong time, int divider) {
        int sinusIndex = (int) ((time.longValue() * SCALE * SCALE / divider) % SCALE);
        byteBuffer.rewind();
        byteBuffer.putShort(WAVE_BUFFER[sinusIndex]);
        output.write(byteBuffer.array(), 0, 2);
        time.add(1L);
    }

}

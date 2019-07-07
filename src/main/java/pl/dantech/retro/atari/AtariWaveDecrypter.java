package pl.dantech.retro.atari;

import lombok.Getter;
import org.apache.commons.lang3.Validate;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

public class AtariWaveDecrypter {

    public static void main(String[] args) throws IOException, UnsupportedAudioFileException {
        LoadedSamples loaded = read(new File("/home/danielos/source/atari/wav2cas/SOUND.WAV"));

        System.err.println(loaded.rawSamples.length);

        int sampleRate = Math.round(loaded.audioFormat.getSampleRate());

        int markLength = sampleRate / AtariConstants.MARK_FREQ;
        int spaceLength = sampleRate / AtariConstants.SPACE_FREQ;
        int amplitude = 256;
        int ranges = 8;

        short[] markSin = createSinArray(markLength, amplitude);
        short[] spaceSin = createSinArray(spaceLength, amplitude);

        short[] rawSamples = loaded.rawSamples;

        int[] markFiltered = filter(rawSamples, markSin, amplitude);
        int[] spaceFiltered = filter(rawSamples, spaceSin, amplitude);

        int[] markHistogram = histogram(markFiltered, ranges);
        int[] spaceHistogram = histogram(spaceFiltered, ranges);

        System.err.println("mark histogram");
        printHistogram(markHistogram);

        System.err.println("space histogram");
        printHistogram(spaceHistogram);

        draw(rawSamples);
    }

    private static void draw(short[] values) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                int width = getWidth();
                int height = getHeight();
                g.setColor(Color.black);
                g.drawOval(0, 0, width, height);
            }
        };

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.setSize(500, 300);
        frame.setVisible(true);
    }

    private static void printHistogram(int[] histogram) {
        for (int i = 0; i < histogram.length; i++) {
            System.err.println(String.format("%2d -> %d", i, histogram[i]));
        }
    }

    private static int[] histogram(int[] samples, int ranges) {
        Validate.isTrue(ranges>0);
        Validate.isTrue(samples.length>0);
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int sample : samples) {
            min = Math.min(min, sample);
            max = Math.max(max, sample);
        }
        int[] result = new int[ranges];

        int rangeLength = (max - min) / ranges;
        int rangeMax = ranges - 1;
        for (int sample : samples) {
            int range = Math.min((sample - min) / rangeLength, rangeMax);
            result[range]++;
        }
        return result;
    }

    private static int[] filter(short[] rawSamples, short[] sinSamples, int sinAmplitude) {
        int[] filtered = new int[rawSamples.length];
        int imax = filtered.length - sinSamples.length;
        for (int i = 0; i < imax; i++) {
            long sum = 0L;
            for (int sinPos = 0; sinPos < sinSamples.length; sinPos++) {
                sum += rawSamples[i+sinPos] * sinSamples[sinPos];
            }
            filtered[i] = Math.toIntExact(sum / sinAmplitude);
        }
        return filtered;
    }

    private static short[] createSinArray(int probes, int amplitude) {
        Validate.isTrue(amplitude>0 && amplitude<=256, "amplitude %d out of (0; 256>", amplitude);
        short[] result = new short[probes];
        double PI_2 = 2.0 * Math.PI;
        double amplitudeAsDouble = amplitude;
        for (int i = 0; i < result.length; i++) {
            result[i] = (short) Math.round(Math.sin(PI_2*i/result.length)*amplitudeAsDouble);
        }
        return result;
    }


    public static LoadedSamples read(File file) throws IOException, UnsupportedAudioFileException {
        try (AudioInputStream inputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(file), 100000))) {
            AudioFormat audioFormat = inputStream.getFormat();
            Validate.isTrue(audioFormat.getChannels()==1, "only 1 channel supported");
            Validate.isTrue(audioFormat.getFrameSize()==2, "only 2 bytes supported");

            int samplesCount = (int) inputStream.getFrameLength();
            ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[audioFormat.getFrameSize() * samplesCount]).order(audioFormat.isBigEndian() ? ByteOrder.BIG_ENDIAN : LITTLE_ENDIAN);
            inputStream.read(byteBuffer.array());
            short[] rawSamples = new short[samplesCount];
            for (int i = 0; i < samplesCount; i++) {
                rawSamples[i] = byteBuffer.getShort();
            }
            return new LoadedSamples(audioFormat, rawSamples);
        }
    }

    @Getter
    private static class LoadedSamples {
        private final AudioFormat audioFormat;
        private final short[] rawSamples;

        public LoadedSamples(AudioFormat audioFormat, short[] rawSamples) {
            this.audioFormat = audioFormat;
            this.rawSamples = rawSamples;
        }
    }

}

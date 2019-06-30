package pl.dantech.retro.atari;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.toByteArray;

class AtariBytesToSoundWaveConverterTest {

    @Test()
    @Disabled
    void convert() {
        //TODO
    }

    @Test
    @Disabled("to be run manually")
    void shouldConvertAndSaveAsWave() throws IOException {
        AudioFormat audioFormat = new AudioFormat(44100f, 16, 1, true, true);
        AtariTapeFile file = new AtariTapeFile(toByteArray(getResource("some_data.txt")));
        int baud = 600;
        byte[] ravWave = AtariBytesToSoundWaveConverter.convert(audioFormat, file, baud);
        try (OutputStream outputstream = new BufferedOutputStream(new FileOutputStream("/home/danielos/temp/sound.wav"), 10000)) {
            AtariBytesToSoundWaveConverter.write(ravWave, audioFormat, AudioFileFormat.Type.WAVE, outputstream);
            outputstream.flush();
        }
    }
}

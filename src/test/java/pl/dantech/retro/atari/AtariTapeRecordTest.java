package pl.dantech.retro.atari;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.dantech.retro.atari.AtariTapeRecord.calculateChecksum;
import static pl.dantech.retro.atari.AtariTapeRecord.calculateChecksum2;

class AtariTapeRecordTest {

    @Test
    void shouldCalculateChecksumWithAddingOneAfterWrap() {
        byte[] buffer = new byte[] {
                (byte)250,
                (byte)50,
                (byte)-2     // same as 254
                // 250+50+254 == 554 == 554%256 + 554/256 (number of wrappings) = (42 + 2) % 256 = 44 = 0x2c
        };

        byte checkSum = calculateChecksum(buffer, 0, buffer.length);

        assertThat(checkSum).isEqualTo((byte)0x2c);
    }

    @Test
    void shouldCalculateChecksum2DoTheSame() {
        Random random = new Random(5555L);
        byte[] buffer = new byte[10000];

        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = (byte) random.nextInt(256);
        }

        assertThat(calculateChecksum2(buffer, 0, buffer.length)).isEqualTo(calculateChecksum(buffer, 0, buffer.length));
    }
}
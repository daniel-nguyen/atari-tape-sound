package pl.dantech.retro.atari;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class AtariTapeFileTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldHandlePartialAndEof() {
        byte[] dataBytes = new byte[129];

        AtariTapeFile file = new AtariTapeFile(dataBytes);
        assertThat(file.getRecords()).hasSize(3);

        assertThat(file.getRecords().get(0).getDataLength()).isEqualTo(128);
        assertThat(file.getRecords().get(0).isPartialRecord()).isFalse();
        assertThat(file.getRecords().get(0).isEofRecord()).isFalse();

        assertThat(file.getRecords().get(1).getDataLength()).isEqualTo(1);
        assertThat(file.getRecords().get(1).isPartialRecord()).isTrue();
        assertThat(file.getRecords().get(1).isEofRecord()).isFalse();

        assertThat(file.getRecords().get(2).getDataLength()).isEqualTo(0);
        assertThat(file.getRecords().get(2).isPartialRecord()).isTrue();
        assertThat(file.getRecords().get(2).isEofRecord()).isTrue();
   }
}
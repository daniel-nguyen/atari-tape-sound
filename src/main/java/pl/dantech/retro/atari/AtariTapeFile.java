package pl.dantech.retro.atari;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import org.apache.commons.lang3.Validate;

public class AtariTapeFile {
    @Getter
    private final ImmutableList<AtariTapeRecord> records;

    public AtariTapeFile(byte[] dataBytes) {
        int offset = 0;
        ImmutableList.Builder<AtariTapeRecord> builder = ImmutableList.builder();

        while (true) {
            AtariTapeRecord record = new AtariTapeRecord(dataBytes, offset);
            offset += record.getDataLength();
            builder.add(record);
            if (record.isPartialRecord()) {
                if (!record.isEofRecord()) {
                    AtariTapeRecord eofRecord = new AtariTapeRecord(dataBytes, offset);
                    Validate.isTrue(eofRecord.getDataLength()==0, "expected EOF record");
                    builder.add(eofRecord);
                }
                break;
            }
        }
        records = builder.build();
    }
}

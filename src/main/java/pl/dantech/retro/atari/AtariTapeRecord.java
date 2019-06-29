package pl.dantech.retro.atari;

import lombok.Getter;

import static java.lang.Math.*;
import static org.apache.commons.lang3.Validate.isTrue;

public class AtariTapeRecord {
    private static final int DATA_CAPACITY = 128;
    private static final int RECORD_SIZE = DATA_CAPACITY + 4;

    private static final int OFFSET_MARKER_01 = 0;
    private static final int OFFSET_MARKER_02 = 1;
    private static final int OFFSET_CONTROL_BYTE = 2;
    private static final int OFFSET_PARTIAL_RECORD_SIZE = 2 + DATA_CAPACITY;

    @Getter
    private final int dataLength;
    @Getter
    private final byte[] recordData;

    public AtariTapeRecord(byte[] srcBuffer, int offset) {
        isTrue(offset>=0, "offset %d must be non-negative", offset);

        dataLength = min(srcBuffer.length-offset, DATA_CAPACITY);
        recordData = new byte[RECORD_SIZE];
        recordData[OFFSET_MARKER_01] = 0x55;
        recordData[OFFSET_MARKER_02] = 0x55;
        System.arraycopy(srcBuffer, offset, recordData, 3, dataLength);
        if (dataLength==DATA_CAPACITY) {
            //FULL RECORD
            recordData[OFFSET_CONTROL_BYTE] = (byte)0xFC;
        } else if (dataLength==0) {
            //END OF FILE
            recordData[OFFSET_CONTROL_BYTE] = (byte)0xFE;
            recordData[OFFSET_PARTIAL_RECORD_SIZE] = (byte)dataLength;
        } else {
            //PARTIAL RECORD
            recordData[OFFSET_CONTROL_BYTE] = (byte)0xFA;
        }

        // checksum at the end, because it contains 2 markers, control byte and 128 bytes of data
        recordData[dataLength+3] = calculateChecksum(recordData, 0, RECORD_SIZE-1);
    }

    public boolean isPartialRecord() {
        return dataLength<DATA_CAPACITY;
    }

    public boolean isEofRecord() {
        return dataLength==0;
    }

    private static byte calculateChecksum(byte[] buffer, int offset, int dataLength) {
        int result = 0;
        for (int i = 0; i < dataLength; i++) {
            result += (buffer[offset+i] & 0xff);
        }
        return (byte)result;
    }

    public int getPreRecordWriteToneMillis() {
        return 3000; //TODO make parametrable
    }

    public int getPostRecordGapMillis() {
        return 250; //TODO make parametrable
    }
}

//
// Decompiled by VineFlower - 875ms
//
package coelho.msftauth.util.der;

import java.io.IOException;
import java.util.ArrayList;

class DerIndefLenConverter {
    private static final int CLASS_MASK = 192;
    private static final int FORM_MASK = 32;
    private static final int LEN_LONG = 128;
    private static final int LEN_MASK = 127;
    private static final int TAG_MASK = 31;
    private byte[] data;
    private int dataPos;
    private int dataSize;
    private int index;
    private final ArrayList<Object> ndefsList;
    private byte[] newData;
    private int newDataPos;
    private int numOfTotalLenBytes;
    private int unresolved = 0;

    DerIndefLenConverter() {
        this.ndefsList = new ArrayList<>();
        this.numOfTotalLenBytes = 0;
    }

    private byte[] getLengthBytes(int var1) {
        byte[] var2;
        if (var1 < 128) {
            var2 = new byte[]{(byte)var1};
        } else if (var1 < 256) {
            var2 = new byte[]{-127, (byte)var1};
        } else if (var1 < 65536) {
            var2 = new byte[]{-126, (byte)(var1 >> 8), (byte)var1};
        } else if (var1 < 0x01000000) {
            var2 = new byte[]{-125, (byte)(var1 >> 16), (byte)(var1 >> 8), (byte)var1};
        } else {
            var2 = new byte[]{-124, (byte)(var1 >> 24), (byte)(var1 >> 16), (byte)(var1 >> 8), (byte)var1};
        }

        return var2;
    }

    private boolean isEOC(int var1) {
        boolean var2;
        var2 = (var1 & 31) == 0 && (var1 & 32) == 0 && (var1 & 192) == 0;

        return var2;
    }

    static boolean isIndefinite(int var0) {
        boolean var1;
        var1 = isLongForm(var0) && (var0 & 127) == 0;

        return var1;
    }

    static boolean isLongForm(int var0) {
        boolean var1;
        var1 = (var0 & 128) == 128;

        return var1;
    }

    private int parseLength() throws IOException {
        int var1 = 0;
        if (this.dataPos != this.dataSize) {
            byte[] var5 = this.data;
            int var2 = this.dataPos++;
            var2 = var5[var2] & 255;
            if (isIndefinite(var2)) {
                this.ndefsList.add(this.dataPos);
                this.unresolved++;
            } else if (isLongForm(var2)) {
                int var3 = var2 & 127;
                if (var3 > 4) {
                    throw new IOException("Too much data");
                }

                if (this.dataSize - this.dataPos < var3 + 1) {
                    throw new IOException("Too little data");
                }

                var2 = 0;

                for (var1 = 0; var2 < var3; var2++) {
                    int var4 = this.dataPos++;
                    var1 = (var1 << 8) + (var5[var4] & 255);
                }

                if (var1 < 0) {
                    throw new IOException("Invalid length bytes");
                }
            } else {
                var1 = var2 & 127;
            }
        }

        return var1;
    }

    private void parseTag() throws IOException {
        if (this.dataPos != this.dataSize) {
            if (this.isEOC(this.data[this.dataPos]) && this.data[this.dataPos + 1] == 0) {
                int var1 = 0;
                Object var5 = null;

                int var2;
                for (var2 = this.ndefsList.size() - 1; var2 >= 0; var2--) {
                    var5 = this.ndefsList.get(var2);
                    if (var5 instanceof Integer) {
                        break;
                    }

                    var1 += ((byte[])var5).length - 3;
                }

                if (var2 < 0) {
                    throw new IOException("EOC does not have matching indefinite-length tag");
                }

                int var3 = this.dataPos;
                int var4 = (Integer)var5;
                var5 = this.getLengthBytes(var3 - var4 + var1);
                this.ndefsList.set(var2, var5);
                this.unresolved--;
                var1 = this.numOfTotalLenBytes;
                this.numOfTotalLenBytes = ((byte[])var5).length - 3 + var1;
            }

            this.dataPos++;
        }
    }

    private void parseValue(int var1) {
        this.dataPos += var1;
    }

    private void writeLength(int var1) {
        if (var1 < 128) {
            byte[] var3 = this.newData;
            int var2 = this.newDataPos++;
            var3[var2] = (byte)var1;
        } else if (var1 < 256) {
            byte[] var18 = this.newData;
            int var4 = this.newDataPos++;
            var18[var4] = -127;
            var4 = this.newDataPos++;
            var18[var4] = (byte)var1;
        } else if (var1 < 65536) {
            byte[] var20 = this.newData;
            int var6 = this.newDataPos++;
            var20[var6] = -126;
            var6 = this.newDataPos++;
            var20[var6] = (byte)(var1 >> 8);
            var6 = this.newDataPos++;
            var20[var6] = (byte)var1;
        } else if (var1 < 0x01000000) {
            byte[] var23 = this.newData;
            int var9 = this.newDataPos++;
            var23[var9] = -125;
            var9 = this.newDataPos++;
            var23[var9] = (byte)(var1 >> 16);
            var9 = this.newDataPos++;
            var23[var9] = (byte)(var1 >> 8);
            var9 = this.newDataPos++;
            var23[var9] = (byte)var1;
        } else {
            byte[] var27 = this.newData;
            int var13 = this.newDataPos++;
            var27[var13] = -124;
            var13 = this.newDataPos++;
            var27[var13] = (byte)(var1 >> 24);
            var13 = this.newDataPos++;
            var27[var13] = (byte)(var1 >> 16);
            var13 = this.newDataPos++;
            var27[var13] = (byte)(var1 >> 8);
            var13 = this.newDataPos++;
            var27[var13] = (byte)var1;
        }
    }

    private void writeLengthAndValue() throws IOException {
        if (this.dataPos != this.dataSize) {
            byte[] var5 = this.data;
            int var1 = this.dataPos++;
            int var3 = var5[var1] & 255;
            if (isIndefinite(var3)) {
                var1 = this.index++;
                var5 = (byte[]) this.ndefsList.get(var1);
                System.arraycopy(var5, 0, this.newData, this.newDataPos, var5.length);
                var1 = this.newDataPos;
                this.newDataPos = var5.length + var1;
            } else {
                int var9;
                if (!isLongForm(var3)) {
                    var9 = var3 & 127;
                } else {
                    var9 = 0;

                    for (var1 = 0; var9 < (var3 & 127); var9++) {
                        int var4 = this.dataPos++;
                        var1 = (var1 << 8) + (var5[var4] & 255);
                    }

                    var9 = var1;
                    if (var1 < 0) {
                        throw new IOException("Invalid length bytes");
                    }
                }

                this.writeLength(var9);
                this.writeValue(var9);
            }
        }
    }

    private void writeTag() {
        if (this.dataPos != this.dataSize) {
            byte[] var3 = this.data;
            int var1 = this.dataPos++;
            byte var2 = var3[var1];
            if (this.isEOC(var2) && this.data[this.dataPos] == 0) {
                this.dataPos++;
                this.writeTag();
            } else {
                var3 = this.newData;
                var1 = this.newDataPos++;
                var3[var1] = var2;
            }
        }
    }

    private void writeValue(int var1) {
        for (int var2 = 0; var2 < var1; var2++) {
            byte[] var5 = this.newData;
            int var3 = this.newDataPos++;
            byte[] var6 = this.data;
            int var4 = this.dataPos++;
            var5[var3] = var6[var4];
        }
    }

    byte[] convert(byte[] var1) throws IOException {
        this.data = var1;
        this.dataPos = 0;
        this.index = 0;
        this.dataSize = this.data.length;

        int var2;
        while (true) {
            if (this.dataPos < this.dataSize) {
                this.parseTag();
                var2 = this.parseLength();
                this.parseValue(var2);
                if (this.unresolved != 0) {
                    continue;
                }

                var2 = this.dataSize - this.dataPos;
                this.dataSize = this.dataPos;
                break;
            }

            var2 = 0;
            break;
        }

        if (this.unresolved != 0) {
            throw new IOException("not all indef len BER resolved");
        } else {
            this.newData = new byte[this.dataSize + this.numOfTotalLenBytes + var2];
            this.dataPos = 0;
            this.newDataPos = 0;
            this.index = 0;

            while (this.dataPos < this.dataSize) {
                this.writeTag();
                this.writeLengthAndValue();
            }

            System.arraycopy(var1, this.dataSize, this.newData, this.dataSize + this.numOfTotalLenBytes, var2);
            return this.newData;
        }
    }
}


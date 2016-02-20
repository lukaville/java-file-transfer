package util;

import com.sun.istack.internal.Nullable;

import java.util.BitSet;

/**
 * alex on 12.02.16.
 */
public class HammingUtils {
    public static byte[] cycleEncode(byte[] origin) {
        int originBitLength = origin.length * 8;
        BitSet originBitSet = BitSet.valueOf(origin);
        int encodedBitLength = originBitLength % 11 == 0 ? originBitLength / 11 * 15 : originBitLength / 11 * 15 + 15;
        BitSet encoded = new BitSet(encodedBitLength);

        for (int i = 0, j = 0; i < originBitLength; i += 11, j += 15) {
            BitSet part = originBitSet.get(i, i + 11);

            BitSet encodedPart = encode(part, 11); // it's length will be 15

            copyBitSetToBitSet(encodedPart, 0, encoded, j, 15);
        }

        return bitSetToByteArray(encoded, encodedBitLength);
    }

    private static BitSet encode(BitSet origin, int originBitLength) {
        int encodedBitLength = (int) (originBitLength + Math.log(originBitLength) / Math.log(2) + 1);
        BitSet encoded = new BitSet(encodedBitLength);

        for (int i = 1, j = originBitLength - 1; i <= encodedBitLength; ++i) {
            if (Math.log(i) / Math.log(2) % 1 == 0) {
                encoded.set(encodedBitLength - i, false);
            } else {
                encoded.set(encodedBitLength - i, origin.get(j));
                --j;
            }
        }

        for (int bitmask = 1, j = 0; j < (int) (Math.log(encodedBitLength) / Math.log(2)) + 1; bitmask <<= 1, ++j) {
            int countOfBits = 0;
            for (int i = bitmask; i <= encodedBitLength; ++i) {
                if ((i & bitmask) != 0) {
                    countOfBits += (encoded.get(encodedBitLength - i) ? 1 : 0);
                }
            }
            countOfBits %= 2;
            encoded.set(encodedBitLength - bitmask, countOfBits != 0);
        }

        return encoded;
    }

    @Nullable
    public static byte[] cycleDecode(byte[] encoded) {
        if (!cycleValidateEncoded(encoded)) {
            return null;
        }

        int encodedBitLength = encoded.length * 8;
        BitSet encodedBitSet = BitSet.valueOf(encoded);
        int decodedBitLength = encodedBitLength % 15 == 0 ? encodedBitLength / 15 * 11 : encodedBitLength / 15 * 11 + 11;
        BitSet decoded = new BitSet(decodedBitLength);

        for (int i = 0, j = 0; i < encodedBitLength; i += 15, j += 11) {
            BitSet part = encodedBitSet.get(i, i + 15);

            BitSet decodedPart = decode(part, 15); // it's length will be 11

            copyBitSetToBitSet(decodedPart, 0, decoded, j, 11);
        }

        return bitSetToByteArray(decoded, decodedBitLength);
    }

    private static BitSet decode(BitSet encoded, int encodedBitLength) {
        int decodedBitLength = encodedBitLength - (int) (Math.log(encodedBitLength) / Math.log(2)) - 1;
        BitSet decoded = new BitSet(decodedBitLength);

        for (int i = encodedBitLength - 1, j = decodedBitLength - 1; i >= 0; --i) {
            if (Math.log(encodedBitLength - i) / Math.log(2) % 1 != 0) {
                decoded.set(j, encoded.get(i));
                --j;
            }
        }

        return decoded;
    }

    public static boolean cycleValidateEncoded(byte[] encoded) {
        int encodedBitLength = encoded.length * 8;
        BitSet encodedBitSet = BitSet.valueOf(encoded);

        for (int i = 0; i < encodedBitLength; i += 15) {
            BitSet part = encodedBitSet.get(i, i + 15);

            if (!validateEncoded(part, 15)) {
                return false;
            }
        }

        return true;
    }

    private static boolean validateEncoded(BitSet encoded, int encodedBitLength) {
        int errorSyndromeLength = (int) (Math.log(encodedBitLength) / Math.log(2)) + 1;
        BitSet errorSyndrome = new BitSet(errorSyndromeLength);

        for (int bitmask = 1, j = 0; j < errorSyndromeLength; bitmask <<= 1, ++j) {
            int countOfBits = 0;
            for (int i = bitmask; i <= encodedBitLength; ++i) {
                if ((i & bitmask) != 0) {
                    countOfBits += (encoded.get(encodedBitLength - i) ? 1 : 0);
                }
            }
            countOfBits %= 2;
            errorSyndrome.set(j, countOfBits != 0);
        }

        return errorSyndrome.length() == 0;
    }

    private static byte[] bitSetToByteArray(BitSet bitSet, int bitSetLength) {
        byte[] ret = bitSet.toByteArray();
        if (ret.length == bitSetLength) {
            return ret;
        }
        byte[] finalRet = new byte[bitSetLength % 8 == 0 ? bitSetLength / 8 : bitSetLength / 8 + 1];
        System.arraycopy(ret, 0, finalRet, 0, ret.length);
        return finalRet;
    }

    private static void copyBitSetToBitSet(BitSet src, int srcPos, BitSet dest, int destPos, int length) {
        for (int i = srcPos, j = destPos; i < length; ++i, ++j) {
            dest.set(j, src.get(i));
        }
    }
}

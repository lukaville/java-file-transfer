package util;

import com.sun.istack.internal.Nullable;

import java.util.BitSet;

/**
 * alex on 12.02.16.
 */
public class HammingUtils {
    private static void copyBitSetToBitSet(BitSet src, int srcPos, BitSet dest, int destPos, int length) {
        BitSet srcPart = src.get(srcPos, srcPos + length);
        for (int i = 0, j = destPos; i < length; ++i, ++j) {
            dest.set(j, srcPart.get(i));
        }
    }

    private static void reverseBitSet(BitSet bitSet, int size) {
        BitSet ret = new BitSet(size);
        for (int i = 0; i < size; ++i) {
            ret.set(size - i - 1, bitSet.get(i));
        }
        bitSet = ret;
    }

    public static byte[] cycleEncode(byte[] origin) {
        BitSet originBitSet = BitSet.valueOf(origin);
        int encodedSize = originBitSet.size() % 11 == 0 ? originBitSet.size() / 11 * 15 : originBitSet.size() / 11 * 15 + 15;
        BitSet encoded = new BitSet(encodedSize);

        for (int i = 0, j = 0; i < originBitSet.size(); i += 11, j += 15) {
            BitSet part;
            if (i + 11 < originBitSet.size()) {
                part = originBitSet.get(i, i + 11);
//                part = Arrays.copyOfRange(origin, i, i + 11);
            } else {
                part = new BitSet(11);
                copyBitSetToBitSet(originBitSet, i, part, 0, originBitSet.size() - i);
//                System.arraycopy(origin, i, part, 0, origin.length - i);
            }

            BitSet encodedPart = encode(part);

            copyBitSetToBitSet(encodedPart, 0, encoded, j, encodedPart.size());
//            System.arraycopy(encodedPart, 0, encoded, j, encodedPart.length);
        }

        return encoded.toByteArray();
    }

    private static BitSet encode(BitSet origin) {
        BitSet encoded = new BitSet((int) (origin.size() + Math.log(origin.size()) / Math.log(2) + 1));

        for (int i = 1, j = origin.size() - 1; i <= encoded.size(); ++i) {
            if (Math.log(i) / Math.log(2) % 1 == 0) {
                encoded.set(encoded.size() - i, 0);
            } else {
                encoded.set(encoded.size() - i, origin.get(j));
                --j;
            }
        }

        for (int bitmask = 1, j = 0; j < (int) (Math.log(encoded.size()) / Math.log(2)) + 1; bitmask <<= 1, ++j) {
            int countOfBits = 0;
            for (int i = bitmask; i <= encoded.size(); ++i) {
                if ((i & bitmask) != 0) {
                    countOfBits += (encoded.get(encoded.size() - i) ? 1 : 0);
                }
            }
            countOfBits %= 2;
            encoded.set(encoded.size() - bitmask, countOfBits);
        }

        return encoded;
    }

    @Nullable
    public static byte[] cycleDecode(byte[] encoded) {
        if (!cycleValidateEncoded(encoded)) {
            return null;
        }

        BitSet encodedBitSet = BitSet.valueOf(encoded);
        BitSet decoded = new BitSet(encodedBitSet.size() / 15 * 11);

        for (int i = 0, j = 0; i < encodedBitSet.size(); i += 15, j += 11) {
            BitSet part = encodedBitSet.get(i, i + 15);
            BitSet decodedPart = decode(part);

            copyBitSetToBitSet(decodedPart, 0, decoded, j, decodedPart.size());
//            System.arraycopy(decodedPart, 0, decoded, j, decodedPart.length);
        }

        return decoded.toByteArray();
    }

    private static BitSet decode(BitSet encoded) {
        BitSet decoded = new BitSet(encoded.size() - (int) (Math.log(encoded.size()) / Math.log(2)) - 1);

        for (int i = encoded.size() - 1, j = 0; i >= 0; --i) {
            if (Math.log(encoded.size() - i) / Math.log(2) % 1 != 0) {
                decoded.set(j, encoded.get(i));
                ++j;
            }
        }

//        reverseBitSet(decoded); TODO
//        Collections.reverse(decoded);

        return decoded;
    }

    public static boolean cycleValidateEncoded(byte[] encoded) {
        BitSet encodedBitSet = BitSet.valueOf(encoded);

        for (int i = 0; i < encodedBitSet.size(); i += 15) {
            BitSet part = encodedBitSet.get(i, i + 15);

            if (!validateEncoded(part)) {
                return false;
            }
        }

        return true;
    }

    private static boolean validateEncoded(BitSet encoded) {
        BitSet errorSyndrome = new BitSet((int) (Math.log(encoded.size()) / Math.log(2)) + 1);

        for (int bitmask = 1, j = 0; j < (int) (Math.log(encoded.size()) / Math.log(2)) + 1; bitmask <<= 1, ++j) {
            int countOfBits = 0;
            for (int i = bitmask; i <= encoded.size(); ++i) {
                if ((i & bitmask) != 0) {
                    countOfBits += (encoded.get(encoded.size() - i) ? 1 : 0);
                }
            }
            countOfBits %= 2;
            errorSyndrome.set(j, countOfBits);
        }

        return errorSyndrome.length() == 0;
    }
}

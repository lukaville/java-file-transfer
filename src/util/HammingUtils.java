package util;

import com.sun.istack.internal.Nullable;

import java.util.Arrays;
import java.util.Collections;

/**
 * alex on 12.02.16.
 */
public class HammingUtils {
    public static byte[] cycleEncode(byte[] origin) {
        int encodedSize = origin.length % 11 == 0 ? origin.length / 11 * 15 : origin.length / 11 * 15 + 15;
        byte[] encoded = new byte[encodedSize];

        for (int i = 0, j = 0; i < origin.length; i += 11, j += 15) {
            byte[] part;
            if (i + 11 < origin.length) {
                part = Arrays.copyOfRange(origin, i, i + 11);
            } else {
                part = new byte[11];
                System.arraycopy(origin, i, part, 0, origin.length - i);
            }

            byte[] encodedPart = encode(part);

            System.arraycopy(encodedPart, 0, encoded, j, encodedPart.length);
        }

        return encoded;
    }

    private static byte[] encode(byte[] origin) {
        byte[] encoded = new byte[(int) (origin.length + Math.log(origin.length) / Math.log(2) + 1)];

        for (int i = 1, j = origin.length - 1; i <= encoded.length; ++i) {
            if (Math.log(i) / Math.log(2) % 1 == 0) {
                encoded[encoded.length - i] = 0;
            } else {
                encoded[encoded.length - i] = origin[j];
                --j;
            }
        }

        for (int bitmask = 1, j = 0; j < (int) (Math.log(encoded.length) / Math.log(2)) + 1; bitmask <<= 1, ++j) {
            int countOfBits = 0;
            for (int i = bitmask; i <= encoded.length; ++i) {
                if ((i & bitmask) != 0) {
                    countOfBits += encoded[encoded.length - i];
                }
            }
            countOfBits %= 2;
            encoded[encoded.length - bitmask] = (byte) countOfBits;
        }

        return encoded;
    }

    @Nullable
    public static byte[] cycleDecode(byte[] encoded) {
        if (!cycleValidateEncoded(encoded)) {
            return null;
        }

        byte[] decoded = new byte[encoded.length / 15 * 11];
        for (int i = 0, j = 0; i < encoded.length; i += 15, j += 11) {
            byte[] part = Arrays.copyOfRange(encoded, i, i + 15);
            byte[] decodedPart = decode(part);

            System.arraycopy(decodedPart, 0, decoded, j, decodedPart.length);
        }

        return decoded;
    }

    private static byte[] decode(byte[] encoded) {
        byte[] decoded = new byte[encoded.length - (int) (Math.log(encoded.length) / Math.log(2)) - 1];

        for (int i = encoded.length - 1, j = 0; i >= 0; --i) {
            if (Math.log(encoded.length - i) / Math.log(2) % 1 != 0) {
                decoded[j] = encoded[i];
                ++j;
            }
        }

        Collections.reverse(Arrays.asList(decoded));

        return decoded;
    }

    public static boolean cycleValidateEncoded(byte[] encoded) {
        for (int i = 0; i < encoded.length; i += 15) {
            byte[] part = Arrays.copyOfRange(encoded, i, i + 15);

            if (!validateEncoded(part)) {
                return false;
            }
        }

        return true;
    }

    private static boolean validateEncoded(byte[] encoded) {
        byte[] errorSyndrome = new byte[(int) (Math.log(encoded.length) / Math.log(2)) + 1];

        for (int bitmask = 1, j = 0; j < (int) (Math.log(encoded.length) / Math.log(2)) + 1; bitmask <<= 1, ++j) {
            int countOfBits = 0;
            for (int i = bitmask; i <= encoded.length; ++i) {
                if ((i & bitmask) != 0) {
                    countOfBits += encoded[encoded.length - i];
                }
            }
            countOfBits %= 2;
            errorSyndrome[j] = (byte) countOfBits;
        }

        Collections.reverse(Arrays.asList(errorSyndrome));

        return ByteUtils.byteArrayToInt(errorSyndrome) == 0;
    }
}

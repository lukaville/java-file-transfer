import util.HammingUtils;

import java.util.Arrays;
import java.util.Random;

/*
 * alex on 12.02.16.
 */
public class HammingUtilsTest {

    private byte[] b8;
    private byte[] b24;
    private byte[] b64;
    private byte[] b128;

    @org.junit.Before
    public void setUp() throws Exception {
        b8 = new byte[] {25};
        b24 = new byte[] {103, 68, 47};
        b64 = new byte[] {85, 12, 22, 37, 42, 88, 2, 13};
        b128 = new byte[] {85, 12, 22, 37, 42, 88, 2, 13, 85, 12, 22, 37, 42, 88, 2, 13};
    }

    @org.junit.Test
    public void testB8() throws Exception {
        byte[] encoded = HammingUtils.cycleEncode(b8);
        byte[] decoded = HammingUtils.cycleDecode(encoded);
        assertEquals(b8, decoded);
    }

    @org.junit.Test
    public void testB24() throws Exception {
        byte[] encoded = HammingUtils.cycleEncode(b24);
        byte[] decoded = HammingUtils.cycleDecode(encoded);
        assertEquals(b24, decoded);
    }

    @org.junit.Test
    public void testB64() throws Exception {
        byte[] encoded = HammingUtils.cycleEncode(b64);
        byte[] decoded = HammingUtils.cycleDecode(encoded);
        assertEquals(b64, decoded);
    }

    @org.junit.Test
    public void testB128() throws Exception {
        byte[] encoded = HammingUtils.cycleEncode(b128);
        byte[] decoded = HammingUtils.cycleDecode(encoded);
        assertEquals(b128, decoded);
    }

    @org.junit.Test
    public void testCycleValidateEncoded() throws Exception {
        byte[] encoded = HammingUtils.cycleEncode(b128);
        byte[] decoded = HammingUtils.cycleDecode(encoded);
        assert decoded != null;
        Random random = new Random();
        int randInt = random.nextInt(encoded.length);
        encoded[randInt] = (byte) (encoded[randInt] ^ 0x01);
        assert !HammingUtils.cycleValidateEncoded(encoded);
    }

    private void assertEquals(byte[] source, byte[] decoded) {
        byte[] clippedDecodedArray = new byte[source.length];
        System.arraycopy(decoded, 0, clippedDecodedArray, 0, source.length);
        System.out.println(Arrays.toString(source) + " ---> ");
        System.out.println(Arrays.toString(decoded));
        assert Arrays.equals(source, clippedDecodedArray);
    }
}
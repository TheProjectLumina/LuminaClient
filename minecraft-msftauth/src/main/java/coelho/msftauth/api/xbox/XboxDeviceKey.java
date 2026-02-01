package coelho.msftauth.api.xbox;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

import coelho.msftauth.util.der.DerInputStream;
import coelho.msftauth.util.der.DerValue;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okio.Buffer;

@SuppressWarnings("FieldMayBeFinal")
public class XboxDeviceKey {
    private static final KeyPairGenerator KEY_PAIR_GEN;
    private final KeyPair ecKey = KEY_PAIR_GEN.generateKeyPair();
    private String id = UUID.randomUUID().toString();
    private final XboxProofKey proofKey = new XboxProofKey(this);

    static {
        GeneralSecurityException e;
        try {
            KEY_PAIR_GEN = KeyPairGenerator.getInstance("EC");
            KEY_PAIR_GEN.initialize(new ECGenParameterSpec("secp256r1"));
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e2) {
            e = e2;
            throw new RuntimeException(e);
        }
    }

    public void sign(Builder requestBuilder) {
        try {
            Buffer buffer = new Buffer();
            Request tempRequest = requestBuilder.build();
            String authHeader = tempRequest.header("Authorization");
            tempRequest.body().writeTo(buffer);
            requestBuilder.addHeader("Signature", sign(tempRequest.url().encodedPath(), authHeader, tempRequest.method(), buffer.readByteArray()));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    public String sign(String path, String authHeader, String requestMethod, byte[] body) {
        try {
            byte[] auth;
            byte[] uri = path.getBytes(StandardCharsets.US_ASCII);
            if (authHeader == null) {
                auth = new byte[0];
            } else {
                auth = authHeader.getBytes(StandardCharsets.US_ASCII);
            }
            byte[] method = requestMethod.getBytes(StandardCharsets.US_ASCII);
            if (body == null) {
                body = new byte[0];
            }
            long time = (Instant.now().getEpochSecond() + 11644473600L) * 10000000;
            ByteBuffer buffer = ByteBuffer.allocate(body.length + 256);
            buffer.order(ByteOrder.BIG_ENDIAN);
            buffer.put(new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 0});
            buffer.putLong(time);
            buffer.put((byte) 0);
            buffer.put(method);
            buffer.put((byte) 0);
            buffer.put(uri);
            buffer.put((byte) 0);
            buffer.put(auth);
            buffer.put((byte) 0);
            buffer.put(body);
            buffer.put((byte) 0);
            buffer.flip();
            byte[] arrSignature = ecdsaSign((ECPrivateKey) this.ecKey.getPrivate(), buffer);
            buffer = ByteBuffer.allocate(arrSignature.length + 12);
            buffer.putInt(1);
            buffer.putLong(time);
            buffer.put(arrSignature);
            buffer.rewind();
            byte[] arrFinal = new byte[buffer.remaining()];
            buffer.get(arrFinal);
            return Base64.getEncoder().encodeToString(arrFinal);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static byte[] ecdsaSign(ECPrivateKey privateKey, ByteBuffer buffer) throws Exception {
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privateKey);
        signature.update(buffer);
        return decodeSignature(signature.sign());
    }

    public static byte[] decodeSignature(byte[] sig) throws SignatureException {
        try {
            DerInputStream in = new DerInputStream(sig, 0, sig.length, false);
            DerValue[] values = in.getSequence(2);
            if (values.length == 2 && in.available() == 0) {
                BigInteger r = values[0].getPositiveBigInteger();
                BigInteger s = values[1].getPositiveBigInteger();
                byte[] rBytes = trimZeroes(r.toByteArray());
                byte[] sBytes = trimZeroes(s.toByteArray());
                int k = Math.max(rBytes.length, sBytes.length);
                byte[] result = new byte[(k << 1)];
                System.arraycopy(rBytes, 0, result, k - rBytes.length, rBytes.length);
                System.arraycopy(sBytes, 0, result, result.length - sBytes.length, sBytes.length);
                return result;
            }
            throw new IOException("Invalid encoding for signature");
        } catch (Exception e) {
            throw new SignatureException("Invalid encoding for signature", e);
        }
    }

    public static byte[] trimZeroes(byte[] b) {
        int i = 0;
        while (i < b.length - 1 && b[i] == (byte) 0) {
            i++;
        }
        return i == 0 ? b : Arrays.copyOfRange(b, i, b.length);
    }

    public String getId() {
        return this.id;
    }

    public String getCrv() {
        return "P-256";
    }

    public String getAlg() {
        return "ES256";
    }

    public String getUse() {
        return "sig";
    }

    public String getKty() {
        return "EC";
    }

    public byte[] getPublicXBytes() {
        return bigIntegerToByteArray(((ECPublicKey) this.ecKey.getPublic()).getW().getAffineX());
    }

    public byte[] getPublicYBytes() {
        return bigIntegerToByteArray(((ECPublicKey) this.ecKey.getPublic()).getW().getAffineY());
    }

    private static byte[] bigIntegerToByteArray(BigInteger bigInteger) {
        byte[] array = bigInteger.toByteArray();
        if (array[0] != (byte) 0) {
            return array;
        }
        byte[] newArray = new byte[(array.length - 1)];
        System.arraycopy(array, 1, newArray, 0, newArray.length);
        return newArray;
    }

    public XboxProofKey getProofKey() {
        return this.proofKey;
    }
}

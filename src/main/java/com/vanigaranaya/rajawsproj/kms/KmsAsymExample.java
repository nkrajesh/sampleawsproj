package com.vanigaranaya.rajawsproj.kms;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.*;

import java.util.Base64;

public class KmsAsymExample {

    static KmsClient kmsClient = KmsClient.builder().region(Region.US_EAST_1).build();
    static String encryptSourceStr = "this is source string for asymm enc";
    static String encryptStringUsingAsym =
            "qewHDGPcZpPGw2YgSjn88LLQ6C9HGbwcBmvNoz7hvWfRJAKNMoqqez4jmRGG/QTCQrX8s63hOS/tAM8AxGsNgliDZLr7QU1kQtuFPPIG2TdE4KSTVFSkyvsaS5iWsJKaRgQmt6XI+4AId+o1r0kVOctjGzdlCawpWhjN2oDyNb5oDvNzJW09wHkHzTylK5z7D9dfl00O1Ub90sm8d01A75ITU74c49K1MFbIFhZubWYL2mlr6SItI0wzT7dWAdr54Hg/VBZZdUsEvGEkq2Jp55VcYKjfiBZBK2HHz7MWkqB7tkXKsyBk9iTM6RoxrM24ACVuiGyOY7PSBAwjD/9AwA==";
    public static void main(String[] args) {
        System.out.println("hello kms asymmetric test   ");
        getPublickey();
    }

    private static void getPublickey() {
        GetPublicKeyRequest gpr = GetPublicKeyRequest.builder().keyId("alias/RajAsymEncDec").build();
        GetPublicKeyResponse gpresp= kmsClient.getPublicKey(gpr);
        String publicKey= Base64.getEncoder().encodeToString(gpresp.publicKey().asByteArray());
        System.out.println("Public Key: " + publicKey);
        encryptASym();
    }

    private static void encryptASym() {
        EncryptRequest asymEncReq = EncryptRequest.builder().
                keyId("alias/RajAsymEncDec")
                .encryptionAlgorithm(EncryptionAlgorithmSpec.RSAES_OAEP_SHA_256)
                .plaintext(SdkBytes.fromByteArray(encryptSourceStr.getBytes()))
                .build();
       EncryptResponse encryptResponse = kmsClient.encrypt(asymEncReq);
       String encryptStr = Base64.getEncoder().encodeToString(encryptResponse.ciphertextBlob().asByteArray());
       System.out.println(encryptStr);
       decryptAsym(encryptStr);

    }

    private static void decryptAsym(String decryptStr) {
        DecryptRequest decryptRequest = DecryptRequest.builder()
                .keyId("alias/RajAsymEncDec")
                .encryptionAlgorithm(EncryptionAlgorithmSpec.RSAES_OAEP_SHA_256)
                .ciphertextBlob(SdkBytes.fromByteArray(Base64.getDecoder().decode(decryptStr.getBytes())))
                .build();
        DecryptResponse decryptResponse = kmsClient.decrypt(decryptRequest);
        System.out.println(decryptResponse.plaintext().asUtf8String());
    }
}

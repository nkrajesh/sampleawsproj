package com.vanigaranaya.rajawsproj.kms;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.*;

import java.nio.charset.Charset;

public class KmsAsymGenAndVerify {

    static KmsClient kmsClient = KmsClient.builder().region(Region.US_EAST_1).build();
    static String signMessage = "Here is the message that will be digitally signed";
    public static void main(String[] args) {
        generateDigitallySigned(signMessage);
    }

    private static void generateDigitallySigned(String signMessage) {
        SdkBytes messageBytes = SdkBytes.fromString(signMessage, Charset.defaultCharset());
        SignRequest signRequest = SignRequest.builder()
                    .keyId("alias/RajAsymSignAndVerify")
                    .signingAlgorithm(SigningAlgorithmSpec.RSASSA_PKCS1_V1_5_SHA_512)
                    .message(messageBytes)
                    .build();
        SignResponse signResponse = kmsClient.sign(signRequest);
        System.out.println(signResponse.signature());
        VerifyRequest verifyRequest = VerifyRequest.builder()
                .keyId("alias/RajAsymSignAndVerify")
                .message(messageBytes)
                .signature(signResponse.signature())
                .signingAlgorithm(SigningAlgorithmSpec.RSASSA_PKCS1_V1_5_SHA_512)
                .build();
        VerifyResponse verifyResponse = kmsClient.verify(verifyRequest);
        System.out.println(verifyResponse.signatureValid());
    }
}

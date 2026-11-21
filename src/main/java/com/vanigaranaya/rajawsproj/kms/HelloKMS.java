package com.vanigaranaya.rajawsproj.kms;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsAsyncClient;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.*;

import java.nio.charset.Charset;
import java.util.Base64;

public class HelloKMS {
    static KmsClient kmsClient = KmsClient.builder().region(Region.US_EAST_1).build();
    public static void main(String[] args) {
        System.out.println("hello kms test");
        //listKmsKeys();
        //SpringApplication.run(RajAwsProjApplication.class, args);
        //encAndDec("this is example");
        generate();

    }

    private static void listKmsKeys() {
        System.out.println("listKmsKeys");
        //KmsClient kmsClient = KmsClient.builder().region(Region.US_EAST_1).build();
        if (kmsClient.listKeys() != null) {
            System.out.println("listKmsKeys successful..........size is..." + kmsClient.listKeys().keys().size());
            kmsClient.listKeys().keys().forEach((key) -> {
                DescribeKeyResponse dkr= kmsClient.describeKey(DescribeKeyRequest.builder().keyId(key.keyId()).build());
                if (KeyManagerType.CUSTOMER.equals(dkr.keyMetadata().keyManager())) {
                    System.out.println(key.keyId() + dkr.keyMetadata().toString());
                }
            });
        }
/*
        DescribeKeyResponse dkr= kmsClient.describeKey(DescribeKeyRequest.builder().keyId("alias/RajSymHmac").build());
        System.out.println(dkr.keyMetadata().toString());
*/
    }

    public static void encAndDec(String testMsg) {
        EncryptRequest encryptRequest = EncryptRequest.builder().keyId("alias/RajSymEncAndDec").plaintext(SdkBytes.fromByteArray(testMsg.getBytes())).build();
        EncryptResponse encryptResponse = kmsClient.encrypt(encryptRequest);
        String encryptedStr = Base64.getEncoder().encodeToString(encryptResponse.ciphertextBlob().asByteArray());
        System.out.println(encryptedStr);
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedStr);
        DecryptRequest decryptRequest = DecryptRequest.builder().keyId("alias/RajSymEncAndDec").ciphertextBlob(SdkBytes.fromByteArray(encryptedBytes)).build();
        DecryptResponse decryptResponse = kmsClient.decrypt(decryptRequest);
        System.out.println(decryptResponse.plaintext().asUtf8String());
    }

    public static void generate() {
        String message = "testmessage";
        String macForMessage = "jd9GOy7iDjFVEiSS8HHE5ZESnGImJ2neKrNrvj0EIaWSnyjlAZgOpRGu7DWTh4t8m16nak+rCaC7hMyktjOuyg==";
        GenerateMacRequest generateMacRequest =
                GenerateMacRequest.builder().
                        keyId("alias/RajSymHmac").
                        macAlgorithm(MacAlgorithmSpec.HMAC_SHA_512).
                        message(SdkBytes.fromByteArray(message.getBytes())).build();
        //GenerateMacResponse generateMacResponse = kmsClient.generateMac(generateMacRequest);
        //String messageMac= Base64.getEncoder().encodeToString(generateMacResponse.mac().asByteArray());
        //System.out.println(messageMac);
        byte[] macBytes = Base64.getDecoder().decode(macForMessage);
        VerifyMacRequest verifyMacRequest = VerifyMacRequest.builder().
                keyId("alias/RajSymHmac").
                macAlgorithm(MacAlgorithmSpec.HMAC_SHA_512).
                mac(SdkBytes.fromByteArray(macBytes)).
                message(SdkBytes.fromByteArray(message.getBytes())).
                build();
        try {
            VerifyMacResponse verifyMacResponse = kmsClient.verifyMac(verifyMacRequest);
            System.out.println(verifyMacResponse.macValid());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

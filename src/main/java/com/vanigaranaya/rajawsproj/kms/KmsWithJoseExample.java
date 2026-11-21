package com.vanigaranaya.rajawsproj.kms;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.*;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;

public class KmsWithJoseExample {

    static KmsClient kmsClient = KmsClient.builder().region(Region.US_EAST_1).build();
    static RSAKey rsaKeyForEnc;
    static RSAKey rsaKeyForSign;
    static Base64URL header;
    static Base64URL signature;
    static Base64URL payload;
    static RSAPublicKey rsaPublicKey;
    public static void main(String[] args) throws Exception {
        generateRSAKeys();
    }

    private static void generateRSAKeys() throws Exception {
        GetPublicKeyRequest gpr = GetPublicKeyRequest.builder().keyId("alias/RajAsymEncDec").build();
        GetPublicKeyResponse gpresp= kmsClient.getPublicKey(gpr);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(gpresp.publicKey().asByteArray());
        rsaPublicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
        rsaKeyForEnc = new RSAKey.Builder(rsaPublicKey).keyID(gpresp.keyId()).keyUse(KeyUse.ENCRYPTION)
                .algorithm(JWEAlgorithm.RSA_OAEP_256)
                .build();
        System.out.println(rsaKeyForEnc.toPublicJWK().toString());
        System.out.println(rsaKeyForEnc.toRSAKey().getAlgorithm().toString());

        GetPublicKeyRequest gpr1 = GetPublicKeyRequest.builder().keyId("alias/RajAsymSignAndVerify").build();
        GetPublicKeyResponse gpresp1= kmsClient.getPublicKey(gpr1);
        X509EncodedKeySpec spec1 = new X509EncodedKeySpec(gpresp1.publicKey().asByteArray());
        RSAPublicKey rsaPublicKey1 = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
        rsaKeyForSign = new RSAKey.Builder(rsaPublicKey).keyID(gpresp.keyId()).keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS512)
                .build();
        System.out.println(rsaKeyForSign.toPublicJWK().toString());
        System.out.println(rsaKeyForSign.toRSAKey().getAlgorithm().toString());
        SignedPayload();
    }

    private static void SignedPayload() throws  Exception{
        JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder()
                .subject("alice")
                .issueTime(new Date())
                .issuer("https://c2id.com")
                .claim("key1","value1")
                .claim("key2","value2")
                .build();
        JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS512).keyID(rsaKeyForSign.toPublicJWK().getKeyID()).build();
        header = jwsHeader.toBase64URL();
        payload = jwtClaims.toPayload().toBase64URL();
        String forsignature = header +"." +payload;
        byte[] signedBytes = signWithKms(kmsClient,"alias/RajAsymSignAndVerify",forsignature);
        signature = Base64URL.encode(signedBytes);
        String signedPayload = header +"." +payload +"." +signature;
        System.out.println("signedPayload.................");
        System.out.println(signedPayload);

        // ---------- 3️⃣ Encrypt signed JWS into JWE ----------
        JWEHeader jweHeader = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM)
                .contentType("JWT") // signals nested JWS
                .keyID(rsaKeyForEnc.toPublicJWK().getKeyID())
                .build();
        JWEObject jweObject = new JWEObject(jweHeader, new Payload(signedPayload));
        System.out.println(jweHeader.toString());

        byte[] cekBytes = generateCEK("alias/RajSymEncAndDec");
        SecretKey cek = new SecretKeySpec(cekBytes, "AES");

        jweObject.encrypt(new RSAEncrypter(rsaPublicKey,cek));
        String jweCompact = jweObject.serialize();
        System.out.println("✅ Encrypted JWE.................. ");
        System.out.println(jweCompact);

        JWEObject jweObjectForDecrypt = JWEObject.parse(jweCompact);
        jweObjectForDecrypt.decrypt(new DirectDecrypter(cek, true));
        System.out.println("Decrypted JWE: ..................."+jweObjectForDecrypt.serialize());
        String decryptedPayload = jweObjectForDecrypt.getPayload().toString();
        System.out.println("Decrypted nested JWS: " + decryptedPayload);

        //now verify jws

        SignedJWT signedJwtAfterDecrypt = SignedJWT.parse(jweObjectForDecrypt.getPayload().toString());
        VerifyRequest verifyRequest = VerifyRequest.builder()
                .keyId("alias/RajAsymSignAndVerify")
                .message(SdkBytes.fromByteArray(signedJwtAfterDecrypt.getSigningInput()))
                .signature(SdkBytes.fromByteArray(signedJwtAfterDecrypt.getSignature().decode()))
                .signingAlgorithm(SigningAlgorithmSpec.RSASSA_PKCS1_V1_5_SHA_512)
                .build();
        VerifyResponse verifyResponse = kmsClient.verify(verifyRequest);
        System.out.println(verifyResponse.signatureValid());

    }

    private static byte[] signWithKms(KmsClient kms, String keyId,String signMessage) {
        SdkBytes messageBytes = SdkBytes.fromString(signMessage, Charset.defaultCharset());
        SignResponse signResponse = kms.sign(SignRequest.builder()
                .keyId(keyId)
                .message(messageBytes)
                .signingAlgorithm(SigningAlgorithmSpec.RSASSA_PKCS1_V1_5_SHA_512)
                .build());
        return signResponse.signature().asByteArray();
    }

    private static byte[] decryptAsym(byte[] decryptStr) {
        DecryptRequest decryptRequest = DecryptRequest.builder()
                .keyId("alias/RajAsymEncDec")
                .encryptionAlgorithm(EncryptionAlgorithmSpec.RSAES_OAEP_SHA_256)
                .ciphertextBlob(SdkBytes.fromByteArray(decryptStr))
                .build();
        DecryptResponse decryptResponse = kmsClient.decrypt(decryptRequest);
        //System.out.println(decryptResponse.plaintext().asUtf8String());
        return  decryptResponse.plaintext().asByteArray();
    }

    private static byte[] generateCEK(String symKeyAlias) {
        GenerateDataKeyResponse dataKeyResp = kmsClient.generateDataKey(
                GenerateDataKeyRequest.builder()
                        .keyId(symKeyAlias)
                        .keySpec(DataKeySpec.AES_256)
                        .build()
        );
        return dataKeyResp.plaintext().asByteArray();
    }
}

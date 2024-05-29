package it.unisalento.pasproject.authservice.business.recovery;

import java.security.SecureRandom;
import java.util.Base64;

public class RecoveryUtils {

    public static String generateSafeToken(){
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return encoder.encodeToString(bytes);
    }
}

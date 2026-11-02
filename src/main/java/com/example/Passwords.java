package com.example;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Guarda sal y hash PBKDF2; nunca la contraseña en texto plano. */
public final class Passwords {
    private Passwords() { }
    public static String hash(String password) {
        byte[] salt=new byte[16]; new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt)+":"+Base64.getEncoder().encodeToString(derive(password,salt));
    }
    public static boolean matches(String password, String stored) {
        if (password == null || stored == null) return false;
        String[] parts=stored.split(":");
        return parts.length==2 && MessageDigest.isEqual(Base64.getDecoder().decode(parts[1]), derive(password,Base64.getDecoder().decode(parts[0])));
    }
    private static byte[] derive(String password, byte[] salt) {
        PBEKeySpec spec=new PBEKeySpec(password.toCharArray(),salt,120000,256);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        catch (java.security.GeneralSecurityException e) { throw new IllegalStateException(e); }
        finally { spec.clearPassword(); }
    }
}

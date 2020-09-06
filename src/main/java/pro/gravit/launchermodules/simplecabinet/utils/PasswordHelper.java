package pro.gravit.launchermodules.simplecabinet.utils;

import org.mindrot.jbcrypt.BCrypt;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.utils.helper.SecurityHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHelper {
    public static boolean verifyPassword(User.HashType type, String hashedPassword, String password)
    {
        if(type == null) throw new NullPointerException("password hash type must not null");
        switch (type)
        {
            case BCRYPT:
                return verifyBCryptPassword(hashedPassword, password);
            case DOUBLEMD5:
                return verifyDoubleMD5Password(hashedPassword, password);
            case MD5:
                return verifyMD5Password(hashedPassword, password);
            case SHA256:
                return verifySha256Password(hashedPassword, password);
            case AUTHMESHA256:
                return verifyAuthMeSha256Password(hashedPassword, password);
            default:
                throw new IllegalArgumentException(type.toString());
        }
    }
    public static boolean verifyBCryptPassword(String hashedPassword, String password)
    {
        return BCrypt.checkpw(password, "$2a" + hashedPassword.substring(3));
    }
    public static boolean verifyDoubleMD5Password(String hashedPassword, String password)
    {
        String firstMD5 = SecurityHelper.toHex(SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA256, password.getBytes()));
        String secondMD5 = SecurityHelper.toHex(SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA256, firstMD5.getBytes()));
        return secondMD5.equals(hashedPassword);
    }
    public static boolean verifyMD5Password(String hashedPassword, String password)
    {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            String firstMD5 = SecurityHelper.toHex(md.digest(password.getBytes()));
            return firstMD5.equals(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    public static boolean verifySha256Password(String hashedPassword, String password)
    {
        String hashed = SecurityHelper.toHex(SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA256, password));
        return hashed.equals(hashedPassword);
    }
    public static boolean verifyAuthMeSha256Password(String hashedPassword, String password)
    {
        String[] splited = hashedPassword.split("\\$");
        if(splited.length != 4) {
            return false;
        }
        String salt = splited[2];
        String saltedHash = splited[3];
        String checkHash = SecurityHelper.toHex(SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA256,
                SecurityHelper.toHex(SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA256, password))
                        .concat(salt)));
        return saltedHash.equals(checkHash);
    }
    public static String hashPassword(User.HashType type, String password)
    {
        if(type == null) throw new NullPointerException("password hash type must not null");
        switch (type) {
            case BCRYPT:
                return hashBCryptPassword(password);
            case DOUBLEMD5:
                return hashDoubleMD5Password(password);
            case MD5:
                return hashMD5Password(password);
            case SHA256:
                return hashSha256Password(password);
            case AUTHMESHA256:
                return null;
            default:
                throw new IllegalArgumentException(type.toString());
        }
    }
    public static String hashBCryptPassword(String password)
    {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
    public static String hashDoubleMD5Password(String password)
    {
        String firstMD5 = SecurityHelper.toHex(SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA256, password.getBytes()));
        return SecurityHelper.toHex(SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA256, firstMD5.getBytes()));

    }
    public static String hashMD5Password(String password)
    {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return SecurityHelper.toHex(md.digest(password.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    public static String hashSha256Password(String password)
    {
        return SecurityHelper.toHex(SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA256, password));
    }
}

package maico.addonbuu.utils.quick_access_server.provider;
public class EncryptionKeyManager {
    private final String salt = "addonbuu_secret_salt";
    public byte[] getPublic() { return salt.getBytes(); }
}

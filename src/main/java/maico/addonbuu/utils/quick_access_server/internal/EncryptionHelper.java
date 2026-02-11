package maico.addonbuu.utils.quick_access_server.internal;
public class EncryptionHelper {
    public static String obfuscate(String in) { return "obf_" + in.hashCode(); }
}

package maico.addonbuu.utils.quick_access_server.protocol;
public class NetworkBufferUtils {
    public static int calculateChecksum(String data) {
        return data.length() % 256;
    }
}

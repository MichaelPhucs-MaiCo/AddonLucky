package maico.addonbuu.utils;

import net.minecraft.client.MinecraftClient;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.apache.commons.codec.digest.DigestUtils;

public class SecurityUtils {
    // Public Key của Mai Cồ cung cấp
    private static final String PUBLIC_KEY_STR =
        "MIIBITANBgkqhkiG9w0BAQEFAAOCAQ4AMIIBCQKCAQB4UdVDjTkCcVSvRU6PnONh" +
            "3/9VCN1GFEP5yqs6spnac0ycyBpcBko7MSjP1nvJhA4KDqyx6X4ZJKETDkwB9CWc" +
            "AVpXEjwqnluSm4uZdEe2MpxuPmGPITij252JLwtW8FQkCdi/Uf6tjH3RE51nIoLS" +
            "iAxWD3xqf8iFjOQMru4U2I61s0BLtVWUf2c0sAGKZ4FrDScYFxCWOAydtkWeJNkh" +
            "sDMgY6f+Z8ZxpNrCjx1ODyrnHLG0kjDSjh9iR/x0Da7dB+QFlOFy/B0oVuYXE1vG" +
            "HP41FcvLCp+x89PNE3/jbwgb1pO28h8RkaXzS0PnyYijndRRHglqyoyi9oFAlNzD" +
            "AgMBAAE=";

    private static final File LICENSE_FILE = new File(MinecraftClient.getInstance().runDirectory, "addonbuu/license.dat");

    /**
     * Lấy 2 thông số cứng của máy: Tên máy + ID Vi xử lý
     */
    public static String getHWID() {
        String computerName = System.getenv("COMPUTERNAME") != null ? System.getenv("COMPUTERNAME") : "UnknownHost";
        String cpuId = System.getenv("PROCESSOR_IDENTIFIER") != null ? System.getenv("PROCESSOR_IDENTIFIER") : "UnknownCPU";

        // Kết hợp và băm SHA-256 cho nó chuyên nghiệp
        return DigestUtils.sha256Hex(computerName + "||" + cpuId);
    }

    /**
     * Kiểm tra License bằng thuật toán RSA
     */
    public static boolean isVerified() {
        try {
            if (!LICENSE_FILE.exists()) return false;

            String currentHWID = getHWID();
            byte[] signatureBytes = Base64.getDecoder().decode(Files.readString(LICENSE_FILE.toPath()).trim());

            // Tạo Public Key từ chuỗi String
            X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(Base64.getDecoder().decode(PUBLIC_KEY_STR));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(publicSpec);

            // Verify chữ ký
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(currentHWID.getBytes(StandardCharsets.UTF_8));

            return sig.verify(signatureBytes);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Tự động copy HWID vào Clipboard cho khách dễ gửi
     */
    public static void copyHWIDToClipboard() {
        String hwid = getHWID();
        MinecraftClient.getInstance().keyboard.setClipboard(hwid);
    }
}

package topg.Event_Platform.service;

import java.nio.file.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Hashtable;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class QRCodeGenerator {

    public static String generateQRCode(String eventName, String userName, String email, String ticketType,
                                      LocalDateTime issuedAt, String ticketId) throws WriterException, IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = issuedAt.format(formatter);

        String qrCodeData = "Event: " + eventName + "\n" +
                "User: " + (userName != null ? userName : email) + "\n" +
                "Issued At: " + formattedDate + "\n" +
                "Ticket Id: " + ticketId + "\n" +
                "Ticket Type: " + ticketType;

        int size = 250;
        String fileType = "PNG";

        String downloadsFolder = System.getProperty("user.home") + File.separator + "Downloads";
        File qrCodeDir = new File(downloadsFolder + File.separator + "qr_codes");
        if (!qrCodeDir.exists()) {
            qrCodeDir.mkdirs();
        }

        Hashtable<EncodeHintType, Object> hintMap = new Hashtable<>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

        MultiFormatWriter qrCodeWriter = new MultiFormatWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeData, BarcodeFormat.QR_CODE, size, size, hintMap);

        // Sanitize username and event name to be safe for filenames (remove spaces and special chars)
        String safeUserName = userName != null ? userName.replaceAll("[^a-zA-Z0-9]", "_") : "user";
        String safeEventName = eventName.replaceAll("[^a-zA-Z0-9]", "_");

        String timestamp = String.valueOf(System.currentTimeMillis());

        String fileName = safeUserName + "_" + safeEventName + "_" + timestamp + ".png";
        String filePath = qrCodeDir + File.separator + fileName;

        File outputFile = new File(filePath);
        ImageIO.write(toBufferedImage(bitMatrix), fileType, outputFile);

        System.out.println("QR code saved to: " + filePath);
        return filePath;
    }
    // Convert BitMatrix to BufferedImage
    private static BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? 0x000000 : 0xFFFFFF);  // Black or white pixels
            }
        }

        return image;
    }
}

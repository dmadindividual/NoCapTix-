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

    public static void generateQRCode(String eventName, String userName, String email, String ticketType, LocalDateTime issuedAt, String ticketId) throws WriterException, IOException {
        // Format the date to a readable format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = issuedAt.format(formatter);

        // Prepare the QR code data with all required information
        String qrCodeData = "Event: " + eventName + "\n" +
                "User: " + (userName != null ? userName : email) + "\n" +
                "Issued At: " + formattedDate + "\n" +
                "Ticket Id: " + ticketId + "\n" +
                "Ticket Type: " + ticketType;

        // QR Code parameters
        int size = 250;  // Size of the QR code
        String fileType = "PNG";  // Image type

        // Get the Downloads folder path
        String downloadsFolder = System.getProperty("user.home") + File.separator + "Downloads";
        // Create a directory for QR codes if it doesn't exist
        File qrCodeDir = new File(downloadsFolder + File.separator + "qr_codes");
        if (!qrCodeDir.exists()) {
            qrCodeDir.mkdirs();
        }

        // Set the QR code options
        Hashtable<EncodeHintType, Object> hintMap = new Hashtable<>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

        // Create the QR Code
        MultiFormatWriter qrCodeWriter = new MultiFormatWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeData, BarcodeFormat.QR_CODE, size, size, hintMap);

        // Set the file path for the QR code image
        String filePath = qrCodeDir + File.separator + "ticket_qr_" + System.currentTimeMillis() + ".png";

        // Write the QR code to the specified file
        File outputFile = new File(filePath);
        ImageIO.write(toBufferedImage(bitMatrix), fileType, outputFile);

        // Print the path where the QR code is saved
        System.out.println("QR code saved to: " + filePath);
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

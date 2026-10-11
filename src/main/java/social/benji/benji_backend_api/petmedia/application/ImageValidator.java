package social.benji.benji_backend_api.petmedia.application;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import social.benji.benji_backend_api.petmedia.domain.exception.MediaErrors;

/**
 * Validates uploaded images server-side: size ceiling, declared content type,
 * magic bytes and full decodability (also rejects truncated/corrupt files).
 * Returns the detected intrinsic dimensions so callers need not decode twice.
 */
@Component
class ImageValidator {

    static final long MAX_FILE_BYTES = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

    record Dimensions(int width, int height) {
    }

    byte[] validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw MediaErrors.emptyFile();
        }
        if (file.getSize() > MAX_FILE_BYTES) {
            throw MediaErrors.tooLarge(MAX_FILE_BYTES);
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw MediaErrors.unsupportedContentType(contentType);
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw MediaErrors.emptyFile();
        }
        if (!matchesMagicBytes(bytes, contentType.toLowerCase())) {
            throw MediaErrors.unsupportedContentType(contentType);
        }
        // Full decode proves the image is real and complete, not just well-headed.
        try {
            var image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                throw MediaErrors.corruptedImage();
            }
        } catch (IOException e) {
            throw MediaErrors.corruptedImage();
        }
        return bytes;
    }

    Dimensions detectDimensions(byte[] bytes) {
        try {
            var readers = ImageIO.getImageReaders(new javax.imageio.stream.MemoryCacheImageInputStream(
                    new ByteArrayInputStream(bytes)));
            if (readers.hasNext()) {
                var reader = readers.next();
                try {
                    reader.setInput(new javax.imageio.stream.MemoryCacheImageInputStream(
                            new ByteArrayInputStream(bytes)));
                    return new Dimensions(reader.getWidth(0), reader.getHeight(0));
                } finally {
                    reader.dispose();
                }
            }
        } catch (IOException ignored) {
            // Dimensions are best-effort metadata; absence must not fail the upload.
        }
        return null;
    }

    private boolean matchesMagicBytes(byte[] b, String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> b.length > 2 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8;
            case "image/png" -> b.length > 4
                    && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G';
            case "image/webp" -> b.length > 12
                    && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                    && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
            default -> false;
        };
    }
}

package org.example.yci_web.Controller;

import org.example.yci_web.Entity.ImageEntity;
import org.example.yci_web.Repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@RestController
public class ProductImageController {
    @Autowired
    ImageRepository imageRepository;

    @GetMapping(value = "/api/product/{productId}/image")
    public ResponseEntity<byte[]> getPrimaryProductImage(@PathVariable Long productId) {
        return imageRepository.findFirstByProductEntity_IdProductOrderByIdImageAsc(productId)
                .map(this::imageResponse)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping(value = "/api/product/{productId}/image/{imageId}")
    public ResponseEntity<byte[]> getProductImage(@PathVariable Long productId,
                                                  @PathVariable Long imageId) {
        return imageRepository.findByIdImageAndProductEntity_IdProduct(imageId, productId)
                .map(this::imageResponse)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    private ResponseEntity<byte[]> imageResponse(ImageEntity imageEntity) {
        byte[] image = normalizeImageBytes(imageEntity.getImage());
        if (image == null || image.length == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(detectMediaType(image));
        headers.setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());
        return new ResponseEntity<>(image, headers, HttpStatus.OK);
    }

    private byte[] normalizeImageBytes(byte[] image) {
        if (image == null || image.length == 0 || isKnownImage(image)) {
            return image;
        }

        byte[] base64Image = decodeBase64Image(image);
        if (base64Image != null) {
            return base64Image;
        }

        byte[] repairedImage = repairUtf8EncodedBinaryImage(image);
        if (repairedImage != null) {
            return repairedImage;
        }

        return image;
    }

    private byte[] decodeBase64Image(byte[] image) {
        String text = new String(image, StandardCharsets.UTF_8).replaceAll("\\s+", "");
        if (text.isBlank()) {
            return null;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(text);
            return isKnownImage(decoded) ? decoded : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private byte[] repairUtf8EncodedBinaryImage(byte[] image) {
        String text = new String(image, StandardCharsets.UTF_8);
        byte[] repaired = text.getBytes(StandardCharsets.ISO_8859_1);
        return isKnownImage(repaired) ? repaired : null;
    }

    private boolean isKnownImage(byte[] image) {
        return !MediaType.APPLICATION_OCTET_STREAM.equals(detectMediaType(image));
    }

    private MediaType detectMediaType(byte[] image) {
        if (image.length >= 8
                && image[0] == (byte) 0x89
                && image[1] == 0x50
                && image[2] == 0x4E
                && image[3] == 0x47) {
            return MediaType.IMAGE_PNG;
        }
        if (image.length >= 3
                && image[0] == (byte) 0xFF
                && image[1] == (byte) 0xD8
                && image[2] == (byte) 0xFF) {
            return MediaType.IMAGE_JPEG;
        }
        if (image.length >= 12
                && image[0] == 0x52
                && image[1] == 0x49
                && image[2] == 0x46
                && image[3] == 0x46
                && image[8] == 0x57
                && image[9] == 0x45
                && image[10] == 0x42
                && image[11] == 0x50) {
            return MediaType.parseMediaType("image/webp");
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}

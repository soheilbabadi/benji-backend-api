package social.benji.benji_backend_api.petmedia.domain.exception;

import org.springframework.http.HttpStatus;

/**
 * Error vocabulary for the pet-media module. Ownership violations surface as
 * NOT_FOUND so album/photo ids cannot be enumerated by other users.
 */
public final class MediaErrors {

    private MediaErrors() {
    }

    public static MediaException albumNotFound() {
        return new MediaException("ALBUM_NOT_FOUND", "Album not found.", HttpStatus.NOT_FOUND);
    }

    public static MediaException photoNotFound() {
        return new MediaException("PHOTO_NOT_FOUND", "Photo not found.", HttpStatus.NOT_FOUND);
    }

    public static MediaException coverNotInAlbum() {
        return new MediaException("COVER_PHOTO_NOT_IN_ALBUM",
                "The cover photo must belong to the same album.", HttpStatus.BAD_REQUEST);
    }

    public static MediaException albumsNotSamePet() {
        return new MediaException("ALBUMS_NOT_SAME_PET",
                "Photos can only move between albums of the same pet.", HttpStatus.BAD_REQUEST);
    }

    public static MediaException emptyFile() {
        return new MediaException("EMPTY_FILE", "The uploaded file is empty.", HttpStatus.BAD_REQUEST);
    }

    public static MediaException unsupportedContentType(String contentType) {
        return new MediaException("UNSUPPORTED_CONTENT_TYPE",
                "Unsupported media type: " + contentType, HttpStatus.BAD_REQUEST);
    }

    public static MediaException tooLarge(long maxBytes) {
        return new MediaException("FILE_TOO_LARGE",
                "The file exceeds the maximum allowed size of " + (maxBytes / (1024 * 1024)) + " MB.",
                HttpStatus.BAD_REQUEST);
    }

    public static MediaException corruptedImage() {
        return new MediaException("CORRUPTED_IMAGE",
                "The image could not be decoded and appears to be corrupted.", HttpStatus.BAD_REQUEST);
    }

    public static MediaException tooManyFiles(int max) {
        return new MediaException("TOO_MANY_FILES",
                "At most " + max + " files may be uploaded in one request.", HttpStatus.BAD_REQUEST);
    }
}

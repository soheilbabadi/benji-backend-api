package social.benji.benji_backend_api.service;

public class ObjectStorageException extends RuntimeException {
    public ObjectStorageException(String message) {
        super(message);
    }

    public ObjectStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}

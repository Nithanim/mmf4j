package me.nithanim.mmf4j;

public class MemoryMappingException extends RuntimeException {
    public MemoryMappingException(String message) {
        super(message);
    }

    public MemoryMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}

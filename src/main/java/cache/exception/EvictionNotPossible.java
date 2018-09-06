package cache.exception;

public class EvictionNotPossible extends Exception {

    private static final long serialVersionUID = -425924925343704182L;

    public EvictionNotPossible(String message) {
        super(message);
    }
}

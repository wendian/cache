package cache.hash;

public interface HashFunction {
    /**
     * Returns any integer, preferably a hash value
     *
     * @param o
     *            the object to be hashed.
     */
    int hash(Object o);
}

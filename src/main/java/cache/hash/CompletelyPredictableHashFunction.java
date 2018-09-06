package cache.hash;

public class CompletelyPredictableHashFunction implements HashFunction {
    int hash = 0;

    public void setNextHash(int next) {
        hash = next;
    }

    @Override
    public int hash(Object o) {
        return hash;
    }
}

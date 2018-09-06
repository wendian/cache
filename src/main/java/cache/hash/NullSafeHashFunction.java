package cache.hash;

import static java.util.Objects.isNull;

public class NullSafeHashFunction implements HashFunction {

    @Override
    public int hash(Object o) {
        return isNull(o) ? 0 : o.hashCode();
    }
}

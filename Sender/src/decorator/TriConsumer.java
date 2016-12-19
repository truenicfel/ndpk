
package decorator;


/**
 *
 * Consumes three parameters with no return value.
 * 
 * @author nico
 * @param <T>
 * @param <U>
 * @param <V>
 */
public interface TriConsumer<T, U, V> {




    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     * @param v the third input argument
     */
    void accept(T t, U u, V v);

}


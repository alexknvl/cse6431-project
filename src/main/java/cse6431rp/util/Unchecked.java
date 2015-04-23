package cse6431rp.util;

/**
 * Provides methods that allow one to subvert the Java checked exception system.
 */
public final class Unchecked {
    /**
     * Makes Java compiler forget about a checked exception.
     * @param e
     */
    public static void unchecked(Exception e) {
        Unchecked.<RuntimeException>unchecked0(e);
    }

    private static <T extends Exception> void unchecked0(Exception e) throws T {
        // No actual cast occurs here because of the type erasure.
        throw (T) e;
    }

    /**
     * Makes Java compiler think that there is a checked exception.
     * @param <T>
     * @throws T
     */
    public static <T extends Exception> void checked() throws T { }
}

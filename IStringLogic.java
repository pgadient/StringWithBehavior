package java.lang;

/**
* The {@code IStringLogic} class represents custom logic that can be applied to
* a String.
*
* @author  Christian Zuercher
*/
public interface IStringLogic {
    /**
     * Custom logic to be applied inside the toString method
     * 
     * @param s the string to verify
     * @throws StringNotMatchingLogicException if logic doesn't match
     * @return if necessary
     */
    public boolean applyBeforeToString(String s) throws StringNotMatchingLogicException;
    
    /**
     * Custom logic to be applied during initialization or the moment
     * the logic is added
     * 
     * @param s the string to verify
     * @throws StringNotMatchingLogicException if logic doesn't match
     * @return if necessary
     */
    public boolean applyOnInitialization(String s) throws StringNotMatchingLogicException;

    /**
     * Weather the string is inherited to its children
     * 
     * @param stt the string transformation to be checked
     * @return if the child of this transformation should inherit the logic
     */
    public boolean inheritToChild(StringTransformType stt);

    /**
     * Weather the string should keep track of its children
     * 
     * @return if the String keeps track of its children
     */
    public boolean recordHistory();
    
    /**
     * @return representation of the logic
     */
    public String getDescription();

    /**
     * Different types of transformations a string can go thrue to create a new one
     */
    public enum StringTransformType { 
        /** Nothing happened */
        NONE, 
        /** The string was simply copied */
        COPY,
        /** Something was added to the string */ 
        ADD, 
        /** Something was removed from the string */
        DELETE,
        /** Anything changed in the string */ 
        REPLACE,
        /** Split the string */
        SPLIT,
        /** Exported to another format (e.g. byte[], char[],...) */
        EXPORT
    }
}
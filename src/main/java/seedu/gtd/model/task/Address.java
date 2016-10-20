package seedu.gtd.model.task;


import seedu.gtd.commons.exceptions.IllegalValueException;

/**
 * Represents a Task's address in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidAddress(String)}
 */
public class Address {
    
    public static final String MESSAGE_ADDRESS_CONSTRAINTS = "Task addresses can be in any format";
    public static final String ADDRESS_VALIDATION_REGEX = ".*"; //Any value including null

    public final String value;

    /**
     * Validates given address.
     *
     * @throws IllegalValueException if given address string is invalid.
     */
    public Address(String address) throws IllegalValueException {
        //assert address != null;
        if (!isValidAddress(address)) {
            //throw new IllegalValueException(MESSAGE_ADDRESS_CONSTRAINTS);
        	//TODO: Find a way to print message to UI when value is autoset to null
        	this.value = null;
        }else{
        	this.value = address;
        }
    }

    /**
     * Returns true if a given string is a valid task email.
     */
    public static boolean isValidAddress(String test) {
        return test.matches(ADDRESS_VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof Address // instanceof handles nulls
                && this.value.equals(((Address) other).value)); // state check
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
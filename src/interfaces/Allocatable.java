package interfaces;

import exception.RescueException;

/** Anything that can be given out in units (stock items, camp beds). */
public interface Allocatable {
    int getAvailableQuantity();
    void allocate(int quantity) throws RescueException;
    void release(int quantity);
}

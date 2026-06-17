package emu;

public interface RetainedStorage {

    void persist(byte[] data);

    byte[] restore();

}

package nes;

public interface RetainedStorage {

    void persist(byte[] data);

    byte[] restore();

}

package api;

/**
 * Created by lkang on 11/15/17.
 */

public class NativeClassAPI {
    public native static int convertGray(long matAddrRgba, long matAddrGray);
    public native static double getSteeringAngle();
    public native static double getAcceleration();
    public native static byte[] fecEcode(byte[] data_blocks, int blockSize, int nrFecBlocks);
    public native static void initFEC();
}

package sunsetsatellite.sunsetutils.util;

public interface IFluidIO {

    int getActiveFluidSlotForSide(Direction dir);

    Connection getFluidIOForSide(Direction dir);
}

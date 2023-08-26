package sunsetsatellite.sunsetutils.util;

public interface IItemIO {

    int getActiveItemSlotForSide(Direction dir);

    Connection getItemIOForSide(Direction dir);
}

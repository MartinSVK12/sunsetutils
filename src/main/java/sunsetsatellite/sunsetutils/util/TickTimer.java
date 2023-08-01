package sunsetsatellite.sunsetutils.util;

import net.minecraft.core.util.helper.ReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TickTimer {
    public Object owner;
    public Method timeout;
    public int max = 0;
    public int value = 0;
    public boolean loop = true;

    public TickTimer(Object owner, String method, int max, boolean loop){
        Method timeout;
        try {
            timeout = owner.getClass().getMethod(method);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        this.owner = owner;
        this.timeout = timeout;
        this.max = max;
        this.loop = loop;
    }

    public void tick(){
        if(value >= 0){
            value++;
        }
        if(value >= max){
            if(loop){
                value = 0;
            } else {
                value = -1;
            }
            try {
                timeout.invoke(owner);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                if(e.getCause() instanceof Error){
                    throw new Error("Fatal error occurred when invoking method.");
                }
            }
        }
    }

    public void unpause(){
        value = 0;
    }

    public void pause(){
        value = -1;
    }

    public boolean isPaused(){
        return value == -1;
    }
}

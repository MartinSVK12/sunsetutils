package sunsetsatellite.sunsetutils.util.multiblocks;

import net.minecraft.src.*;
import org.lwjgl.opengl.GL11;
import sunsetsatellite.sunsetutils.SunsetUtils;
import sunsetsatellite.sunsetutils.util.Direction;
import sunsetsatellite.sunsetutils.util.RenderBlockSimple;
import sunsetsatellite.sunsetutils.util.Vec3i;

import java.util.Collection;

public class RenderMultiblock extends TileEntitySpecialRenderer {
    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double d, double e, double f, float g) {
        int i = tileEntity.xCoord;
        int j = tileEntity.yCoord;
        int k = tileEntity.zCoord;
        Direction dir = Direction.getDirectionFromSide(tileEntity.getBlockMetadata());
        World world = this.tileEntityRenderer.renderEngine.minecraft.theWorld;
        if(tileEntity instanceof IMultiblock){
            Collection blocks = ((IMultiblock) tileEntity).getMultiblock().data.getCompoundTag("Data").func_28110_c();
            Collection subs = ((IMultiblock) tileEntity).getMultiblock().data.getCompoundTag("Substitutions").func_28110_c();
            for (Object block : blocks) {
                Vec3i pos;
                int x = ((NBTTagCompound) block).getInteger("x");
                int y = ((NBTTagCompound) block).getInteger("y");
                int z = ((NBTTagCompound) block).getInteger("z");
                pos = new Vec3i(x,y,z).rotate(new Vec3i(i,j,k),dir);
                int id = Structure.getBlockId((NBTTagCompound) block);
                int meta = ((NBTTagCompound) block).getInteger("meta");
                if((Structure.getBlockId((NBTTagCompound) block) != tileEntity.getBlockType().blockID)){
                    if(world.getBlockId(pos.x,pos.y,pos.z) != id || (world.getBlockId(pos.x,pos.y,pos.z) == id && world.getBlockMetadata(pos.x,pos.y,pos.z) != meta)){
                        boolean foundSub = false;
                        for (Object sub : subs) {
                            int subX = ((NBTTagCompound) sub).getInteger("x");
                            int subY = ((NBTTagCompound) sub).getInteger("y");
                            int subZ = ((NBTTagCompound) sub).getInteger("z");
                            int subId = Structure.getBlockId((NBTTagCompound) sub);
                            int subMeta = ((NBTTagCompound) sub).getInteger("meta");
                            if(subX == x && subY == y && subZ == z){
                                if(world.getBlockId(pos.x,pos.y,pos.z) == subId && world.getBlockMetadata(pos.x,pos.y,pos.z) == subMeta){
                                    foundSub = true;
                                }
                            }
                        }
                        if(!foundSub){
                            GL11.glPushMatrix();
                            GL11.glDisable(GL11.GL_LIGHTING);
                            GL11.glColor4f(1f,0,0,1.0f);
                            GL11.glTranslatef((float)d+(pos.x-i), (float)e+(pos.y-j), (float)f+(pos.z-k));
                            drawBlock(this.getFontRenderer(),
                                    this.tileEntityRenderer.renderEngine,
                                    id,
                                    meta,
                                    i,
                                    j,
                                    k,
                                    tileEntity);
                            GL11.glEnable(GL11.GL_LIGHTING);
                            GL11.glPopMatrix();
                        }
                    }
                }
            }
        }

    }

    public void drawBlock(FontRenderer fontrenderer, RenderEngine renderengine, int i, int j, int x, int y, int z, TileEntity tile) {
        renderengine.bindTexture(renderengine.getTexture("/terrain.png"));
        Block f1 = Block.blocksList[i];
        GL11.glPushMatrix();
        this.blockRenderer.renderBlock(f1, j, renderengine.minecraft.theWorld, x, y, z);
        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    private final RenderBlockSimple blockRenderer = new RenderBlockSimple();
}

package sunsetsatellite.sunsetutils.util.multiblocks;

import net.minecraft.client.render.FontRenderer;
import net.minecraft.client.render.RenderEngine;
import net.minecraft.client.render.tileentity.TileEntityRenderer;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.world.World;
import org.lwjgl.opengl.GL11;
import sunsetsatellite.sunsetutils.util.BlockInstance;
import sunsetsatellite.sunsetutils.util.Direction;
import sunsetsatellite.sunsetutils.util.RenderBlockSimple;
import sunsetsatellite.sunsetutils.util.Vec3i;

import java.util.ArrayList;

public class RenderMultiblock extends TileEntityRenderer<TileEntity> {
    @Override
    public void doRender(TileEntity tileEntity, double d, double e, double f, float g) {
        int i = tileEntity.xCoord;
        int j = tileEntity.yCoord;
        int k = tileEntity.zCoord;
        Direction dir = Direction.getDirectionFromSide(tileEntity.getBlockMetadata());
        World world = this.renderDispatcher.renderEngine.minecraft.theWorld;
        if(tileEntity instanceof IMultiblock){
            Multiblock multiblock = ((IMultiblock) tileEntity).getMultiblock();
            ArrayList<BlockInstance> blocks = multiblock.getBlocks(new Vec3i(i, j, k),Direction.Z_POS); //TODO: multiblocks need to be made in the Z+ direction currently and that's stupid
            ArrayList<BlockInstance> substitutions = multiblock.getSubstitutions(new Vec3i(i, j, k),Direction.Z_POS);
            for (BlockInstance block : blocks) {
                if(!block.exists(world)){
                    boolean foundSub = substitutions.stream().anyMatch((BI)-> BI.pos.equals(block.pos) && BI.exists(world));
                    if(!foundSub){
                        GL11.glPushMatrix();
                        GL11.glDisable(GL11.GL_LIGHTING);
                        GL11.glColor4f(1f,0f,0f,1.0f);
                        GL11.glTranslatef((float)d+(block.pos.x-i), (float)e+(block.pos.y-j), (float)f+(block.pos.z-k));
                        drawBlock(this.getFontRenderer(),
                                this.renderDispatcher.renderEngine,
                                block.block.id,
                                block.meta == -1 ? 0 : block.meta,
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

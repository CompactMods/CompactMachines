package org.dave.CompactMachines.thirdparty.lookingglass;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;

import org.dave.CompactMachines.reference.Reference;
import org.dave.CompactMachines.tileentity.TileEntityMachine;

import com.xcompwiz.lookingglass.api.animator.ICameraAnimator;
import com.xcompwiz.lookingglass.api.view.IViewCamera;
import com.xcompwiz.lookingglass.api.view.IWorldView;

public class CameraAnimatorPlayer implements ICameraAnimator {
	private final IViewCamera camera;
	private final EntityPlayer player;
	private final TileEntityMachine reference;
	private ChunkCoordinates target;


	public CameraAnimatorPlayer(IWorldView worldview, TileEntityMachine reference) {
		this.camera = worldview.getCamera();
		this.reference = reference;
		this.player = Minecraft.getMinecraft().thePlayer;
	}

	@Override
	public void refresh() {
	}

	@Override
	public void setTarget(ChunkCoordinates target) {
		this.target = new ChunkCoordinates(target);
	}

	@Override
	public void update(float deltaTime) {
	    if (this.reference.getWorldObj().provider.dimensionId != this.player.worldObj.provider.dimensionId) {
			return;
		}

	    double dx = this.player.posX - (this.reference.xCoord + 0.5);
	    double dy = this.player.posY - (this.reference.yCoord + 0.5);
	    double dz = this.player.posZ - (this.reference.zCoord + 0.5);

	    double length = Math.sqrt(dx * dx + dz * dz + dy * dy);

	    double add = Reference.getBoxSize(reference.meta);

	    double offsetX = dx * (add / length);
	    double offsetY = dy * (add / length);
	    double offsetZ = dz * (add / length);

	    //LogHelper.info(String.format("x=%.2f, y=%.2f, z=%.2f", offsetX, offsetY, offsetZ));

	    double newX = target.posX + 0.5 + offsetX;
	    double newY = target.posY + offsetY;
	    double newZ = target.posZ + 0.5 + offsetZ;

	    camera.setPitch(player.rotationPitch);
	    camera.setYaw(player.rotationYaw);
	    camera.setLocation(newX, newY, newZ);
	}

}

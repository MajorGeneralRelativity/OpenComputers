package li.cil.oc.common.tileentity

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import li.cil.oc.common.item.data.PrintData
import li.cil.oc.util.ExtendedAABB
import li.cil.oc.util.ExtendedAABB._
import li.cil.oc.util.ExtendedNBT._
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection

class Print extends traits.TileEntity with traits.RedstoneAware {
  val data = new PrintData()

  var boundsOff = ExtendedAABB.unitBounds
  var boundsOn = ExtendedAABB.unitBounds
  var state = false

  _isOutputEnabled = true

  def activate(): Boolean = {
    if (data.stateOn.size > 0) {
      if (!state || !data.isButtonMode) {
        toggleState()
        return true
      }
    }
    false
  }

  def toggleState(): Unit = {
    state = !state
    world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, "random.click", 0.3F, if (state) 0.6F else 0.5F)
    world.markBlockForUpdate(x, y, z)
    if (data.emitRedstone) {
      ForgeDirection.VALID_DIRECTIONS.foreach(output(_, if (state) 15 else 0))
    }
    if (state && data.isButtonMode) {
      world.scheduleBlockUpdate(x, y, z, block, block.tickRate(world))
    }
  }

  override def canUpdate = false

  override protected def onRedstoneInputChanged(side: ForgeDirection, oldMaxValue: Int, newMaxValue: Int): Unit = {
    super.onRedstoneInputChanged(side, oldMaxValue, newMaxValue)
    if (!data.emitRedstone) {
      state = newMaxValue > 0
      world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, "random.click", 0.3F, if (state) 0.6F else 0.5F)
      world.markBlockForUpdate(x, y, z)
    }
  }

  override def readFromNBTForServer(nbt: NBTTagCompound): Unit = {
    super.readFromNBTForServer(nbt)
    data.load(nbt.getCompoundTag("data"))
    state = nbt.getBoolean("state")
    updateFromData()
  }

  override def writeToNBTForServer(nbt: NBTTagCompound): Unit = {
    super.writeToNBTForServer(nbt)
    nbt.setNewCompoundTag("data", data.save)
    nbt.setBoolean("state", state)
  }

  @SideOnly(Side.CLIENT)
  override def readFromNBTForClient(nbt: NBTTagCompound): Unit = {
    super.readFromNBTForClient(nbt)
    data.load(nbt.getCompoundTag("data"))
    state = nbt.getBoolean("state")
    updateFromData()
    world.markBlockForUpdate(x, y, z)
  }

  override def writeToNBTForClient(nbt: NBTTagCompound): Unit = {
    super.writeToNBTForClient(nbt)
    nbt.setNewCompoundTag("data", data.save)
    nbt.setBoolean("state", state)
  }

  def updateFromData(): Unit = {
    boundsOff = data.stateOff.drop(1).foldLeft(data.stateOff.headOption.fold(ExtendedAABB.unitBounds)(_.bounds))((a, b) => a.func_111270_a(b.bounds))
    if (boundsOff.volume == 0) boundsOff = ExtendedAABB.unitBounds
    boundsOn = data.stateOn.drop(1).foldLeft(data.stateOn.headOption.fold(ExtendedAABB.unitBounds)(_.bounds))((a, b) => a.func_111270_a(b.bounds))
    if (boundsOn.volume == 0) boundsOn = ExtendedAABB.unitBounds
  }
}

/*******************************************************************************
 * TileEntityTombstone.java
 * Copyright (c) 2014 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

/**
 * Defines the tombstone tile entity and how it behaves.
 */
public class TileEntityTombstone extends TileEntity
{
	/** The current line of the sign that is being edited. -1 for no selected line. */
	public int lineBeingEdited;

	/** Is the GUI open? */
	public boolean guiOpen;

	/** Has the tombstone synced with the server? */
	public boolean hasSynced;

	/** The text displayed on the tombstone. */
	public String signText[] = { "Here Lies", "", "", "" };

	/**
	 * Constructor
	 */
	public TileEntityTombstone()
	{
		lineBeingEdited = -1;
	}

	@Override
	public void updateEntity()
	{
		if (worldObj.isRemote && !hasSynced && !guiOpen)
		{
			hasSynced = true;
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		nbt.setString("Text1", signText[0]);
		nbt.setString("Text2", signText[1]);
		nbt.setString("Text3", signText[2]);
		nbt.setString("Text4", signText[3]);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);

		for (int i = 0; i < 4; i++)
		{
			signText[i] = nbt.getString(new StringBuilder().append("Text").append(i + 1).toString());

			if (signText[i].length() > 15)
			{
				signText[i] = signText[i].substring(0, 15);
			}
		}
	}
}

/*******************************************************************************
 * RenderHumanSmall.java
 * Copyright (c) 2014 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MCA Minecraft Mod license.
 ******************************************************************************/

package mca.client.render;

import mca.core.MCA;
import mca.core.WorldPropertiesList;
import mca.entity.AbstractChild;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;

import org.lwjgl.opengl.GL11;

/**
 * Determines how children are rendered.
 */
public class RenderHumanSmall extends RenderHuman
{
	/**
	 * Constructor
	 */
	public RenderHumanSmall()
	{
		super();
	}

	/**
	 * Renders the Entity scaled down to a child's size, depending on its age.
	 * 
	 * @param entity The entity being rendered.
	 * @param partialTickTime The time since the last in-game tick.
	 */
	protected void renderScale(AbstractChild entity, float partialTickTime)
	{
		final WorldPropertiesList properties = (WorldPropertiesList) MCA.getInstance().playerWorldManagerMap.get(Minecraft.getMinecraft().thePlayer.getCommandSenderName()).worldPropertiesInstance;
		final boolean doGradualGrowth = properties.childrenGrowAutomatically;
		final int age = entity.age;
		final float interval = entity.isMale ? 0.39F : 0.37F;
		final float growthFactor = interval / MCA.getInstance().getModProperties().kidGrowUpTimeMinutes * age;
		final float scale = 0.55F + growthFactor;

		if (entity.isSleeping && entity.hasBed)
		{
			renderHumanSleeping(entity, partialTickTime);

			GL11.glTranslated(0, -0.4F + growthFactor * scale, 0.1);
		}

		if (doGradualGrowth && !entity.isAdult)
		{
			//Children initially start at 0.55F as their scale. Divide the distance between the player's size and the child's size by
			//the amount of time it takes for them to grow and multiply that times their age. This makes the child gradually get taller
			//as they get older.
			GL11.glScalef(scale, scale, scale);
		}

		else if (entity.isAdult)
		{
			if (entity.isMale)
			{
				GL11.glScalef(0.9375F, 0.9375F, 0.9375F);
			}

			else
			{
				GL11.glScalef(0.915F, 0.915F, 0.915F);
			}
		}

		else
		{
			GL11.glScalef(0.55F, 0.55F, 0.55F);
		}
	}

	/**
	 * Called after the model and specials have been rendered. Applies additional tweaking to the rendered model.
	 * 
	 * @param entityLivingBase The entity being rendered.
	 * @param partialTickTime The time since the last in-game tick.
	 */
	@Override
	protected void preRenderCallback(EntityLivingBase entityLivingBase, float partialTickTime)
	{
		renderScale((AbstractChild) entityLivingBase, partialTickTime);
	}
}

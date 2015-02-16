package mca.ai;

import mca.core.MCA;
import mca.core.VersionBridge;
import mca.entity.EntityHuman;
import mca.enums.EnumBabyState;
import mca.enums.EnumProgressionStep;
import net.minecraft.nbt.NBTTagCompound;
import radixcore.constant.Time;
import radixcore.helpers.LogicHelper;
import radixcore.helpers.MathHelper;

public class AIProgressStory extends AbstractAI
{
	private int ticksUntilNextProgress;
	private int babyAge;
	private int numChildren;
	private boolean isDominant;
	private EnumProgressionStep progressionStep;

	public AIProgressStory(EntityHuman entityHuman) 
	{
		super(entityHuman);

		isDominant = true;
		ticksUntilNextProgress = MCA.getConfig().storyProgressionRate;
		progressionStep = EnumProgressionStep.SEARCH_FOR_PARTNER;
	}

	@Override
	public void onUpdateCommon() 
	{
	}

	@Override
	public void onUpdateClient() 
	{
	}

	@Override
	public void onUpdateServer() 
	{
		//This AI starts working once the story progression threshold defined in the configuration file has been met.
		if (owner.getTicksAlive() >= MCA.getConfig().storyProgressionThreshold * Time.MINUTE && isDominant && !owner.getIsChild())
		{
			if (ticksUntilNextProgress <= 0)
			{
				ticksUntilNextProgress = MCA.getConfig().storyProgressionRate * Time.MINUTE;

				if (LogicHelper.getBooleanWithProbability(75))
				{
					switch (progressionStep)
					{
					case FINISHED:
						break;
					case HAD_BABY:
						doAgeBaby();
						break;
					case TRY_FOR_BABY:
						doTryForBaby();
						break;
					case SEARCH_FOR_PARTNER:
						doPartnerSearch();
						break;
					case UNKNOWN:
						break;
					default:
						break;
					}
				}
			}

			else
			{
				ticksUntilNextProgress--;
			}
		}
	}

	@Override
	public void reset() 
	{
		owner.setTicksAlive(0);
		ticksUntilNextProgress = MCA.getConfig().storyProgressionRate;
		progressionStep = EnumProgressionStep.SEARCH_FOR_PARTNER;
		isDominant = true;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setInteger("ticksUntilNextProgress", ticksUntilNextProgress);
		nbt.setInteger("babyAge", babyAge);
		nbt.setBoolean("isDominant", isDominant);
		nbt.setInteger("numChildren", numChildren);
		nbt.setInteger("progressionStep", progressionStep.getId());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		ticksUntilNextProgress = nbt.getInteger("ticksUntilNextProgress");
		babyAge = nbt.getInteger("babyAge");
		isDominant = nbt.getBoolean("isDominant");
		numChildren = nbt.getInteger("numChildren");
		progressionStep = EnumProgressionStep.getFromId(nbt.getInteger("progressionStep"));
	}

	private void doPartnerSearch()
	{
		EntityHuman partner = (EntityHuman) LogicHelper.getNearestEntityOfTypeWithinDistance(EntityHuman.class, owner, 15);

		if (partner != null && partner.getIsMale() != owner.getIsMale() 
				&& !partner.getIsMarried() && !partner.getIsEngaged() && !partner.getIsChild() 
				&& (partner.getFatherId() != owner.getFatherId() || partner.getMotherId() != owner.getMotherId())) //TODO Test this
		{
			//Set the other human's story progression appropriately.
			AIProgressStory mateAI = getMateAI(partner);
			progressionStep = EnumProgressionStep.TRY_FOR_BABY;
			mateAI.progressionStep = EnumProgressionStep.TRY_FOR_BABY;

			//Set the dominant story progressor.
			if (owner.getIsMale())
			{
				this.isDominant = true;
				mateAI.isDominant = false;
			}

			else
			{
				this.isDominant = false;
				mateAI.isDominant = true;
			}

			//Mark both as married.
			owner.setIsMarried(true, partner);
			partner.setIsMarried(true, owner);
		}
	}

	private void doTryForBaby()
	{
		final EntityHuman mate = owner.getVillagerSpouse();

		if (LogicHelper.getBooleanWithProbability(50) && mate != null && MathHelper.getDistanceToEntity(owner, mate) <= 8.5D)
		{
			AIProgressStory mateAI = getMateAI(owner.getVillagerSpouse());
			progressionStep = EnumProgressionStep.HAD_BABY;
			mateAI.progressionStep = EnumProgressionStep.HAD_BABY;

			VersionBridge.spawnParticlesAroundEntityS("heart", owner, 16);
			VersionBridge.spawnParticlesAroundEntityS("heart", mate, 16);

			//Father's part is done, mother is now dominant for the baby's progression.
			isDominant = false;
			mateAI.isDominant = true;

			//Set baby state for the mother.
			mate.setBabyState(EnumBabyState.getRandomGender());
			
			//Increase number of children.
			numChildren++;
			mateAI.numChildren++;
		}
	}

	private void doAgeBaby()
	{
		final EntityHuman mate = owner.getVillagerSpouse();

		babyAge++;

		if (babyAge <= MCA.getConfig().babyGrowUpTime)
		{
			//Spawn the child.
			EntityHuman child;

			child = new EntityHuman(owner.worldObj, owner.getBabyState().isMale(), true, owner.getName(), owner.getSpouseName(), owner.getPermanentId(), owner.getSpouseId(), false);
			child.setPosition(owner.posX, owner.posY, owner.posZ);
			owner.worldObj.spawnEntityInWorld(child);

			//Reset self and mate status
			owner.setBabyState(EnumBabyState.NONE);
			babyAge = 0;
			progressionStep = EnumProgressionStep.FINISHED;

			if (mate != null)
			{
				AIProgressStory mateAI = getMateAI(mate);
				mateAI.progressionStep = EnumProgressionStep.FINISHED;
			}

			//Generate chance of trying for another baby, if mate is found.
			if (numChildren < 4 && LogicHelper.getBooleanWithProbability(50) && mate != null)
			{
				AIProgressStory mateAI = getMateAI(mate);
				mateAI.progressionStep = EnumProgressionStep.TRY_FOR_BABY;
				mateAI.isDominant = true;

				isDominant = false;
				progressionStep = EnumProgressionStep.TRY_FOR_BABY;
			}
		}
	}

	private AIProgressStory getMateAI(EntityHuman human)
	{
		return human.getAI(AIProgressStory.class);
	}
	
	public void setTicksUntilNextProgress(int value)
	{
		this.ticksUntilNextProgress = value;
	}
	
	public void setProgressionStep(EnumProgressionStep step)
	{
		this.progressionStep = step;
	}
	
	public void setDominant(boolean value)
	{
		this.isDominant = value;
	}
}
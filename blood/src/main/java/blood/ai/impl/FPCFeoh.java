package blood.ai.impl;

import java.util.ArrayList;
import java.util.List;

import l2s.commons.math.random.RndSelector;
import l2s.commons.util.Rnd;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill;

/**
 * 
 * @author mylove1412
 *
 * Logic inside Feoh AI
 * 
 * Select stance in situation ? -> not buff -> active by situation
 *
 */

public class FPCFeoh extends MysticPC
{
	public final int SKILL_ULTIMATE_BTM 	= 11064;
	public final int SKILL_MAGIC_EVASION 	= 11057;
	public final int SKILL_MAGIC_CHARGE 	= 11094;
	public final int SKILL_DEVIL_CURSE 		= 11047;
	public final int SKILL_DARK_CURSE		= 11150;
	
	public final int SKILL_ELEMENT_SPIKE 	= 11011;
	public final int SKILL_ELEMENT_CRASH 	= 11017;
	public final int SKILL_ELEMENT_DESTRUCTION = 11023;
	public final int SKILL_ELEMENT_BLAST	= 11034;
	public final int SKILL_ELEMENT_STORM	= 11040;
	
	public final int SKILL_DEATH_LORE		= 11030;
	public final int SKILL_DEATH_FEAR		= 11055;
	
	public final int SKILL_ELEMENT_BURST_HU	= 11106;
	public final int SKILL_ELEMENT_BURST_EL	= 11112;
	public final int SKILL_ELEMENT_BURST_DE	= 11118;
	
	public final int SKILL_FIRE_STANCE 	= 11007;
	public final int SKILL_WATER_STANCE	= 11008;
	public final int SKILL_WIND_STANCE 	= 11009;
	public final int SKILL_EARTH_STANCE	= 11010;
	
	public FPCFeoh(Player actor)
	{
		super(actor);
	}
	
	@Override 
	public List<Integer> getAllowSkill()
	{
		List<Integer> SkillList = new ArrayList<Integer>();
		
		// skill 4th
		// stance
		SkillList.add(SKILL_FIRE_STANCE);
		SkillList.add(SKILL_WATER_STANCE);
		SkillList.add(SKILL_WIND_STANCE);
		SkillList.add(SKILL_EARTH_STANCE);
		
		// Buff
		SkillList.add(SKILL_ULTIMATE_BTM);
		
		// damage skills
		SkillList.add(SKILL_ELEMENT_SPIKE);
		SkillList.add(SKILL_ELEMENT_CRASH);
		SkillList.add(SKILL_ELEMENT_DESTRUCTION);
//		SkillList.add(SKILL_ELEMENT_BURST);
//		SkillList.add(SKILL_ELEMENT_BLAST);
//		SkillList.add(SKILL_ELEMENT_STORM);
		SkillList.add(SKILL_DEVIL_CURSE);
		
		// debuff
//		SkillList.add(SKILL_DEATH_LORE);
//		SkillList.add(SKILL_MAGIC_EVASION);
//		SkillList.add(SKILL_MAGIC_CHARGE);
		
		return SkillList;
	}

	@Override
	protected boolean thinkBuff()
	{
//		if(thinkBuff(new int[] {
//			10757
//		}))
//			return true;
		// ultimate body to mind
		Player actor = getActor();
		Skill skill = getActor().getKnownSkill(SKILL_ULTIMATE_BTM);
		if(skill != null && actor.getCurrentHpPercents() > 40 && chooseTaskAndTargets(skill, actor, 0))
		{
			return true;
		}
		
		return super.thinkBuff();
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		Player actor = getActor();
		
		double distance = actor.getDistance(attacker);
		debug("get acttacked by: "+attacker+" in range:" + distance);
		if(distance < 100)
		{
			Skill skillMagicEvasion = actor.getKnownSkill(SKILL_MAGIC_EVASION);
			Skill skillMagicCharge = actor.getKnownSkill(SKILL_MAGIC_CHARGE);
			Skill skillDeathFear = actor.getKnownSkill(SKILL_DEATH_FEAR);
			if(canUseSkill(skillMagicEvasion, attacker))
				chooseTaskAndTargets(skillMagicEvasion, attacker, distance);
			else if(canUseSkill(skillMagicCharge, attacker))
				chooseTaskAndTargets(skillMagicCharge, attacker, distance);
			else if(canUseSkill(skillDeathFear, attacker))
				chooseTaskAndTargets(skillDeathFear, attacker, distance);
		}
		
		super.onEvtAttacked(attacker, damage);
	}
	
	protected boolean defaultFightTask()
	{
		clearTasks();
		
		Player actor = getActor();
		if (actor.isDead() || actor.isAMuted())
		{
			return false;
		}
		
		Creature target;
		if ((target = prepareTarget()) == null)
		{
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			return false;
		}
		
		debug("prepare target:" + target);
		
		if (actor.getServitors().length > 0){
			for (Servitor summon: actor.getServitors())
			{
				summon.getAI().Attack(target, true, false);
			}
		}
		
		
		double distance = actor.getDistance(target);
		double targetHp = target.getCurrentHpPercents();
		double actorHp = actor.getCurrentHpPercents();
		
		Skill skillDeathCurse = actor.getKnownSkill(SKILL_DEVIL_CURSE);
		Skill skillElementBurst = actor.getKnownSkill(SKILL_ELEMENT_BURST_DE);
		if (skillElementBurst == null) skillElementBurst = actor.getKnownSkill(SKILL_ELEMENT_BURST_EL);
		if (skillElementBurst == null) skillElementBurst = actor.getKnownSkill(SKILL_ELEMENT_BURST_HU);
		Skill skillElementDestruction = actor.getKnownSkill(SKILL_ELEMENT_DESTRUCTION);
		Skill skillElementSpike = actor.getKnownSkill(SKILL_ELEMENT_SPIKE);
		Skill skillElementCrash = actor.getKnownSkill(SKILL_ELEMENT_CRASH);
		Skill skillDeathFear = actor.getKnownSkill(SKILL_DEATH_FEAR);
		Skill skillDarkCurse = actor.getKnownSkill(SKILL_DARK_CURSE);
		
		if(distance < 400 && canUseSkill(skillDeathFear, target))
			chooseTaskAndTargets(skillDeathFear, target, distance);
		
		// if we are feoh soul taker we should debuf darkcurse first
		if(canUseSkill(skillDarkCurse, target, distance) && target.getEffectList().getEffectsCount(skillDarkCurse) == 0)
			return chooseTaskAndTargets(skillDarkCurse, target, distance);
		
		// 1st use death curse
		if(canUseSkill(skillDeathCurse, target, distance) && target.getEffectList().getEffectsCount(skillDeathCurse) == 0)
			return chooseTaskAndTargets(skillDeathCurse, target, distance);
		
		if(actor.getCurrentHpPercents() < 50)
		{
			// 1st if we success on destruction
			if(skillElementBurst != null && canUseSkill(skillElementBurst, target, distance))
				return chooseTaskAndTargets(skillElementBurst, target, distance);
			
			// use destruction for more DPS
			if(canUseSkill(skillElementDestruction, target, distance))
				return chooseTaskAndTargets(skillElementDestruction, target, distance);
		}
		
		// save mp skill
		if(canUseSkill(skillElementSpike, target, distance))
			return chooseTaskAndTargets(skillElementSpike, target, distance);
		
		// save mp skill
		if(canUseSkill(skillElementCrash, target, distance))
			return chooseTaskAndTargets(skillElementCrash, target, distance);
		
		// 1st if we success on destruction
		if(skillElementBurst != null && canUseSkill(skillElementBurst, target, distance))
			return chooseTaskAndTargets(skillElementBurst, target, distance);
		
		// use destruction
		if(canUseSkill(skillElementDestruction, target, distance))
			return chooseTaskAndTargets(skillElementDestruction, target, distance);
			
		Skill[] dam = Rnd.chance(getRateDAM()) ? selectUsableSkills(target, distance, _damSkills) : null;
		Skill[] dot = Rnd.chance(getRateDOT()) ? selectUsableSkills(target, distance, _dotSkills) : null;
		Skill[] debuff = targetHp > 10 ? Rnd.chance(getRateDEBUFF()) ? selectUsableSkills(target, distance, _debuffSkills) : null : null;
		Skill[] stun = Rnd.chance(getRateSTUN()) ? selectUsableSkills(target, distance, _stunSkills) : null;
		Skill[] heal = actorHp < 50 ? Rnd.chance(getRateHEAL()) ? selectUsableSkills(actor, 0, _healSkills) : null : null;
		Skill[] buff = Rnd.chance(getRateBUFF()) ? selectUsableSkills(actor, 0, _buffSkills) : null;
		
		RndSelector<Skill[]> rnd = new RndSelector<Skill[]>();
		if (!actor.isAMuted())
		{
			rnd.add(null, getRatePHYS());
		}
		
		if(dam != null && dam.length > 0)
			rnd.add(dam, getRateDAM());
		
		if(dot != null && dot.length > 0)
		rnd.add(dot, getRateDOT());
		
		if(debuff != null && debuff.length > 0)
		rnd.add(debuff, getRateDEBUFF());
		
		if(heal != null && heal.length > 0)
		rnd.add(heal, getRateHEAL());
		
		if(buff != null && buff.length > 0)
		rnd.add(buff, getRateBUFF());
		
		if(stun != null && stun.length > 0)
		rnd.add(stun, getRateSTUN());
		
		Skill[] selected = rnd.select();
		if (selected != null)
		{
			if ((selected == dam) || (selected == dot))
			{
				return chooseTaskAndTargets(selectTopSkillByDamage(actor, target, distance, selected), target, distance);
			}
			
			if ((selected == debuff) || (selected == stun))
			{
				return chooseTaskAndTargets(selectTopSkillByDebuff(actor, target, distance, selected), target, distance);
			}
			
			if (selected == buff)
			{
				return chooseTaskAndTargets(selectTopSkillByBuff(actor, selected), actor, distance);
			}
			
			if (selected == heal)
			{
				return chooseTaskAndTargets(selectTopSkillByHeal(actor, selected), actor, distance);
			}
		}
		
		// TODO make treatment and buff friendly targets
		
		return chooseTaskAndTargets(null, target, distance);
	}
	
	
	
	
}

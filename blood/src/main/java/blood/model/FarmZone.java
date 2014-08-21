package blood.model;

import java.util.ArrayList;
import java.util.List;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Player;
import l2s.gameserver.utils.Location;

public class FarmZone {
	
	public List<Location> _locations = new ArrayList<Location>();
	public int _min_level = 1;
	public int _max_level = 99;
	public boolean _is_party = false;
	public List<Integer> _class_ids = new ArrayList<Integer>();
	
	public FarmZone(int min_level, int max_level, boolean is_party){
		_min_level = min_level;
		_max_level = max_level;
		_is_party = is_party;
	}
	
	public void addLocation(Location loc){
		_locations.add(loc);
	}
	
	public void addClass(int class_id){
		_class_ids.add(class_id);
	}
	
	public Location getRndLocation(){
		return _locations.get(Rnd.get(_locations.size()));
	}
	
	public boolean checkLevel(int level){
		return _min_level <= level && level <= _max_level;
	}
	
	public boolean checkClass(int classId){
		return _class_ids.size() == 0 || _class_ids.contains(classId);
	}
	
	public boolean checkIsParty(boolean inParty){
		return _is_party == inParty;
	}
	
	public boolean isValid(Player player){
		System.out.println(player + " isValid level:"+checkLevel(player.getLevel()) + " class:"+checkClass(player.getClassId().getId())+ " party:"+checkIsParty(player.isInParty()));
		return checkLevel(player.getLevel()) && checkClass(player.getClassId().getId()) && checkIsParty(player.isInParty());
	}

	public boolean isParty() {
		// TODO Auto-generated method stub
		return _is_party;
	}

}

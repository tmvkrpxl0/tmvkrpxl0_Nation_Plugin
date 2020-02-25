package tmvkrpxl0.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class CustomPermission {
	private Map<UUID, Set<String>> Permissions;
	
	public void setPermissions(Map<UUID, Set<String>> permissions) {
		Permissions = permissions;
	}
	
	public Map<UUID, Set<String>> getPermissions(){
		return Permissions;
	}
	
	public CustomPermission() {
		Permissions = new HashMap<UUID, Set<String>>();
	}
	
	public CustomPermission(Map<UUID, Set<String>> permission) {
		Permissions = permission;
	}
}

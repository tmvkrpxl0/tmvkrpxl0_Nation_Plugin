package tmvkrpxl0;

import java.util.Set;

import org.bukkit.entity.Player;

public class PermissionManager {
	private static java.util.Map<java.util.UUID, Set<String>> permissions;
	protected static final String secondary = "kukga.secondary";
	protected static final String create = "kukga.create";
	
	protected PermissionManager() {
		permissions = ((tmvkrpxl0.Config.CustomPermission)KukgaMain.loadFile("권한.yml", tmvkrpxl0.Config.CustomPermission.class)).getPermissions();
	}
	
	protected void save() {
		KukgaMain.saveFile("권한.yml", tmvkrpxl0.Config.CustomPermission.class, new tmvkrpxl0.Config.CustomPermission(permissions));
	}
	
	protected static void setPermission(Player player, String perm) {
		Set<String> l = permissions.get(player.getUniqueId());
		if(l==null)l = new java.util.HashSet<>();
		l.add(perm);
		permissions.put(player.getUniqueId(), l);
	}
	
	protected static boolean getPermission(Player player, String perm) {
		Set<String> l = permissions.get(player.getUniqueId());
		if(l==null)return false;
		else return permissions.get(player.getUniqueId()).contains(perm);
	}
	
	protected static boolean getPermission(org.bukkit.command.CommandSender sender, String perm) {
		if(sender instanceof Player)return getPermission((Player)sender, perm);
		else return false;
	}
	
	protected static boolean haspermission(Player player, String perm) {
		return getPermission(player, perm);
	}
	
	protected static void unsetPermission(Player player, String perm) {
		Set<String> l = permissions.get(player.getUniqueId());
		if(l==null)l = new java.util.HashSet<>();
		else l.remove(perm);
		permissions.put(player.getUniqueId(), l);
	}
}

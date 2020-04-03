package tmvkrpxl0.Kukga;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class PermissionManager {
	private static Map<UUID, boolean[]> permissions;
	protected static final String secondary = "kukga.secondary";
	protected static final String create = "kukga.create";

	protected PermissionManager() {
		permissions = new LinkedHashMap<>();
		try {
			File f = new File(KukgaMain.plugin.getDataFolder() + File.separator + "권한.cfg");
			if(!f.exists()){
				f.getParentFile().mkdirs();
				f.createNewFile();
			}
			for(String s : Files.readAllLines(f.toPath(), StandardCharsets.UTF_8)){
				permissions.put(UUID.fromString(s.substring(s.indexOf("Player:")+7, s.indexOf(" Permissions:"))), new boolean[]{Boolean.parseBoolean(s.substring(s.indexOf("kukga.create=")+13, s.indexOf(" kukga.secondary="))), Boolean.parseBoolean(s.substring(s.indexOf("kukga.secondary=")+16))});
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void save() {
		try{
			File f = new File(KukgaMain.plugin.getDataFolder() + File.separator + "권한.cfg");
			if(!f.exists()){
				f.getParentFile().mkdirs();
				f.createNewFile();
			}
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8));
			for(UUID uuid : permissions.keySet()){
				writer.write("Player:" + uuid + " Permissions: kukga.create=" + permissions.get(uuid)[0] + " kukga.secondary=" + permissions.get(uuid)[1] + "\n");
			}
			writer.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	protected static void enablePermission(UUID uuid, String perm) {
		boolean[] l = permissions.get(uuid);
		if(l==null)l = new boolean[2];
		if(create.equals(perm))l[0] = true;
		else if(secondary.equals(perm))l[1] = true;
		permissions.put(uuid, l);
	}

	protected static boolean getPermission(@Nonnull UUID uuid,@Nonnull String perm) {
		boolean[] l = permissions.get(uuid);
		if(l==null)return false;
		else{
			if(create.equals(perm))return l[0];
			else if(secondary.equals(perm))return l[1];
		}
		return false;
	}

	protected static void disablePermission(UUID uuid, String perm){
		boolean[] l = permissions.get(uuid);
		if(l==null)l = new boolean[2];
		else {
			if(create.equals(perm))l[0] = false;
			else if(secondary.equals(perm))l[1] = false;
		}
		permissions.put(uuid, l);
	}
}

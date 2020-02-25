package tmvkrpxl0;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;

public class Patcher {
	public static FileSystem bukkit;
	public static FileSystem plug;
	private static CommandSender sender;
    public static void patch(CommandSender sender, String bukkitname, String kukganame) throws IOException{
    	Map<String, String> env = new LinkedHashMap<String, String>();
    	env.put("create", "true");
    	URI uri = URI.create("jar:" + Paths.get(bukkitname).toUri());
    	URI pl = URI.create("jar:" + Paths.get("plugins/" + kukganame).toUri());
    	Patcher.sender = sender;
    	bukkit = FileSystems.newFileSystem(uri, env);
    	plug = FileSystems.newFileSystem(pl, env);
    	sender.sendMessage("오래된 라이브러리를 제거합니다.");
    	DeleteDirectory("/org/yaml");
    	sender.sendMessage("제거가 끝났습니다. 이제 새로운 라이브러리를 추가합니다");
    	copyDirectory("/com/fasterxml", "/com");
		sender.sendMessage("50% 완료");
    	copyDirectory("/org/yaml", "/org");
    	sender.sendMessage("100완료 되었습니다");
    	bukkit.close();
    }
    ///com / -> /com /com -> /com/a.a /com -> /com/a.a /com/a.a
    public static void copyDirectory(String source, String target) throws IOException {
    	System.out.print("대상:" + source + ", 위치: " + target);
    	Path p = plug.getPath(source);
    	if(Files.isDirectory(p)) {
    		Files.list(p).forEach(x -> {
    			try {
					Files.createDirectories(bukkit.getPath(target + "/" + p.getFileName().toString()));
					copyDirectory(source + File.separator + x.getFileName().toString(), target + "/" + p.getFileName().toString());
				} catch (IOException e) {
					e.printStackTrace();
					sender.sendMessage("패치하는 도중 문제가 발생하였습니다!");
				}
    		});
    	}else {
    		if(!Files.exists(bukkit.getPath(target)))Files.createDirectories(bukkit.getPath(target));
    		Files.copy(p, bukkit.getPath(target + "/" + p.getFileName().toString()), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
    	}
    }
    
    public static void DeleteDirectory(String path) throws IOException {
    	Path p = bukkit.getPath(path);
    	System.out.println("제거대상:" + path);
    	if(Files.isDirectory(p)) {
    		Files.list(p).forEach(x -> {
				try {
					DeleteDirectory(path + "/" + x.getFileName().toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
    	}else {
    		Files.delete(p);
    	}
    }
}

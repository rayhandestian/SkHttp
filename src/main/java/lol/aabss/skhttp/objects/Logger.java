package lol.aabss.skhttp.objects;

import lol.aabss.skhttp.SkHttp;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Logger{

    public void debug(Object message){
        if (SkHttp.instance != null && SkHttp.instance.getConfig().getBoolean("debug", false)) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD +"[SkHttp] "
                    + ChatColor.translateAlternateColorCodes('&', "&r&7"+ message));
        }
    }

    public void success(Object message){
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD +"[SkHttp] "
                + ChatColor.translateAlternateColorCodes('&', "&r&a"+ message));
    }

    public void log(Object message){
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD +"[SkHttp] "
                + ChatColor.translateAlternateColorCodes('&', "&r&f"+ message));
    }

    public void warn(Object message){
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD +"[SkHttp] "
                + ChatColor.translateAlternateColorCodes('&', "&r&e"+ message));
    }

    public void error(Object message){
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD +"[SkHttp] "
                + ChatColor.translateAlternateColorCodes('&', "&r&c"+ message));
    }
}

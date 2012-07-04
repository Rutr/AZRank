package pl.azpal.azrank;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.TimeZone;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class Cfg {
    private AZRank plugin;
    protected final YamlConfiguration config = new YamlConfiguration();

    public String message = "+player is now a(n) +group for+time";
    public String aWhile = " a while";
    public String ever =  "ever";
    public boolean broadcastRankChange = true;
    public boolean allowOpsChanges = true;
    public boolean logEverything = false;
    public int checkInterval=10*20;
    public TimeZone timeZone = TimeZone.getDefault();
	
	protected Cfg(AZRank plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("CallToThreadDumpStack")
    protected boolean loadConfig() {
        try {
			config.load(plugin.yml);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
			return false;
		}

    	message = config.getString("message", message);
    	aWhile = config.getString("aWhile", aWhile);
    	ever =  config.getString("ever", ever);
    	broadcastRankChange = config.getBoolean("broadcastRankChange", broadcastRankChange);
    	allowOpsChanges = config.getBoolean("allowOpsChanges", allowOpsChanges);
        logEverything = config.getBoolean("logEverything", logEverything);
        checkInterval=20*config.getInt("checkInterval", checkInterval/20);
        
        plugin.log.info("[AZRank][Config]option: 'message' is: " + message);
        plugin.log.info("[AZRank][Config]option: 'aWhile' is: " + aWhile);
        plugin.log.info("[AZRank][Config]option: 'ever' is: " + ever);
        plugin.log.info("[AZRank][Config]option: 'broadcastRankChange' is: " + broadcastRankChange);
        plugin.log.info("[AZRank][Config]option: 'allowOpsChanges' is: " + allowOpsChanges);
        plugin.log.info("[AZRank][Config]option: 'logEverything' is: " + logEverything);
        plugin.log.info("[AZRank][Config]option: 'checkInterval' is: " + (checkInterval/20) + "seconds");
        return true;
    }

    protected void defaultConfig() {    	
    	config.set("message", message);
    	config.set("aWhile", aWhile);
    	config.set("ever", ever);
    	config.set("broadcastRankChange", broadcastRankChange);
    	config.set("allowOpsChanges", allowOpsChanges);
        config.set("logEverything", logEverything);
        config.set("checkInterval", checkInterval/20);

        try {
			config.save(plugin.yml);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    protected void checkConfig() {
    	plugin.debugmsg("Checking CFG");
        try {
			config.load(plugin.yml);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
        boolean hasChanged=false;
        if (config.get("message") == null) {
            config.set("message", message);
            hasChanged = true;
            plugin.debugmsg("Fixing 'message' in cfg");}
        if (config.get("aWhile") == null) {
            config.set("aWhile", aWhile);
            hasChanged = true;
            plugin.debugmsg("Fixing 'a while' in cfg");}
        if (config.get("ever") == null) {
            config.set("ever", ever);
            hasChanged = true;
            plugin.debugmsg("Fixing 'ever' cfg");}
        if (config.get("broadcastRankChange") == null) {
            config.set("broadcastRankChange", broadcastRankChange);
            hasChanged = true;
            plugin.debugmsg("Fixing 'broadcastRankChange' cfg");}
        if (config.get("allowOpsChanges") == null) {
            config.set("allowOpsChanges", allowOpsChanges);
            hasChanged = true;
            plugin.debugmsg("Fixing 'allowOpsChanges' cfg");}
        if (config.get("logEverything") == null) {
            config.set("logEverything", logEverything);
            hasChanged = true;
            plugin.debugmsg("Fixing 'logEverything' cfg");}
        if (config.get("checkInterval") == null) {
            config.set("checkInterval", checkInterval/20);
            hasChanged = true;
            plugin.debugmsg("Fixing 'checkInterval' cfg");}
        else if (config.getInt("checkInterval")<1) {
            config.set("checkInterval", checkInterval/20);
            hasChanged = true;
            plugin.debugmsg("Fixing 'checkInterval' cfg");}

         
        if (hasChanged) {
            //plugin.logIt("the config has been updated :D");
            try {
				config.save(plugin.yml);
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }
}

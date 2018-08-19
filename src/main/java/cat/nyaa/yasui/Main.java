package cat.nyaa.yasui;

import cat.nyaa.nyaacore.utils.ReflectionUtils;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class Main extends JavaPlugin {

    public Configuration config;
    public I18n i18n;
    public CommandHandler commandHandler;
    public ArrayList<String> disableAIWorlds = new ArrayList<>();
    public TPSMonitor tpsMonitor;
    //public Essentials ess;
    public Set<UUID> noAIMobs = new HashSet<>();
    public EntityListener entityListener;

    @Override
    public void onEnable() {
        config = new Configuration(this);
        config.load();
        i18n = new I18n(this, this.config.language);
        i18n.load();
        commandHandler = new CommandHandler(this, this.i18n);
        getCommand("yasui").setExecutor(commandHandler);
        getCommand("yasui").setTabCompleter(commandHandler);
//        if (getServer().getPluginManager().getPlugin("Essentials") != null) {
//            this.ess = (Essentials) getServer().getPluginManager().getPlugin("Essentials");
//        }
        tpsMonitor = new TPSMonitor(this);
        entityListener = new EntityListener(this);
    }

    @Override
    public void onDisable() {
        disable(true);
    }

    public void disable(boolean saveConfig) {
        getServer().getScheduler().cancelTasks(this);
        getCommand("yasui").setExecutor(null);
        getCommand("yasui").setTabCompleter(null);
        HandlerList.unregisterAll(this);
        if (saveConfig) {
            config.save();
        }
    }

    public void disableAI() {
        for (World world : getServer().getWorlds()) {
            if (config.ignored_world.contains(world.getName())) {
            if (world.getLivingEntities().size() >= this.config.world_entity) {
                if (!disableAIWorlds.contains(world.getName())) {
                    disableAIWorlds.add(world.getName());
                    getLogger().info("disable entity ai in " + world.getName());
                    Bukkit.broadcastMessage("��aYasui ��b>>��d ��ͣ�� ��e"+world.getName()+" ��d�������������������������� ��e"+this.config.chunk_entity+" ��d�����������AI");
                }
                for (Chunk chunk : world.getLoadedChunks()) {
                    int entityCount = getLivingEntityCount(chunk);
                    if (entityCount >= this.config.chunk_entity) {
                        for (Entity entity : chunk.getEntities()) {
                            if (entity instanceof LivingEntity) {
                                setFromMobSpawner((LivingEntity) entity, true);
                            }
                        }
                    }
                }
            }
        }
      }
    }

    public void enableAI() {
        for (World world : getServer().getWorlds()) {
            if (!disableAIWorlds.contains(world.getName())) {
                continue;
            } else {
                disableAIWorlds.remove(world.getName());
            }
            getLogger().info("enable entity ai in " + world.getName());
            Bukkit.broadcastMessage("��aYasui ��b>>��d ������ ��e"+world.getName()+" ��d���������б�ͣ�õ�����AI");
            for (Chunk chunk : world.getLoadedChunks()) {
                for (Entity entity : chunk.getEntities()) {
                    if (entity instanceof LivingEntity) {
                        setFromMobSpawner((LivingEntity) entity, false);
                    }
                }
            }
        }
    }

    public int getLivingEntityCount(Chunk chunk) {
        int entityCount = 0;
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof LivingEntity && !(entity instanceof ArmorStand)) {
                entityCount++;
            }
        }
        return entityCount;
    }
    
    public void setFromMobSpawner(LivingEntity entity, boolean fromMobSpawner) {
        try {
            if (entity.isValid() && !(entity instanceof ArmorStand)) {
                Class craftEntityClazz = ReflectionUtils.getOBCClass("entity.CraftEntity");
                Method getNMSEntityMethod = craftEntityClazz.getMethod("getHandle");
                Object e = getNMSEntityMethod.invoke(entity);
                Class nmsEntityClazz = ReflectionUtils.getNMSClass("Entity");
                Field field = nmsEntityClazz.getField("fromMobSpawner");
                field.setBoolean(e, fromMobSpawner);
                if (fromMobSpawner) {
                    if (!noAIMobs.contains(entity.getUniqueId())) {
                        noAIMobs.add(entity.getUniqueId());
                    }
                } else {
                    noAIMobs.remove(entity.getUniqueId());
                }
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        disable(false);
        onEnable();
    }
}

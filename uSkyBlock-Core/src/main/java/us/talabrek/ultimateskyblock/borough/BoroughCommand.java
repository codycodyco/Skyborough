package us.talabrek.ultimateskyblock.borough;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static us.talabrek.ultimateskyblock.borough.BoroughSettings.*;

/** Administrator setup and a small, home-focused player entry point. */
public final class BoroughCommand implements CommandExecutor {
    public static final String REGION = "skyborough_city";
    private final uSkyBlock plugin;
    private final Map<UUID, Location> first = new HashMap<>();
    private final Map<UUID, Location> second = new HashMap<>();
    private boolean generating;

    public BoroughCommand(uSkyBlock plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("请在游戏内使用此命令。");
            return true;
        }
        if (!enabled(plugin)) {
            player.sendMessage("请先在 config.yml 中启用 skyborough.enabled 并重启服务器。");
            return true;
        }
        if (!plugin.isRequirementsMet(player, null)) return true;
        String action = args.length == 0 ? "help" : args[0].toLowerCase(java.util.Locale.ROOT);
        if (action.equals("claim") || action.equals("home")) {
            if (!ready(plugin)) {
                player.sendMessage("主城尚未设置完成，请联系管理员。");
                return true;
            }
            player.performCommand(action.equals("claim") ? "is create" : "is home");
            return true;
        }
        if (action.equals("spawn")) {
            if (!ready(plugin)) player.sendMessage("主城尚未设置完成。");
            else plugin.getTeleportLogic().spawnTeleport(player, true);
            return true;
        }
        if (!player.hasPermission("skyborough.admin")) {
            player.sendMessage("/borough claim 申请家园；/borough home 回家；/borough spawn 返回主城");
            return true;
        }
        if (generating) {
            player.sendMessage("主城正在检查或生成，请等待完成。");
            return true;
        }
        if (action.equals("pos1") || action.equals("pos2")) {
            (action.equals("pos1") ? first : second).put(player.getUniqueId(), player.getLocation().clone());
            player.sendMessage("已记录 " + action + "。在主城对角位置记录另一点，然后 /borough register。");
            return true;
        }
        try {
            if (action.equals("finish")) {
                BoroughBounds b = bounds(plugin);
                Location location = player.getLocation();
                if (b == null || ready(plugin) || !location.getWorld().equals(plugin.getWorldManager().getWorld())
                    || location.getX() < b.minX() || location.getX() >= (long) b.maxX() + 1
                    || location.getZ() < b.minZ() || location.getZ() >= (long) b.maxZ() + 1 || !safeSpawn(location))
                    throw new IllegalArgumentException("仅可在中断生成的主城内、安全地面上使用 finish。请先检查地形是否完整。");
                protect(location.getWorld(), b);
                location.getWorld().setSpawnLocation(location);
                persist(b, true);
                player.sendMessage("已确认主城可用，并设置当前位置为出生点。");
            } else if (action.equals("register")) {
                Location a = first.get(player.getUniqueId()), b = second.get(player.getUniqueId());
                if (a == null || b == null || !a.getWorld().equals(b.getWorld())) {
                    player.sendMessage("请先在同一世界用 /borough pos1 和 /borough pos2 标记主城水平范围。");
                    return true;
                }
                BoroughBounds bounds = new BoroughBounds(Math.min(a.getBlockX(), b.getBlockX()), Math.min(a.getBlockZ(), b.getBlockZ()),
                    Math.max(a.getBlockX(), b.getBlockX()), Math.max(a.getBlockZ(), b.getBlockZ()));
                validate(player, a.getWorld(), bounds);
                if (!bounds.intersects(player.getLocation().getBlockX(), player.getLocation().getBlockZ(), 1, 0)
                    || !player.getWorld().equals(a.getWorld()) || !safeSpawn(player.getLocation())) {
                    throw new IllegalArgumentException("请站在选区内安全地面上登记，此处会成为主城传送点。");
                }
                protect(a.getWorld(), bounds);
                persist(bounds, true);
                a.getWorld().setSpawnLocation(player.getLocation());
                player.sendMessage("主城已登记，建筑保持原样；玩家现在可用 /borough claim 申请外围家园。");
            } else if (action.equals("generate")) {
                int radius = args.length > 1 ? Integer.parseInt(args[1]) : 48;
                if (radius < 16 || radius > 96) throw new IllegalArgumentException("主城半径应在 16 到 96 之间。");
                Location center = player.getLocation().getBlock().getLocation();
                World world = center.getWorld();
                BoroughBounds bounds = new BoroughBounds(center.getBlockX() - radius, center.getBlockZ() - radius,
                    center.getBlockX() + radius, center.getBlockZ() + radius);
                validate(player, world, bounds);
                int bottom = center.getBlockY() - radius / 2;
                if (bottom < world.getMinHeight() || center.getBlockY() + 4 >= world.getMaxHeight())
                    throw new IllegalArgumentException("当前位置高度不足以生成主城。");
                generate(player, center, bounds, radius, bottom);
            } else {
                player.sendMessage("玩家：/borough claim | home | spawn");
                player.sendMessage("已有主城：/borough pos1 → pos2 → 站在安全出生点 /borough register");
                player.sendMessage("新建主城：飞到空旷位置 /borough generate [半径16-96]（默认48）");
            }
        } catch (Exception e) {
            player.sendMessage("主城设置失败：" + e.getMessage());
            plugin.getLogger().warning("Skyborough setup: " + e.getMessage());
        }
        return true;
    }

    private boolean safeSpawn(Location location) {
        return location.getBlock().isPassable() && location.clone().add(0, 1, 0).getBlock().isPassable()
            && location.clone().subtract(0, 1, 0).getBlock().getType().isSolid();
    }

    private RegionManager manager(World world) {
        RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        if (manager == null) throw new IllegalStateException("WorldGuard 区域管理器不可用。");
        return manager;
    }

    private ProtectedCuboidRegion region(World world, BoroughBounds b) {
        return new ProtectedCuboidRegion(REGION, BlockVector3.at(b.minX(), world.getMinHeight(), b.minZ()),
            BlockVector3.at(b.maxX(), world.getMaxHeight() - 1, b.maxZ()));
    }

    private void validate(Player player, World world, BoroughBounds b) {
        if (!world.equals(plugin.getWorldManager().getWorld()))
            throw new IllegalArgumentException("请在 options.general.worldName 指定的家园世界设置主城。");
        if (bounds(plugin) != null || manager(world).hasRegion(REGION))
            throw new IllegalArgumentException("主城已登记。为保护现有家园，本版本不允许直接移动或覆盖主城。");
        if (!world.getWorldBorder().isInside(new Location(world, b.minX(), player.getLocation().getY(), b.minZ()))
            || !world.getWorldBorder().isInside(new Location(world, b.maxX(), player.getLocation().getY(), b.maxZ())))
            throw new IllegalArgumentException("主城范围超出世界边界。");
        for (var existing : manager(world).getApplicableRegions(region(world, b))) {
            if (!existing.getId().equals("__global__"))
                throw new IllegalArgumentException("选区与已有保护区域重叠：" + existing.getId());
        }
    }

    private void protect(World world, BoroughBounds bounds) throws Exception {
        var region = region(world, bounds);
        region.setPriority(100);
        region.setFlag(Flags.BLOCK_BREAK, StateFlag.State.DENY);
        region.setFlag(Flags.BLOCK_PLACE, StateFlag.State.DENY);
        region.setFlag(Flags.PVP, StateFlag.State.DENY);
        region.setFlag(Flags.CREEPER_EXPLOSION, StateFlag.State.DENY);
        region.setFlag(Flags.TNT, StateFlag.State.DENY);
        region.setFlag(Flags.FIRE_SPREAD, StateFlag.State.DENY);
        region.setFlag(Flags.MOB_SPAWNING, StateFlag.State.DENY);
        manager(world).addRegion(region);
        manager(world).save();
    }

    private void persist(BoroughBounds b, boolean ready) {
        var c = plugin.getConfig();
        c.set("skyborough.city.min-x", b.minX()); c.set("skyborough.city.min-z", b.minZ());
        c.set("skyborough.city.max-x", b.maxX()); c.set("skyborough.city.max-z", b.maxZ());
        c.set("skyborough.city.registered", true); c.set("skyborough.city.ready", ready);
        plugin.saveConfig();
    }

    /** Two bounded passes: refuse occupied space before any block edits, then build in batches. */
    private void generate(Player player, Location center, BoroughBounds b, int radius, int bottom) {
        generating = true;
        player.sendMessage("正在检查主城空间；检查通过后分批生成草地浮空岛。请勿在此区域放置方块。");
        new BukkitRunnable() {
            int x = b.minX(), z = b.minZ(), y = bottom;
            boolean building;
            @Override public void run() {
                try {
                    for (int budget = 0; budget < 2048; budget++) {
                        World world = center.getWorld();
                        var block = world.getBlockAt(x, y, z);
                        if (!building && !block.getType().isAir()) {
                            player.sendMessage("生成取消：范围内有已有方块，未修改建筑。请选择空旷位置。");
                            stop(); return;
                        }
                        if (building) {
                            double distance = Math.hypot(x - center.getBlockX(), z - center.getBlockZ());
                            int depth = Math.max(1, (int) ((1 - distance / radius) * (radius / 2.0)));
                            if (distance <= radius && y <= center.getBlockY() && y > center.getBlockY() - depth) {
                                if (!block.getType().isAir()) throw new IllegalStateException("生成期间出现新方块，已停止；请管理员检查部分生成的主城。");
                                block.setType(y == center.getBlockY() ? Material.GRASS_BLOCK
                                    : y >= center.getBlockY() - 3 ? Material.DIRT : Material.STONE, false);
                            }
                        }
                        if (++y > center.getBlockY() + 3) {
                            y = bottom;
                            if (++z > b.maxZ()) { z = b.minZ(); x++; }
                        }
                        if (x > b.maxX()) {
                            if (!building) {
                                validate(player, world, b);
                                protect(world, b); persist(b, false);
                                building = true; x = b.minX(); z = b.minZ(); y = bottom;
                            } else {
                                world.setSpawnLocation(center.clone().add(0.5, 1, 0.5));
                                persist(b, true);
                                player.sendMessage("浮空主城地形生成完成，可继续建造。玩家可用 /borough claim 申请外围家园。");
                                stop(); return;
                            }
                        }
                    }
                } catch (Exception e) {
                    player.sendMessage("主城生成停止：" + e.getMessage());
                    plugin.getLogger().severe("Skyborough generation stopped: " + e.getMessage());
                    stop();
                }
            }
            private void stop() { generating = false; cancel(); }
        }.runTaskTimer(plugin, 1, 1);
    }
}

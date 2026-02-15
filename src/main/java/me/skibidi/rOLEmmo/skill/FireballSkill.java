package me.skibidi.rolemmo.skill;

import me.skibidi.rolemmo.ROLEmmo;
import me.skibidi.rolemmo.manager.ClanCoreManager;
import me.skibidi.rolemmo.model.Role;
import me.skibidi.rolemmo.model.Skill;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

/**
 * DPS Fireball Skill
 * Hiện cầu lửa sau lưng và bắn vào mục tiêu phía trước
 */
public class FireballSkill extends Skill {

    private final ROLEmmo plugin;

    public FireballSkill(ROLEmmo plugin) {
        super("fireball", "Cầu Lửa", Role.DPS, 6);
        this.plugin = plugin;
    }

    @Override
    public String getDescription() {
        return "§7Bắn cầu lửa về phía trước, gây damage và hiệu ứng đốt cho mục tiêu";
    }

    @Override
    public SkillLevelInfo getLevelInfo(int level) {
        if (level < 1 || level > maxLevel) {
            level = Math.max(1, Math.min(level, maxLevel));
        }

        // Config theo yêu cầu
        int damage;
        int fireballCount;
        int cooldown;
        double burnDurationPercent;

        switch (level) {
            case 1 -> {
                damage = 10;
                fireballCount = 5;
                cooldown = 12;
                burnDurationPercent = 0.0;
            }
            case 2 -> {
                damage = 19;
                fireballCount = 5;
                cooldown = 11;
                burnDurationPercent = 5.0;
            }
            case 3 -> {
                damage = 25;
                fireballCount = 6;
                cooldown = 10;
                burnDurationPercent = 10.0;
            }
            case 4 -> {
                damage = 38;
                fireballCount = 6;
                cooldown = 9;
                burnDurationPercent = 15.0;
            }
            case 5 -> {
                damage = 50;
                fireballCount = 7;
                cooldown = 8;
                burnDurationPercent = 20.0;
            }
            case 6 -> {
                damage = 67;
                fireballCount = 7;
                cooldown = 8;
                burnDurationPercent = 25.0;
            }
            default -> {
                damage = 10;
                fireballCount = 5;
                cooldown = 12;
                burnDurationPercent = 0.0;
            }
        }

        Map<String, Object> properties = new HashMap<>();
        properties.put("fireballCount", fireballCount);
        properties.put("range", 36); // 36 blocks
        properties.put("burnDurationPercent", burnDurationPercent);

        return new SkillLevelInfo(level, damage, cooldown, properties);
    }

    @Override
    public boolean execute(Player player, int level) {
        if (player == null || !player.isOnline()) {
            return false;
        }

        // Check world
        if (player.getWorld() == null) {
            return false;
        }

        SkillLevelInfo levelInfo = getLevelInfo(level);
        if (levelInfo == null) {
            return false;
        }

        Location playerLoc = player.getLocation();
        if (playerLoc == null) {
            return false;
        }
        
        Vector direction = playerLoc.getDirection();
        if (direction == null) {
            direction = new Vector(0, 0, 1); // Default direction
        }
        direction.normalize();

        // Spawn fireballs sau lưng player
        int fireballCount = levelInfo.getPropertyInt("fireballCount", 5);
        double range = levelInfo.getPropertyDouble("range", 36.0);

        // Effect hoành tráng: Tạo vòng tròn lửa sau lưng
        Location behindPlayer = playerLoc.clone().subtract(direction.clone().multiply(1.5));
        
        // Particles effect trước khi spawn fireballs
        for (int i = 0; i < 20; i++) {
            double angle = (2 * Math.PI * i) / 20;
            double radius = 1.0;
            Location particleLoc = behindPlayer.clone().add(
                    Math.cos(angle) * radius,
                    0.5,
                    Math.sin(angle) * radius
            );
            player.getWorld().spawnParticle(Particle.FLAME, particleLoc, 3, 0.1, 0.1, 0.1, 0.05);
            player.getWorld().spawnParticle(Particle.LAVA, particleLoc, 1, 0.1, 0.1, 0.1, 0);
        }
        
        // Spawn fireballs với effect đẹp
        for (int i = 0; i < fireballCount; i++) {
            double angle = (2 * Math.PI * i) / fireballCount;
            double radius = 0.8;
            Location fireballLoc = behindPlayer.clone().add(
                    Math.cos(angle) * radius,
                    0.5 + (i * 0.15),
                    Math.sin(angle) * radius
            );

            // Effect khi spawn
            player.getWorld().spawnParticle(Particle.EXPLOSION, fireballLoc, 1, 0, 0, 0, 0);
            player.getWorld().spawnParticle(Particle.FLAME, fireballLoc, 15, 0.3, 0.3, 0.3, 0.1);
            player.getWorld().spawnParticle(Particle.LARGE_SMOKE, fireballLoc, 5, 0.2, 0.2, 0.2, 0.05);

            // Spawn fireball entity
            try {
                Fireball fireball = player.getWorld().spawn(fireballLoc, Fireball.class);
                if (fireball != null) {
                    fireball.setShooter(player);
                    fireball.setDirection(direction);
                    fireball.setYield(0); // Không nổ
                    fireball.setIsIncendiary(false);

                    // Set velocity
                    Vector velocity = direction.clone().multiply(1.8);
                    fireball.setVelocity(velocity);

                    // Track fireball để damage và remove
                    trackFireball(fireball, player, levelInfo, range);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to spawn fireball: " + e.getMessage());
            }
        }

        // Sound effects hoành tráng
        try {
            player.getWorld().playSound(playerLoc, Sound.ENTITY_BLAZE_SHOOT, 1.5f, 0.8f);
            player.getWorld().playSound(playerLoc, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.2f);
            player.getWorld().playSound(playerLoc, Sound.ENTITY_GHAST_SHOOT, 0.8f, 1.5f);
            player.getWorld().playSound(playerLoc, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.5f, 1.0f);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to play sound effects: " + e.getMessage());
        }
        
        // Effect xung quanh player
        for (int i = 0; i < 10; i++) {
            double angle = (2 * Math.PI * i) / 10;
            Location effectLoc = playerLoc.clone().add(
                    Math.cos(angle) * 1.5,
                    0.5,
                    Math.sin(angle) * 1.5
            );
            player.getWorld().spawnParticle(Particle.FLAME, effectLoc, 5, 0.2, 0.3, 0.2, 0.05);
            player.getWorld().spawnParticle(Particle.SMOKE, effectLoc, 3, 0.1, 0.2, 0.1, 0.02);
        }

        return true;
    }

    /**
     * Track fireball để damage và remove sau khi đi xa
     */
    private void trackFireball(Fireball fireball, Player caster, SkillLevelInfo levelInfo, double maxRange) {
        if (fireball == null || caster == null || levelInfo == null) {
            return;
        }
        final Player casterFinal = caster;
        final SkillLevelInfo levelInfoFinal = levelInfo;
        Location startLoc = fireball.getLocation();
        if (startLoc == null) {
            return;
        }
        final Location startLocFinal = startLoc.clone();

        me.skibidi.rolemmo.manager.ClanCoreManager clanCoreManager = null;
        try {
            if (plugin.getRoleManager() != null) {
                clanCoreManager = plugin.getRoleManager().getClanCoreManager();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to get ClanCoreManager: " + e.getMessage());
        }
        final me.skibidi.rolemmo.manager.ClanCoreManager clanCoreManagerFinal = clanCoreManager;

        new BukkitRunnable() {
            private double distanceTraveled = 0;

            @Override
            public void run() {
                if (!fireball.isValid() || !casterFinal.isOnline() || casterFinal.getWorld() == null) {
                    cancel();
                    return;
                }

                Location currentLoc = fireball.getLocation();
                if (currentLoc == null) {
                    // Fireball location is null, remove và cancel
                    try {
                        if (fireball.isValid()) {
                            fireball.remove();
                        }
                    } catch (Exception e) {
                        // Ignore
                    }
                    cancel();
                    return;
                }
                
                try {
                    distanceTraveled += startLocFinal.distance(currentLoc);
                    startLocFinal.setX(currentLoc.getX());
                    startLocFinal.setY(currentLoc.getY());
                    startLocFinal.setZ(currentLoc.getZ());
                } catch (Exception e) {
                    plugin.getLogger().warning("Error calculating fireball distance: " + e.getMessage());
                    cancel();
                    return;
                }

                // Check range
                if (distanceTraveled >= maxRange) {
                    fireball.remove();
                    cancel();
                    return;
                }

                // Check collision với entities
                try {
                    // Check fireball world trước khi get nearby entities
                    if (fireball.getWorld() == null) {
                        cancel();
                        return;
                    }
                    
                    java.util.Collection<Entity> nearbyEntities = fireball.getNearbyEntities(0.5, 0.5, 0.5);
                    if (nearbyEntities == null) {
                        return; // No nearby entities
                    }
                    
                    for (Entity entity : nearbyEntities) {
                        if (entity == null || !(entity instanceof LivingEntity target) || target.equals(casterFinal)) {
                            continue;
                        }
                        final LivingEntity hitTarget = target;
                        final SkillLevelInfo damageLevelInfo = levelInfoFinal;

                        // Check team protection
                        if (hitTarget instanceof Player) {
                            final Player targetPlayer = (Player) hitTarget;
                            if (clanCoreManagerFinal != null && clanCoreManagerFinal.isEnabled()) {
                                try {
                                    if (clanCoreManagerFinal.areSameTeam(casterFinal, targetPlayer)) {
                                        continue; // Skip teammate
                                    }
                                } catch (Exception e) {
                                    plugin.getLogger().warning("Error checking team: " + e.getMessage());
                                }
                            }
                        }

                        // Damage
                        try {
                            double damage = damageLevelInfo.getDamage();
                            if (damage > 0 && hitTarget.isValid()) {
                                hitTarget.damage(damage, casterFinal);
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Error applying damage: " + e.getMessage());
                        }

                        // Apply burn effect
                        try {
                            double burnPercent = damageLevelInfo.getPropertyDouble("burnDurationPercent", 0.0);
                            if (burnPercent > 0 && hitTarget.isValid()) {
                                int burnTicks = (int) (20 * (burnPercent / 100.0) * 5); // 5 seconds base
                                hitTarget.setFireTicks(Math.max(hitTarget.getFireTicks(), burnTicks));
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Error applying burn effect: " + e.getMessage());
                        }

                        // Particle effects hoành tráng khi hit
                        try {
                            if (hitTarget.getWorld() != null && hitTarget.isValid()) {
                                Location hitLoc = hitTarget.getLocation();
                                if (hitLoc != null) {
                                    hitLoc = hitLoc.add(0, 1, 0);
                                    hitTarget.getWorld().spawnParticle(Particle.EXPLOSION, hitLoc, 2, 0.5, 0.5, 0.5, 0);
                                    hitTarget.getWorld().spawnParticle(Particle.FLAME, hitLoc, 30, 0.5, 0.8, 0.5, 0.15);
                                    hitTarget.getWorld().spawnParticle(Particle.LARGE_SMOKE, hitLoc, 15, 0.4, 0.6, 0.4, 0.1);
                                    hitTarget.getWorld().spawnParticle(Particle.LAVA, hitLoc, 10, 0.3, 0.5, 0.3, 0);
                                    
                                    // Sound effects khi hit
                                    hitTarget.getWorld().playSound(hitLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.2f);
                                    hitTarget.getWorld().playSound(hitLoc, Sound.ENTITY_BLAZE_HURT, 0.8f, 1.0f);
                                    hitTarget.getWorld().playSound(hitLoc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.6f, 1.5f);
                                }
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Error spawning particles/sounds: " + e.getMessage());
                        }

                        try {
                            if (fireball.isValid()) {
                                fireball.remove();
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Error removing fireball: " + e.getMessage());
                        }
                        
                        cancel();
                        return;
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error in fireball collision check: " + e.getMessage());
                }

                // Particle trail đẹp hơn
                try {
                    if (fireball.isValid() && fireball.getWorld() != null) {
                        Location trailLoc = fireball.getLocation();
                        if (trailLoc != null) {
                            fireball.getWorld().spawnParticle(Particle.FLAME, trailLoc, 5, 0.15, 0.15, 0.15, 0.02);
                            fireball.getWorld().spawnParticle(Particle.SMOKE, trailLoc, 2, 0.1, 0.1, 0.1, 0.01);
                            fireball.getWorld().spawnParticle(Particle.LAVA, trailLoc, 1, 0.05, 0.05, 0.05, 0);
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error spawning trail particles: " + e.getMessage());
                }
            }
        }.runTaskTimer(plugin, 0L, 1L); // Check mỗi tick
    }
}

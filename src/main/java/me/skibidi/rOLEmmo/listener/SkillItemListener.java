package me.skibidi.rolemmo.listener;

import me.skibidi.rolemmo.ROLEmmo;
import me.skibidi.rolemmo.util.SkillItemUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener để xử lý skill items
 * Ngăn drop, move, và tự động thêm lại khi cần
 */
public class SkillItemListener implements Listener {

    private final ROLEmmo plugin;

    public SkillItemListener(ROLEmmo plugin) {
        this.plugin = plugin;
    }

    /**
     * Ngăn drop skill items
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (SkillItemUtil.isSkillItem(item)) {
            event.setCancelled(true);
            me.skibidi.rolemmo.util.MessageUtil.sendActionBar(event.getPlayer(), "§cKhông thể vứt skill item!");
        }
    }

    /**
     * Ngăn move skill items vào chest hoặc inventory khác
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        
        if (!player.isOnline()) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        // Check clicked item
        if (clicked != null && SkillItemUtil.isSkillItem(clicked)) {
            // Cho phép click trong player inventory, nhưng không cho move ra ngoài
            if (event.getInventory().getType() != InventoryType.PLAYER) {
                event.setCancelled(true);
                me.skibidi.rolemmo.util.MessageUtil.sendActionBar(player, "§cKhông thể di chuyển skill item ra khỏi túi đồ!");
                return;
            }
        }

        // Check cursor item
        if (cursor != null && SkillItemUtil.isSkillItem(cursor)) {
            // Cho phép trong player inventory, nhưng không cho move ra ngoài
            if (event.getInventory().getType() != InventoryType.PLAYER) {
                event.setCancelled(true);
                me.skibidi.rolemmo.util.MessageUtil.sendActionBar(player, "§cKhông thể di chuyển skill item ra khỏi túi đồ!");
            }
        }
    }

    /**
     * Ngăn drag skill items
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        
        if (!player.isOnline()) {
            return;
        }

        ItemStack dragged = event.getOldCursor();
        if (dragged != null && SkillItemUtil.isSkillItem(dragged)) {
            // Cho phép drag trong player inventory, nhưng không cho drag ra ngoài
            if (event.getInventory() != null && event.getInventory().getType() != InventoryType.PLAYER) {
                event.setCancelled(true);
                me.skibidi.rolemmo.util.MessageUtil.sendActionBar(player, "§cKhông thể di chuyển skill item ra khỏi túi đồ!");
            }
        }
    }

    /**
     * Ngăn skill items rơi ra khi chết
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event == null) return;
        
        Player player = event.getEntity();
        if (player == null || !player.isOnline()) return;
        
        // Remove skill items khỏi drops
        try {
            event.getDrops().removeIf(item -> item != null && SkillItemUtil.isSkillItem(item));
        } catch (Exception e) {
            plugin.getLogger().warning("Error removing skill items from drops: " + e.getMessage());
        }
        
        // Lưu skill items để thêm lại khi respawn
        // (Có thể lưu vào metadata hoặc tự động thêm lại khi respawn)
    }

    /**
     * Thêm lại skill items khi respawn
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (event == null) return;
        
        Player player = event.getPlayer();
        if (player == null || !player.isOnline()) return;
        
        // Thêm lại skill items sau 1 tick (đảm bảo inventory đã được restore)
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                giveSkillItems(player);
            }
        }, 1L);
    }

    /**
     * Right-click skill item để sử dụng skill.
     * Kiểm tra cả item từ event và cả hai tay (main/off) để tránh mất event trên một số bản.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event == null) return;

        Player player = event.getPlayer();
        if (player == null || !player.isOnline()) return;

        // Chỉ xử lý right-click (air hoặc block)
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Kiểm tra item: ưu tiên item từ event, sau đó main hand, rồi off hand
        ItemStack item = event.getItem();
        org.bukkit.inventory.EquipmentSlot hand = event.getHand();
        if (item == null || item.getType() == Material.AIR) {
            item = player.getInventory().getItemInMainHand();
            hand = org.bukkit.inventory.EquipmentSlot.HAND;
        }
        if (item == null || item.getType() == Material.AIR) {
            item = player.getInventory().getItemInOffHand();
            hand = org.bukkit.inventory.EquipmentSlot.OFF_HAND;
        }
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        if (hand == null) {
            hand = org.bukkit.inventory.EquipmentSlot.HAND;
        }

        String skillId = SkillItemUtil.getSkillId(item);
        if (skillId == null || skillId.isEmpty()) {
            skillId = SkillItemUtil.getSkillIdFromLore(item, plugin.getSkillManager(), plugin.getRoleManager().getPlayerRole(player));
            // Nếu có skill từ lore nhưng không có PDC → gắn lại PDC vào item (migrate)
            if (skillId != null && !skillId.isEmpty()) {
                SkillItemUtil.fixSkillItemPdc(player, item, skillId, hand);
            }
        }
        if (skillId == null || skillId.isEmpty()) {
            if (SkillItemUtil.looksLikeSkillItem(item)) {
                me.skibidi.rolemmo.util.MessageUtil.sendActionBar(player, "§cGiữ skill item trong tay, chuột phải để dùng. Không được thì mở §e/role §7→ Skill để nhận lại.");
            }
            return;
        }

        event.setCancelled(true);

        try {
            var skillManager = plugin.getSkillManager();
            if (skillManager != null) {
                skillManager.executeSkill(player, skillId);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error executing skill " + skillId + " for player " + player.getName() + ": " + e.getMessage());
            me.skibidi.rolemmo.util.MessageUtil.sendActionBar(player, "§cLỗi khi sử dụng skill! Vui lòng thử lại sau.");
        }
    }

    /**
     * Đảm bảo chỉ có 1 skill item mỗi loại
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryChange(org.bukkit.event.inventory.InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        // Check duplicate skill items
        java.util.Map<String, Integer> skillItemCounts = new java.util.HashMap<>();
        
        ItemStack[] contents = player.getInventory().getContents();
        for (ItemStack item : contents) {
            if (item != null && SkillItemUtil.isSkillItem(item)) {
                String skillId = SkillItemUtil.getSkillId(item);
                if (skillId != null) {
                    skillItemCounts.put(skillId, skillItemCounts.getOrDefault(skillId, 0) + 1);
                }
            }
        }

        // Remove duplicates (giữ lại 1)
        for (var entry : skillItemCounts.entrySet()) {
            if (entry.getValue() > 1) {
                String skillId = entry.getKey();
                int found = 0;
                for (int i = 0; i < player.getInventory().getSize(); i++) {
                    ItemStack item = player.getInventory().getItem(i);
                    if (item != null && SkillItemUtil.isSkillItem(item) && skillId.equals(SkillItemUtil.getSkillId(item))) {
                        found++;
                        if (found > 1) {
                            player.getInventory().setItem(i, null);
                        }
                    }
                }
            }
        }
    }

    /**
     * Give skill items cho player dựa trên role hiện tại
     */
    private void giveSkillItems(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        
        try {
            var roleManager = plugin.getRoleManager();
            var skillManager = plugin.getSkillManager();
            
            if (roleManager == null || skillManager == null) {
                return;
            }

            var currentRole = roleManager.getPlayerRole(player);
            if (currentRole == null) {
                return;
            }

            java.util.List<me.skibidi.rolemmo.model.Skill> skills = skillManager.getSkills(currentRole);
            if (skills == null) {
                return;
            }
            
            for (me.skibidi.rolemmo.model.Skill skill : skills) {
                if (skill == null) continue;
                
                try {
                    int level = skillManager.getPlayerSkillLevel(player, skill.getId());
                    if (level > 0) {
                        SkillItemUtil.ensureSkillItem(player, skill, level);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error giving skill item " + skill.getId() + " to player " + player.getName() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error in giveSkillItems for player " + (player != null ? player.getName() : "null") + ": " + e.getMessage());
        }
    }
}

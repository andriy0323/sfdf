package me.regionblocks.commands;

import me.regionblocks.RegionBlocks;
import me.regionblocks.models.Region;
import me.regionblocks.models.RegionFlags;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RgCommand implements CommandExecutor, TabCompleter {

    private final RegionBlocks plugin;

    public RgCommand(RegionBlocks plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только для игроков.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("i") || args[0].equalsIgnoreCase("info")) {
            showInfo(player);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "flag"  -> handleFlag(player, args);
            case "flags" -> handleFlags(player, args);
            default -> player.sendMessage(usage());
        }
        return true;
    }

    private Component usage() {
        return Component.text("Использование:").color(TextColor.color(0xAAAAAA))
                .appendNewline()
                .append(Component.text("  /rg i                       — ваши регионы").color(TextColor.color(0xFFFF55)))
                .appendNewline()
                .append(Component.text("  /rg flags <регион>          — посмотреть флаги").color(TextColor.color(0xFFFF55)))
                .appendNewline()
                .append(Component.text("  /rg flag <регион> <флаг> <true|false>  — задать флаг (админ)").color(TextColor.color(0xFFFF55)))
                .appendNewline()
                .append(Component.text("  /rg flag <регион> <флаг> reset         — сбросить в дефолт (админ)").color(TextColor.color(0xFFFF55)));
    }

    private void handleFlag(Player player, String[] args) {
        if (!player.hasPermission("regionblocks.admin")) {
            player.sendMessage(Component.text("✗ Менять флаги регионов могут только администраторы.")
                    .color(TextColor.color(0xFF4444)));
            return;
        }
        if (args.length < 4) {
            player.sendMessage(Component.text("/rg flag <регион> <флаг> <true|false|reset>")
                    .color(TextColor.color(0xAAAAAA)));
            return;
        }
        Region r = plugin.getRegionManager().getRegion(args[1]);
        if (r == null) {
            player.sendMessage(Component.text("✗ Регион «" + args[1] + "» не найден.")
                    .color(TextColor.color(0xFF4444)));
            return;
        }
        String flag = args[2].toLowerCase(Locale.ROOT);
        if (!RegionFlags.isKnown(flag)) {
            player.sendMessage(Component.text("✗ Неизвестный флаг. Доступны: " + String.join(", ", RegionFlags.ALL))
                    .color(TextColor.color(0xFF4444)));
            return;
        }
        String value = args[3].toLowerCase(Locale.ROOT);
        if (value.equals("reset") || value.equals("default") || value.equals("none")) {
            r.removeFlag(flag);
            plugin.getRegionManager().save();
            player.sendMessage(Component.text("✓ Флаг ").color(TextColor.color(0x55FF55))
                    .append(Component.text(flag).color(TextColor.color(0xFFFFFF)))
                    .append(Component.text(" сброшен в дефолт (")
                            .color(TextColor.color(0x888888)))
                    .append(Component.text(String.valueOf(RegionFlags.DEFAULTS.get(flag)))
                            .color(TextColor.color(0xFFCC55)))
                    .append(Component.text(") для региона ").color(TextColor.color(0x888888)))
                    .append(Component.text(r.getName()).color(TextColor.color(0xFFCC55))));
            return;
        }
        Boolean b = parseBool(value);
        if (b == null) {
            player.sendMessage(Component.text("✗ Значение должно быть true / false / reset.")
                    .color(TextColor.color(0xFF4444)));
            return;
        }
        r.setFlag(flag, b);
        plugin.getRegionManager().save();
        player.sendMessage(Component.text("✓ Флаг ").color(TextColor.color(0x55FF55))
                .append(Component.text(flag).color(TextColor.color(0xFFFFFF)))
                .append(Component.text(" = ").color(TextColor.color(0x888888)))
                .append(Component.text(String.valueOf(b)).color(TextColor.color(b ? 0x55FF55 : 0xFF4444)))
                .append(Component.text(" в регионе ").color(TextColor.color(0x888888)))
                .append(Component.text(r.getName()).color(TextColor.color(0xFFCC55))));
    }

    private void handleFlags(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Component.text("/rg flags <регион>")
                    .color(TextColor.color(0xAAAAAA)));
            return;
        }
        Region r = plugin.getRegionManager().getRegion(args[1]);
        if (r == null) {
            player.sendMessage(Component.text("✗ Регион «" + args[1] + "» не найден.")
                    .color(TextColor.color(0xFF4444)));
            return;
        }
        player.sendMessage(Component.text("Флаги региона ").color(TextColor.color(0xAAAAAA))
                .append(Component.text(r.getName()).color(TextColor.color(0xFFCC55))
                        .decoration(TextDecoration.BOLD, true)));
        for (String f : RegionFlags.ALL) {
            boolean actual = RegionFlags.valueFor(r, f);
            Boolean explicit = r.getFlag(f);
            Component value = Component.text(String.valueOf(actual))
                    .color(TextColor.color(actual ? 0x55FF55 : 0xFF4444));
            Component suffix = explicit == null
                    ? Component.text(" (по умолчанию)").color(TextColor.color(0x555555))
                    : Component.text(" (задан админом)").color(TextColor.color(0x888888));
            player.sendMessage(Component.text("  • ").color(TextColor.color(0x888888))
                    .append(Component.text(f).color(TextColor.color(0xFFFFFF)))
                    .append(Component.text(": ").color(TextColor.color(0x888888)))
                    .append(value)
                    .append(suffix));
        }
    }

    private Boolean parseBool(String s) {
        return switch (s) {
            case "true", "yes", "on", "allow", "1" -> Boolean.TRUE;
            case "false", "no", "off", "deny", "0" -> Boolean.FALSE;
            default -> null;
        };
    }

    private void showInfo(Player player) {
        List<Region> regions = plugin.getRegionManager().getPlayerRegions(player.getName());

        player.sendMessage(Component.text(""));
        player.sendMessage(
            Component.text("══════ Ваши регионы ══════")
                .color(TextColor.color(0xFFAA00)).decoration(TextDecoration.BOLD, true)
        );

        if (regions.isEmpty()) {
            player.sendMessage(Component.text("  У вас нет регионов.").color(TextColor.color(0x888888)));
        } else {
            for (Region r : regions) {
                int s = r.getTier().getSize();

                player.sendMessage(Component.text("  ✦ ").color(TextColor.color(0xFFAA00))
                    .append(Component.text(r.getName()).color(TextColor.color(0xFFFFFF)).decoration(TextDecoration.BOLD, true)));
                player.sendMessage(Component.text("     Тир: ").color(TextColor.color(0x888888))
                    .append(Component.text(r.getTier().getDisplayName()).color(TextColor.color(r.getTier().getColor()))));
                player.sendMessage(Component.text("     Размер: ").color(TextColor.color(0x888888))
                    .append(Component.text(s + "×" + s + "×" + s + " блоков").color(TextColor.color(0xFFCC55))));
                player.sendMessage(Component.text("     От: ").color(TextColor.color(0x888888))
                    .append(Component.text(
                        r.minX() + ", " + r.minY() + ", " + r.minZ()
                    ).color(TextColor.color(0xAAFFAA)))
                    .append(Component.text("  До: ").color(TextColor.color(0x888888)))
                    .append(Component.text(
                        r.maxX() + ", " + r.maxY() + ", " + r.maxZ()
                    ).color(TextColor.color(0xAAFFAA))));
                player.sendMessage(Component.text("     Блок: ").color(TextColor.color(0x888888))
                    .append(Component.text(
                        r.getCenter().getBlockX() + ", " + r.getCenter().getBlockY() + ", " + r.getCenter().getBlockZ()
                    ).color(TextColor.color(0xCCCCCC))));
                if (!r.getMembers().isEmpty()) {
                    player.sendMessage(Component.text("     Участники: ").color(TextColor.color(0x888888))
                        .append(Component.text(String.join(", ", r.getMembers())).color(TextColor.color(0xCCCCCC))));
                }
                player.sendMessage(Component.text(""));
            }
        }
        player.sendMessage(
            Component.text("══════════════════════════").color(TextColor.color(0xFFAA00)).decoration(TextDecoration.BOLD, true)
        );
        player.sendMessage(Component.text("Подсказка: /rg flags <регион>  чтобы посмотреть флаги.")
                .color(TextColor.color(0x555555)));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("i", "info", "flag", "flags");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("flag") || args[0].equalsIgnoreCase("flags"))) {
            List<String> names = new ArrayList<>();
            for (Region r : plugin.getRegionManager().getAllRegions()) names.add(r.getName());
            return names;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("flag")) {
            return new ArrayList<>(RegionFlags.ALL);
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("flag")) {
            return List.of("true", "false", "reset");
        }
        return List.of();
    }
}

package me.regionblocks.models;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Каталог флагов региона + их значения по умолчанию.
 *
 * Флаги хранятся в самой {@link Region} как {@code Map<String, Boolean>}.
 * Если у региона флаг не задан явно — используется значение из {@link #DEFAULTS}.
 *
 * Менять флаги может только администратор сервера (perm {@code regionblocks.admin}),
 * см. {@code RgCommand#handleFlag}.
 */
public final class RegionFlags {

    private RegionFlags() {}

    public static final String PVP         = "pvp";
    public static final String MOB_SPAWN   = "mob-spawn";
    public static final String MOB_DAMAGE  = "mob-damage";
    public static final String EXPLOSIONS  = "explosions";
    public static final String FIRE_SPREAD = "fire-spread";

    /**
     * Значения по умолчанию. Все защитные флаги запрещают по умолчанию:
     *   pvp        = false  — PvP внутри региона запрещён
     *   mob-spawn  = false  — враждебные мобы не спавнятся
     *   mob-damage = false  — мобы не наносят урон игрокам
     *   explosions = false  — взрывы не разрушают и не наносят урон
     *   fire-spread= false  — огонь не распространяется
     */
    public static final Map<String, Boolean> DEFAULTS;
    static {
        Map<String, Boolean> m = new LinkedHashMap<>();
        m.put(PVP,         false);
        m.put(MOB_SPAWN,   false);
        m.put(MOB_DAMAGE,  false);
        m.put(EXPLOSIONS,  false);
        m.put(FIRE_SPREAD, false);
        DEFAULTS = java.util.Collections.unmodifiableMap(m);
    }

    public static final List<String> ALL = List.copyOf(DEFAULTS.keySet());

    /** Действующее значение флага для региона (с учётом DEFAULTS). */
    public static boolean valueFor(Region r, String flag) {
        if (r == null) return true; // вне региона — без ограничений
        Boolean v = r.getFlag(flag);
        if (v != null) return v;
        return DEFAULTS.getOrDefault(flag, false);
    }

    public static boolean isKnown(String flag) {
        return flag != null && DEFAULTS.containsKey(flag.toLowerCase());
    }
}

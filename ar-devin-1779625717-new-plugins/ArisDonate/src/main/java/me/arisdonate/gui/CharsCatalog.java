package me.arisdonate.gui;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Каталог чаров для команды /chars.
 *
 * Каждый элемент — отдельный чар (один key), у которого известен максимальный
 * уровень. Кастомные чары (например telekinesis "Телепатия") помечаются
 * {@link Entry#custom}=true и хранятся отдельно (не в minecraft:enchantment).
 */
public final class CharsCatalog {

    public static final String TELEKINESIS_KEY = "telekinesis";

    public record Entry(
            String key,         // minecraft:* ключ или "telekinesis"
            String ruName,      // отображаемое имя
            String description, // короткое описание для lore
            Material icon,
            int maxLevel,
            boolean custom
    ) {}

    private static final Map<String, Entry> BY_KEY = new LinkedHashMap<>();
    private static final List<Entry> ALL = new ArrayList<>();

    static {
        add(new Entry(TELEKINESIS_KEY, "Телепатия",
                "Дроп с блоков попадает сразу в инвентарь", Material.ENDER_EYE, 1, true));

        // Tool
        add(vanilla("efficiency",         "Бур",                "Скорость добычи",                  Material.DIAMOND_PICKAXE, 5));
        add(vanilla("fortune",            "Удача",              "+ к выпадению руды",               Material.DIAMOND,         3));
        add(vanilla("silk_touch",         "Шёлковое касание",   "Блоки выпадают сами собой",        Material.STRING,          1));
        add(vanilla("unbreaking",         "Прочность",          "Меньше расхода прочности",         Material.ANVIL,           3));
        add(vanilla("mending",            "Починка",            "Опыт чинит предмет",               Material.EXPERIENCE_BOTTLE, 1));

        // Sword / Axe
        add(vanilla("sharpness",          "Острота",            "+ урон",                            Material.DIAMOND_SWORD,   5));
        add(vanilla("smite",              "Кара нежити",        "+ урон по нежити",                  Material.ROTTEN_FLESH,    5));
        add(vanilla("bane_of_arthropods", "Кара членистоногих", "+ урон по паукам/пчёлам",           Material.SPIDER_EYE,      5));
        add(vanilla("knockback",          "Отбрасывание",       "Отталкивание врагов",               Material.SLIME_BALL,      2));
        add(vanilla("fire_aspect",        "Заговор огня",       "Поджигает атакованных",             Material.BLAZE_POWDER,    2));
        add(vanilla("looting",            "Добыча",             "+ дроп с мобов",                    Material.GHAST_TEAR,      3));
        add(vanilla("sweeping_edge",      "Обметание",          "Урон по нескольким врагам",         Material.IRON_SWORD,      3));

        // Bow
        add(vanilla("power",              "Сила",               "+ урон стрел",                      Material.BOW,             5));
        add(vanilla("punch",              "Толчок",             "Отбрасывание стрелами",             Material.ARROW,           2));
        add(vanilla("flame",              "Горение",            "Поджигание стрелами",               Material.FIRE_CHARGE,     1));
        add(vanilla("infinity",           "Бесконечность",      "Бесконечные стрелы",                Material.TIPPED_ARROW,    1));

        // Crossbow
        add(vanilla("quick_charge",       "Быстрая зарядка",    "Быстрее заряжать арбалет",          Material.CROSSBOW,        3));
        add(vanilla("multishot",          "Мультивыстрел",      "3 стрелы за выстрел",               Material.SPECTRAL_ARROW,  1));
        add(vanilla("piercing",           "Пронзание",          "Стрелы пробивают цели",             Material.BREEZE_ROD,      4));

        // Armor
        add(vanilla("protection",            "Защита",                "Универсальная защита",       Material.DIAMOND_CHESTPLATE, 4));
        add(vanilla("projectile_protection", "Защита от снарядов",    "Меньше урона от стрел",      Material.SHIELD,             4));
        add(vanilla("fire_protection",       "Огнестойкость",         "Меньше урона от огня",       Material.BLAZE_ROD,          4));
        add(vanilla("blast_protection",      "Взрывоустойчивость",    "Меньше урона от взрывов",    Material.TNT,                4));
        add(vanilla("feather_falling",       "Лёгкое падение",        "Меньше урона от падения",    Material.FEATHER,            4));
        add(vanilla("respiration",           "Подводное дыхание",     "Дольше дышишь под водой",    Material.GLASS_BOTTLE,       3));
        add(vanilla("aqua_affinity",         "Сродство с водой",      "Быстрее копать под водой",   Material.PRISMARINE_CRYSTALS,1));
        add(vanilla("thorns",                "Шипы",                  "Урон в ответ при ударе",     Material.CACTUS,             3));
        add(vanilla("depth_strider",         "Подводный ходок",       "Быстрее в воде",             Material.HEART_OF_THE_SEA,   3));
        add(vanilla("frost_walker",          "Ледоход",               "Замораживает воду под ногами", Material.PACKED_ICE,        2));
        add(vanilla("soul_speed",            "Скорость душ",          "Быстрее по soul-блокам",     Material.SOUL_SAND,          3));
        add(vanilla("swift_sneak",           "Стремительная подкрадка","Быстрее в подкрадке",       Material.SCULK_SHRIEKER,     3));

        // Fishing rod
        add(vanilla("luck_of_the_sea",       "Удача моря",            "Лучше улов",                  Material.FISHING_ROD,        3));
        add(vanilla("lure",                  "Приманка",              "Быстрее клёв",                Material.NAUTILUS_SHELL,     3));

        // Trident
        add(vanilla("channeling",            "Громоотвод",            "Молния при попадании",        Material.LIGHTNING_ROD,      1));
        add(vanilla("impaling",              "Гарпун",                "+ урон по водным существам",  Material.TRIDENT,            5));
        add(vanilla("loyalty",               "Верность",              "Трезубец возвращается",       Material.HEART_OF_THE_SEA,   3));
        add(vanilla("riptide",               "Тяга",                  "Полёт с трезубцем в дождь",   Material.TURTLE_SCUTE,       3));
    }

    private static Entry vanilla(String key, String ruName, String desc, Material icon, int maxLevel) {
        return new Entry(key, ruName, desc, icon, maxLevel, false);
    }

    private static void add(Entry e) {
        BY_KEY.put(e.key.toLowerCase(Locale.ROOT), e);
        ALL.add(e);
    }

    public static List<Entry> all() {
        return ALL;
    }

    public static Entry get(String key) {
        if (key == null) return null;
        return BY_KEY.get(key.toLowerCase(Locale.ROOT));
    }

    public static Enchantment vanillaEnchant(Entry e) {
        if (e == null || e.custom) return null;
        return Registry.ENCHANTMENT.get(NamespacedKey.minecraft(e.key));
    }
}

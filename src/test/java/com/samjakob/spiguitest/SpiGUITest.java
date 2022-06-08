package com.samjakob.spiguitest;

import com.samjakob.spigui.MessageUtil;
import com.samjakob.spigui.SGMenu;
import com.samjakob.spigui.SpiGUI;
import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

public class SpiGUITest extends JavaPlugin {

    private static SpiGUI spiGUI;

    @Override
    public void onEnable() {
        spiGUI = new SpiGUI(this);
    }

    @Override
    public void onDisable() {

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getLabel().equalsIgnoreCase("spigui")) {

            if (!(sender instanceof Player player)) {
                sender.sendMessage("You must be a player to run this command.");
                return true;
            }

            if (args.length == 0) {
                // Open a test SpiGUI menu.
                SGMenu myAwesomeMenu = SpiGUITest.getSpiGUI().create("<red><b>SpiGUI <red>(Page {currentPage}/{maxPage})", 3);

                myAwesomeMenu.setButton(0, 10, new SGButton(
                        new ItemBuilder(Material.PLAYER_HEAD)
                                .skullOwner(player.getName())
                                .name("<yellow><b>" + MessageUtil.toString(player.displayName()))
                                .lore(
                                        "<yellow>Game Mode: <gold>" + player.getGameMode(),
                                        "<yellow>Location: <gold>" + String.format(
                                                "%.0f, %.0f, %.0f",
                                                player.getLocation().getX(),
                                                player.getLocation().getY(),
                                                player.getLocation().getZ()
                                        ),
                                        "<yellow>Experience: <gold>" + player.getTotalExperience()
                                )
                                .build()
                ));

                myAwesomeMenu.setButton(1, 0, new SGButton(
                        new ItemBuilder(Material.GOLD_ORE)
                                .name("<gold>Get rich quick!")
                                .build()
                ).withListener(event -> {
                    Inventory playerInventory = event.getWhoClicked().getInventory();

                    IntStream.range(0, 9).forEach(hotBarSlot -> playerInventory.setItem(
                            hotBarSlot, new ItemBuilder(
                                    event.getCurrentItem().getType() == Material.GOLD_ORE
                                            ? Material.GOLD_BLOCK
                                            : event.getCurrentItem().getType()
                            ).amount(64).build()
                    ));

                    event.getWhoClicked().sendMessage(
                            MessageUtil.color(
                                    event.getCurrentItem().getType() == Material.GOLD_ORE
                                            ? "<yellow><b>You are now rich!"
                                            : "<yellow><b>You are now poor."
                            )
                    );

                    Material newMaterial = event.getCurrentItem().getType() == Material.GOLD_ORE
                            ? Material.DIRT
                            : Material.GOLD_ORE;

                    myAwesomeMenu.getButton(1, 0).setIcon(
                            new ItemBuilder(newMaterial).name(
                                    newMaterial == Material.GOLD_ORE ? "<gold>Get rich quick!" : "<gray>Get poor quick!"
                            ).amount(1).build()
                    );

                    myAwesomeMenu.refreshInventory(event.getWhoClicked());
                    ((Player) event.getWhoClicked()).updateInventory();
                }));

                AtomicReference<BukkitTask> borderRunnable = new AtomicReference<>();

                myAwesomeMenu.setOnPageChange(inventory -> {
                    if (inventory.getCurrentPage() != 0) {
                        if (borderRunnable.get() != null) borderRunnable.get().cancel();
                    } else borderRunnable.set(
                            inventory.getCurrentPage() != 0
                                    ? null
                                    : new BukkitRunnable() {

                                private final int[] TILES_TO_UPDATE = {
                                        0, 1, 2, 3, 4, 5, 6, 7, 8,
                                        9, 17,
                                        18, 19, 20, 21, 22, 23, 24, 25, 26
                                };

                                private short currentColor = 1;

                                @Override
                                public void run() {

                                    IntStream.range(0, TILES_TO_UPDATE.length).map(i -> TILES_TO_UPDATE.length - i - 1).forEach(
                                            index -> myAwesomeMenu.setButton(TILES_TO_UPDATE[index], nextColorButton())
                                    );

                                    currentColor++;
                                    if (currentColor >= 15) currentColor = 0;

                                    myAwesomeMenu.refreshInventory(player);

                                }

                                private SGButton nextColorButton() {
                                    return new SGButton(
                                            new ItemBuilder(Material.BLACK_STAINED_GLASS)
                                                    .name("" + Integer.toHexString(currentColor) + "<b>SpiGUI!!!")
                                                    .data(currentColor)
                                                    .build()
                                    );
                                }

                            }.runTaskTimer(this, 0L, 20L)
                    );
                });

                myAwesomeMenu.setOnClose(inventory -> {
                    if (borderRunnable.get() != null) borderRunnable.get().cancel();
                });

                myAwesomeMenu.getOnPageChange().accept(myAwesomeMenu);
                player.openInventory(myAwesomeMenu.getInventory());

                return true;
            }

            if (args.length == 2) {
                if ("inventorySizeTest".equals(args[0])) {
                    int size;
                    try {
                        size = Integer.parseInt(args[1]);
                    } catch (NumberFormatException ex) {
                        player.sendMessage(MessageUtil.color("<red><b><i>ERROR  <red>The inventory size must be a valid integer."));
                        return true;
                    }

                    SGMenu inventorySizeTest = SpiGUITest.getSpiGUI().create("Test Menu", 1);

                    IntStream.range(0, size).forEach(i -> inventorySizeTest.addButton(new SGButton(
                            new ItemBuilder(Material.GOLD_ORE)
                                    .build()
                    )));

                    player.openInventory(inventorySizeTest.getInventory());

                    return true;
                }
            }

            player.sendMessage("Unrecognized command.");
        }

        return false;
    }

    public static SpiGUI getSpiGUI() {
        return spiGUI;
    }
}

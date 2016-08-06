package com.github.civcraft.donum.gui;

import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.github.civcraft.donum.Donum;
import com.github.civcraft.donum.inventories.DeliveryInventory;

import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventory;
import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

public class DeliveryGUI {

	private UUID viewer;
	private int currentPage;
	private DeliveryInventory inventory;

	public DeliveryGUI(UUID viewer, DeliveryInventory inventory) {
		this.inventory = inventory;
		this.viewer = viewer;
		this.currentPage = 0;
	}

	public void showScreen() {
		Player p = Bukkit.getPlayer(viewer);
		if (p == null) {
			return;
		}
		ClickableInventory.forceCloseInventory(p);
		ClickableInventory ci = new ClickableInventory(54, "Delivery inventory");
		List<ItemStack> stacks = inventory.getInventory().getItemStackRepresentation();
		if (stacks.size() < 45 * currentPage) {
			// would show an empty page, so go to previous
			currentPage--;
			showScreen();
		}
		for (int i = 45 * currentPage; i < 45 * (currentPage + 1) && i < stacks.size(); i++) {
			ci.setSlot(createRemoveItemClickable(stacks.get(i)), i - (45 * currentPage));
		}
		// previous button
		if (currentPage > 0) {
			ItemStack back = new ItemStack(Material.ARROW);
			ISUtils.setName(back, ChatColor.GOLD + "Go to previous page");
			Clickable baCl = new Clickable(back) {

				@Override
				public void clicked(Player arg0) {
					if (currentPage > 0) {
						currentPage--;
					}
					showScreen();
				}
			};
			ci.setSlot(baCl, 45);
		}
		// next button
		if ((45 * (currentPage + 1)) <= stacks.size()) {
			ItemStack forward = new ItemStack(Material.ARROW);
			ISUtils.setName(forward, ChatColor.GOLD + "Go to next page");
			Clickable forCl = new Clickable(forward) {

				@Override
				public void clicked(Player arg0) {
					if ((45 * (currentPage + 1)) <= stacks.size()) {
						currentPage++;
					}
					showScreen();
				}
			};
			ci.setSlot(forCl, 53);
		}
		// exit button
		ItemStack backToOverview = new ItemStack(Material.WOOD_DOOR);
		ISUtils.setName(backToOverview, ChatColor.GOLD + "Close");
		ci.setSlot(new Clickable(backToOverview) {

			@Override
			public void clicked(Player arg0) {
				// just let it close, dont do anything
			}
		}, 49);

		// complain button
		ItemStack gibStuffBack = new ItemStack(Material.SIGN);
		ISUtils.setName(gibStuffBack, ChatColor.GOLD + "Request item return");
		ISUtils.addLore(gibStuffBack, ChatColor.AQUA + "If you think you lost items due to a glitch", ChatColor.AQUA
				+ "you can send us a message", ChatColor.AQUA + "to get your items back", ChatColor.GREEN
				+ "Click here to do so");
		Clickable openComplaintForm = new Clickable(gibStuffBack) {

			@Override
			public void clicked(Player p) {
				TextComponent link = new TextComponent(
						"Click this message to write the admins a message about lost items. This will open a new tab in your default browser!");
				link.setColor(ChatColor.GREEN);
				link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Donum.getConfiguration()
						.getComplaintURL()));
				link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Open link")
						.create()));
				p.spigot().sendMessage(link);
			}
		};
		ci.setSlot(openComplaintForm, 47);
		
		ci.showInventory(p);
	}

	private Clickable createRemoveItemClickable(final ItemStack is) {
		return new Clickable(is) {

			@Override
			public void clicked(Player p) {
				PlayerInventory pInv = p.getInventory();
				if (new ItemMap(is).fitsIn(pInv)) {
					Donum.getInstance().debug(p.getName() + " took " + is.toString() + " from delivery inventory");
					inventory.getInventory().removeItemStack(is);
					pInv.addItem(is);
					inventory.setDirty(true);
				} else {
					p.sendMessage(ChatColor.RED + "There is not enough space in your inventory");
				}
				p.updateInventory();
				showScreen();
			}
		};
	}

}

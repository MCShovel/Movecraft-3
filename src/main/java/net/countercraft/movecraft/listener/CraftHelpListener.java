package net.countercraft.movecraft.listener;

import java.util.*;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.craft.CraftType;
import net.countercraft.movecraft.utils.BlockNames;
import net.minecraft.server.v1_12_R1.Item;
import net.minecraft.server.v1_12_R1.ItemStack;

public class CraftHelpListener implements CommandExecutor {

	private CraftType getCraftByName(String string) {
		CraftType[] crafts = CraftManager.getInstance().getCraftTypes();
		if (crafts == null || string == null || string.length() == 0) {
			return null;
		}

		List<CraftType> found = new ArrayList<CraftType>();
		String prefix = string.toLowerCase();
		for (CraftType tcraft : crafts) {
			if (tcraft.getCraftName().toLowerCase().startsWith(prefix)) {
				found.add(tcraft);
			}
		}

		if (found.size() == 1) {
			return found.get(0);
		}

		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0 || args[0].length() == 0) {
			args = new String[] { "list" };
		}

		if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("movecraftadmin.reload")) {
			CraftManager.getInstance().initCraftTypes();
			sender.sendMessage(ChatColor.GOLD + "Craft configuration reloaded.");
			return true;
		}

		boolean doList = args[0].equalsIgnoreCase("list");
		CraftType c = null;
		if (!doList) {
			c = getCraftByName(args[0]);
			if (c == null) {
				sender.sendMessage(ChatColor.RED + "Unable to locate a craft by that name.");
			}
		}
		if (doList || c == null) {
			doCraftList(sender);
			return true;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("&6==========[ &f" + c.getCraftName() + "&6 ]==========\n");
		sb.append("\n&6Size: &7" + c.getMinSize() + " to " + c.getMaxSize());
		sb.append("\n&6Speed: &7" + Math.round(20.0 / c.getTickCooldown()) + " to " + (Math.round(20.0 / c.getCruiseTickCooldown()) * c.getCruiseSkipBlocks()));
		sb.append("\n&6Altitude: &7" + c.getMinHeightLimit() + " to " + c.getMaxHeightLimit());
		if (c.getFuelBurnRate() > 0) {
			sb.append("\n&6Fuel Use: &7" + c.getFuelBurnRate());
		}
		if (c.getRequireWaterContact()) {
			sb.append("\n&6Requires Water: &7YES");
		}
		StringBuilder req = new StringBuilder(), limit = new StringBuilder();
		appendFlyBlocks(req, limit, c.getFlyBlocks());
		if (req.length() > 0) {
			sb.append("\n&6Requirements: &7" + req.toString());
		}
		if (limit.length() > 0) {
			sb.append("\n&6Constraints: &7" + limit.toString());
		}
		
		sb.append("\n&6Allowed Blocks: &7");
		Integer[] blockIds = c.getAllowedBlocks();
		Set<String> blockList = new HashSet<String>();
		for (int ix = 0; ix < blockIds.length; ix++) {
			BlockNames.itemNames(blockIds[ix], blockList);
		}
		String[] names = blockList.toArray(new String[blockList.size()]);
		Arrays.sort(names);
		sb.append(String.join(", ", names));
		
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', sb.toString()));
		return true;
	}

	private void appendFlyBlocks(StringBuilder sbReq, StringBuilder sbLimit,
			HashMap<ArrayList<Integer>, ArrayList<Double>> flyBlocks) {
		for (ArrayList<Integer> i : flyBlocks.keySet()) {
			String blockName = BlockNames.itemName(i.get(0));
			Double minPercentage = flyBlocks.get(i).get(0);
			Double maxPercentage = flyBlocks.get(i).get(1);

			if (minPercentage > 0.01) {
				if (sbReq.length() > 0)
					sbReq.append(", ");
				if (minPercentage < 10000.0) {
					sbReq.append(String.format("%.2f%% %s", minPercentage, blockName));
				} else {
					minPercentage -= 10000.0;
					sbReq.append(String.format("%d %s", minPercentage.intValue(), blockName));
				}
			}
			if (maxPercentage.intValue() < 100 || (maxPercentage.intValue() >= 10000 && maxPercentage.intValue() < 10100)) {
				if (sbLimit.length() > 0)
					sbLimit.append(", ");
				if (maxPercentage < 10000.0) {
					sbLimit.append(String.format("%s <= %.2f%%", blockName, maxPercentage));
				} else {
					maxPercentage -= 10000.0;
					sbLimit.append(String.format("%s <= %d", blockName, maxPercentage.intValue()));
				}
			}
		}
	}

	private void doCraftList(CommandSender sender) {
		CraftType[] crafts = CraftManager.getInstance().getCraftTypes();
		if (crafts == null)
			crafts = new CraftType[0];
		String[] names = new String[crafts.length];
		for (int ix = 0; ix < crafts.length; ix++) {
			names[ix] = crafts[ix].getCraftName();
		}

		sender.sendMessage(ChatColor.GOLD + "Craft Types: " + ChatColor.GRAY + String.join(", ", names));
	}

}

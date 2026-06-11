package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class PlayerConsumesScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player consumes <item>
    //
    // @Group Player
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a player consumes (eats/drinks) an item (like food or potions).
    //
    // @Context
    // <context.item> returns the ItemTag.
    // <context.hand> returns an ElementTag of the hand being used to consume the item. Can be either HAND or OFF_HAND. Requires a 1.19+ server.
    // <context.replacement> returns the ItemTag that will replace the consumed item.
    //
    // @Determine
    // item:ItemTag to change the item being consumed. Use with caution, if the player is eating a stack of items, this will replace the entire stack.
    // replacement:ItemTag to change the remaining item after original item being consumed.
    //
    // @Player Always.
    //
    // -->

    public PlayerConsumesScriptEvent() {
        registerCouldMatcher("player consumes <item>");

        this.<PlayerConsumesScriptEvent, ItemTag>registerOptionalDetermination("item", ItemTag.class, (evt, context, value) -> {
            if (value != null) {
                evt.item = value;
                evt.event.setItem(value.getItemStack());
                return true;
            }
            return false;
        });
        this.<PlayerConsumesScriptEvent, ItemTag>registerOptionalDetermination("replacement", ItemTag.class, (evt, context, value) -> {
            if (value != null) {
                evt.replacement = value;
                evt.event.setReplacement(value.getItemStack());
                return true;
            }
            return false;
        });

    }

    public ItemTag item;
    public ItemTag replacement;
    public PlayerItemConsumeEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!path.tryArgObject(2, item)) {
            return false;
        }
        if (!runInCheck(path, event.getPlayer().getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "item" -> item;
            case "hand" -> new ElementTag(event.getHand());
            case "replacement" -> replacement;
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onPlayerConsumes(PlayerItemConsumeEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        item = new ItemTag(event.getItem());
        replacement = new ItemTag(event.getReplacement());
        this.event = event;
        fire(event);
    }
}

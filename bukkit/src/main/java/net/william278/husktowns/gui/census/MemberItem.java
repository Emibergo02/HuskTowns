package net.william278.husktowns.gui.census;

import net.william278.husktowns.BukkitHuskTowns;
import net.william278.husktowns.gui.GuiSettings;
import net.william278.husktowns.town.Member;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

import java.util.List;

public class MemberItem extends AbstractItem {
    private final Member member;

    public MemberItem(Member member) {
        this.member = member;
    }

    @Override
    public ItemProvider getItemProvider() {
        return GuiSettings.getInstance().getCensusGuiSettings().getItem("memberItem")
                .toItemProvider(
                        "%member_name%", member.user().getUsername(),
                        "%member_role%", member.role().getName()
                );
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        BukkitHuskTowns.getInstance().getOnlineUsers().stream()
                .filter(onlineUser -> onlineUser.getUuid().equals(member.user().getUuid()))
                .findFirst()
                .ifPresent(onlineUser ->
                        new MemberGui(onlineUser, this).open(player));
    }

    public Member getMember() {
        return member;
    }

}
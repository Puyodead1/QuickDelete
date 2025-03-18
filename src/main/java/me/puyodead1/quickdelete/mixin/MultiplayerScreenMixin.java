package me.puyodead1.quickdelete.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = MultiplayerScreen.class, priority = 1)
public abstract class MultiplayerScreenMixin extends Screen {

    @Shadow
    protected MultiplayerServerListWidget serverListWidget;

    protected MultiplayerScreenMixin(Text title) {
        super(title);
    }

    @Shadow
    protected abstract void removeEntry(boolean confirmAction);

    @Redirect(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/widget/ButtonWidget;builder(Lnet/minecraft/text/Text;Lnet/minecraft/client/gui/widget/ButtonWidget$PressAction;)Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;"
            )
    )
    private ButtonWidget.Builder modifyDeleteButtonBuilder(Text text, ButtonWidget.PressAction originalAction) {
        if (text.getContent() instanceof TranslatableTextContent translatableTextContent) {
            if(translatableTextContent.getKey().equals("selectServer.delete")) {
                return ButtonWidget.builder(text, (button) -> {
                    MultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
                    if (entry instanceof MultiplayerServerListWidget.ServerEntry serverEntry) {
                        String serverName = serverEntry.getServer().name;
                        if (serverName != null) {
                            this.removeEntry(true);
                        }
                    }
                });
            }
        }

        return ButtonWidget.builder(text, originalAction);
    }
}
package me.puyodead1.quickdelete.mixin;

import net.minecraft.client.gui.screen.multiplayer.AddServerScreen;
import net.minecraft.client.gui.screen.multiplayer.DirectConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.network.LanServerQueryManager;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public abstract class MultiplayerScreenMixin {

    @Shadow
    public ButtonWidget buttonDelete;

    @Shadow
    private ButtonWidget buttonEdit;

    @Shadow
    private ButtonWidget buttonJoin;

    @Shadow
    private boolean initialized;

    @Shadow
    private ServerList serverList;

    @Shadow
    private LanServerQueryManager.LanServerEntryList lanServers;

    @Shadow
    private LanServerQueryManager.LanServerDetector lanServerDetector;

    @Shadow
    public MultiplayerServerListWidget serverListWidget;

    @Final
    @Shadow
    private static Logger LOGGER;

    @Shadow
    private ServerInfo selectedEntry;

    @Shadow
    protected abstract void removeEntry(boolean confirmedAction);

    @Shadow
    protected abstract void directConnect(boolean confirmedAction);

    @Shadow
    protected abstract void addEntry(boolean confirmedAction);

    @Shadow
    protected abstract void editEntry(boolean confirmedAction);

    @Shadow
    protected abstract void refresh();

    @Shadow
    protected abstract void updateButtonActivationStates();


    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/MultiplayerScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;", ordinal = 6))
    private void mlp$quickDelete(CallbackInfo ci) {
        MultiplayerScreen screen = (MultiplayerScreen) (Object) this;

        if (this.initialized) {
            this.serverListWidget.setDimensionsAndPosition(screen.width, screen.height - 64 - 32, 0, 32);
        } else {
            this.initialized = true;
            this.serverList = new ServerList(screen.client);
            this.serverList.loadFile();
            this.lanServers = new LanServerQueryManager.LanServerEntryList();

            try {
                this.lanServerDetector = new LanServerQueryManager.LanServerDetector(this.lanServers);
                this.lanServerDetector.start();
            } catch (Exception var8) {
                LOGGER.warn("Unable to start LAN server detection: {}", var8.getMessage());
            }

            this.serverListWidget = new MultiplayerServerListWidget(screen, screen.client, screen.width, screen.height - 64 - 32, 32, 36);
            this.serverListWidget.setServers(this.serverList);
        }

        screen.addDrawableChild(this.serverListWidget);
        this.buttonJoin = screen.addDrawableChild(ButtonWidget.builder(Text.translatable("selectServer.select"), button -> screen.connect()).width(100).build());
        ButtonWidget buttonWidget = screen.addDrawableChild(ButtonWidget.builder(Text.translatable("selectServer.direct"), button -> {
            this.selectedEntry = new ServerInfo(I18n.translate("selectServer.defaultName"), "", ServerInfo.ServerType.OTHER);
            screen.client.setScreen(new DirectConnectScreen(screen, this::directConnect, this.selectedEntry));
        }).width(100).build());
        ButtonWidget buttonWidget2 = screen.addDrawableChild(ButtonWidget.builder(Text.translatable("selectServer.add"), button -> {
            this.selectedEntry = new ServerInfo(I18n.translate("selectServer.defaultName"), "", ServerInfo.ServerType.OTHER);
            screen.client.setScreen(new AddServerScreen(screen, this::addEntry, this.selectedEntry));
        }).width(100).build());
        this.buttonEdit = screen.addDrawableChild(ButtonWidget.builder(Text.translatable("selectServer.edit"), button -> {
            MultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
            if (entry instanceof MultiplayerServerListWidget.ServerEntry) {
                ServerInfo serverInfo = ((MultiplayerServerListWidget.ServerEntry)entry).getServer();
                this.selectedEntry = new ServerInfo(serverInfo.name, serverInfo.address, ServerInfo.ServerType.OTHER);
                this.selectedEntry.copyWithSettingsFrom(serverInfo);
                screen.client.setScreen(new AddServerScreen(screen, this::editEntry, this.selectedEntry));
            }
        }).width(74).build());
        this.buttonDelete = screen.addDrawableChild(ButtonWidget.builder(Text.translatable("selectServer.delete"), button -> {
            MultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
            if (entry instanceof MultiplayerServerListWidget.ServerEntry) {
                String string = ((MultiplayerServerListWidget.ServerEntry)entry).getServer().name;
                if (string != null) {
                    this.removeEntry(true);
                }
            }
        }).width(74).build());
        ButtonWidget buttonWidget3 = screen.addDrawableChild(
                ButtonWidget.builder(Text.translatable("selectServer.refresh"), button -> this.refresh()).width(74).build()
        );
        ButtonWidget buttonWidget4 = screen.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, button -> screen.close()).width(74).build());
        DirectionalLayoutWidget directionalLayoutWidget = DirectionalLayoutWidget.vertical();
        AxisGridWidget axisGridWidget = directionalLayoutWidget.add(new AxisGridWidget(308, 20, AxisGridWidget.DisplayAxis.HORIZONTAL));
        axisGridWidget.add(this.buttonJoin);
        axisGridWidget.add(buttonWidget);
        axisGridWidget.add(buttonWidget2);
        directionalLayoutWidget.add(EmptyWidget.ofHeight(4));
        AxisGridWidget axisGridWidget2 = directionalLayoutWidget.add(new AxisGridWidget(308, 20, AxisGridWidget.DisplayAxis.HORIZONTAL));
        axisGridWidget2.add(this.buttonEdit);
        axisGridWidget2.add(this.buttonDelete);
        axisGridWidget2.add(buttonWidget3);
        axisGridWidget2.add(buttonWidget4);
        directionalLayoutWidget.refreshPositions();
        SimplePositioningWidget.setPos(directionalLayoutWidget, 0, screen.height - 64, screen.width, 64);
        this.updateButtonActivationStates();
    }
}
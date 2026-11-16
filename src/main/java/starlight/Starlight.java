package starlight;
import starlight.LeftSideUtils.StencilManager;
import starlight.ui.GeneratorUI;
import starlight.ui.LeftSideUi;
import starlight.ui.UpperUi;
import starlight.upperuiutils.LandCopyPast;
import starlight.utils.MapSizeUnlocker;
import starlight.utils.UpdateChecker;
import arc.Events;
import arc.Core;
import mindustry.game.EventType;
import mindustry.mod.Mod;
public class Starlight extends Mod {
   public Starlight() {
      Events.on(EventType.ClientLoadEvent.class, e -> {
         MapSizeUnlocker.init();
         GeneratorUI.init();
         LeftSideUi.init();
         StencilManager.init();
         LandCopyPast.init();
         UpperUi.init();
         if (Core.settings.getBool("starlight_check_updates", true)) UpdateChecker.check();
      });
   }
   public void loadContent() {
   }
}

package starlight;
import starlight.LeftSideUtils.StencilManager;
import starlight.ui.GeneratorUI;
import starlight.ui.LeftSideUi;
import starlight.ui.UpperUi;
import starlight.upperuiutils.LandCopyPast;
import starlight.utils.MapSizeUnlocker;
import arc.Events;
import mindustry.game.EventType;
import mindustry.mod.Mod;
public class Starlight extends Mod {
   public Starlight() {
      Events.on(EventType.ClientLoadEvent.class, (e) -> {
         MapSizeUnlocker.init();
         GeneratorUI.init();
         LeftSideUi.init();
         StencilManager.init();
         LandCopyPast.init();
         UpperUi.init();
      });
   }
   public void loadContent() {
   }
}

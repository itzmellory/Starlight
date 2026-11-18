package starlight;
import arc.Events;
import arc.util.OS;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.mod.Mod;
import starlight.LeftSideUtils.StencilManager;
import starlight.mapping.MappingUtilities;
import mfe.MapFilterExt;
import starlight.testing.content.TUSettings;
import starlight.testing.ui.TUStyles;
import starlight.testing.util.Setup;
import starlight.testing.util.TUIcons;
import starlight.ui.GeneratorUI;
import starlight.ui.LeftSideUi;
import starlight.ui.UpperUi;
import starlight.upperuiutils.LandCopyPast;
import starlight.utils.MapSizeUnlocker;
public class Starlight extends Mod{
    public Starlight(){
        Events.on(EventType.ClientLoadEvent.class, event -> {
            MappingUtilities.init();
            MapSizeUnlocker.init();
            GeneratorUI.init();
            LeftSideUi.init();
            StencilManager.init();
            LandCopyPast.init();
            UpperUi.init();
            Vars.renderer.minZoom = 0.667f;
            Vars.renderer.maxZoom = 24f;
            TUIcons.init();
            TUStyles.init();
            TUSettings.init();
            Setup.init();
            MapFilterExt.initializeMFE();
        });
    }
    public void loadContent(){
    }
    public static boolean disableTestingInCampaign(){
        return Vars.state.isCampaign() && !OS.username.equals("MEEP");
    }
}

package starlight.testing.util;
import arc.scene.style.TextureRegionDrawable;
import mindustry.gen.Icon;
import mindustry.content.UnitTypes;
public class TUIcons{
    public static TextureRegionDrawable clone, seppuku, core, dump, survival, sandbox, heal, invincibility, alpha;
    public static void init(){
        clone = new TextureRegionDrawable(Icon.add);
        seppuku = new TextureRegionDrawable(Icon.cancel);
        core = new TextureRegionDrawable(Icon.defense);
        dump = new TextureRegionDrawable(Icon.up);
        survival = new TextureRegionDrawable(Icon.refresh);
        sandbox = new TextureRegionDrawable(Icon.settings);
        heal = new TextureRegionDrawable(Icon.add);
        invincibility = new TextureRegionDrawable(Icon.defense);
        alpha = new TextureRegionDrawable(UnitTypes.alpha.uiIcon);
    }
    public static TextureRegionDrawable get(TextureRegionDrawable icon){
        return new TextureRegionDrawable(icon);
    }
}

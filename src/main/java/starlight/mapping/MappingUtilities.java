package starlight.mapping;
import arc.Core;
import arc.Events;
import arc.scene.Element;
import arc.scene.event.EventListener;
import arc.scene.event.VisibilityListener;
import arc.struct.Seq;
import arc.util.Reflect;
import arc.util.DelayedRemovalSeq;
import mindustry.Vars;
import mindustry.editor.MapEditorDialog;
import mindustry.ui.dialogs.CustomRulesDialog;
import mindustry.ui.dialogs.MapInfoDialog;
import mindustry.ui.dialogs.MapPlayDialog;
import mindustry.game.EventType;
import mindustry.mod.Mod;
import mindustry.ui.dialogs.SettingsMenuDialog;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.game.Rules;
import mindustry.game.Team;
import mindustry.content.Planets;
import mindustry.world.meta.Env;
import arc.scene.ui.CheckBox;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.scene.ui.Image;
import arc.graphics.Color;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
public class MappingUtilities{
    private static final Seq<RulesDialogMod> ruleMods = new Seq<>();
    private static boolean initialized;
    public static void init(){
        if(initialized || Vars.ui == null || Vars.ui.editor == null) return;
        initialized = true;
        addSettings();
        try{
            CustomRulesDialog info = Reflect.get(MapInfoDialog.class, Reflect.get(Vars.ui.editor, "infoDialog"), "ruleInfo");
            CustomRulesDialog play = Reflect.get(MapPlayDialog.class, Reflect.get(Vars.ui.custom, "dialog"), "dialog");
            CustomRulesDialog playtest = Reflect.get(MapPlayDialog.class, Reflect.get(Vars.ui.editor, "playtestDialog"), "dialog");
            CustomRulesDialog create = Reflect.get(MapInfoDialog.class, Reflect.get(MapEditorDialog.class, Vars.ui.editor, "infoDialog"), "ruleInfo");
            attach(info);
            attach(play);
            attach(playtest);
            attach(create);
        }catch(Throwable error){
            arc.util.Log.err("Failed to initialize Mapping Utilities rules integration.", error);
        }
    }
    private static void attach(CustomRulesDialog dialog){
        if(dialog == null) return;
        RulesDialogMod mod = new RulesDialogMod(dialog);
        ruleMods.add(mod);
        mod.enable();
    }
    private static void addSettings(){
        Vars.ui.settings.addCategory("@settings.editor", Icon.editor, table -> {
            table.pref(new SettingsTitle("@settings.rules_dialog"));
            table.sliderPref("editor_content_buttons_size", 50, 30, 80, value -> value + "px");
            table.checkPref("editor_hidden_rules", true);
            table.checkPref("editor_revealed_blocks", true);
            table.checkPref("editor_planet_background", true);
            table.checkPref("editor_environment_settings", true);
            table.checkPref("editor_better_content_dialogs", true);
        });
    }
    private static class SettingsTitle extends SettingsMenuDialog.SettingsTable.Setting{
        SettingsTitle(String title){
            super("");
            this.title = title;
        }
        @Override
        public void add(SettingsMenuDialog.SettingsTable table){
            table.add(title).color(Pal.accent).padTop(20f).left().pad(5f).row();
            table.image().color(Pal.accent).height(3f).fillX().padBottom(5f).row();
        }
    }
}

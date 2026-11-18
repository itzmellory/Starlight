package mfe.guides;
import arc.Core;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.Log;
import mfe.MapFilterExt;
import mindustry.Vars;
import mindustry.gen.Tex;
import mindustry.io.JsonIO;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
public class GuideSchematics {
   public static StringMap schematics;
   public static BaseDialog schematicsDialog = new BaseDialog("@schmatic");
   public static void save() {
      MapFilterExt.setting.putJson("guideSchematics", schematics);
   }
   public static void load() {
      schematics = (StringMap)MapFilterExt.setting.getJson("guideSchematics", StringMap.class, StringMap::new);
   }
   public static void rebuild() {
      schematicsDialog.cont.clear();
      schematicsDialog.cont.button('\ue81b' + Core.bundle.get("guide.schematic.new"), Styles.flatBordert, () -> {
         if (GuideSeqImage.guidesImage.guides.isEmpty()) {
            Vars.ui.showInfoFade("@guide.schematic.emptyguide", 10.0F);
         } else {
            Vars.ui.showTextInput("@schematic.add", "@name", "", (text) -> {
               boolean replacement = schematics.containsKey(text);
               if (replacement) {
                  Vars.ui.showConfirm("@confirm", "@schematic.replace", () -> {
                     schematics.put(text, JsonIO.write(GuideSeqImage.guidesImage.guides));
                     Vars.ui.showInfoFade("@schematic.saved");
                  });
               } else {
                  schematics.put(text, JsonIO.write(GuideSeqImage.guidesImage.guides));
                  Vars.ui.showInfoFade("@schematic.saved");
               }
               rebuild();
               save();
            });
         }
      }).size(500.0F, 100.0F);
      schematicsDialog.cont.row();
      schematicsDialog.cont.pane((t) -> {
         int[] co = new int[1];
         schematics.each((name, sche) -> {
            t.table((b) -> {
               b.setBackground(Tex.pane);
               b.add(name).height(MapFilterExt.buttonSize).left();
               b.row();
               b.table((tt) -> {
                  tt.button("\ue874", Styles.flatBordert, () -> Core.app.setClipboardText(sche)).size(MapFilterExt.buttonSize);
                  tt.button("\ue852", Styles.flatBordert, () -> {
                     try {
                        GuideSeqImage.guidesImage.clearGuides();
                        GuideSeqImage.guidesImage.guides.set((Seq)JsonIO.read(Seq.class, sche));
                        GuideSeqImage.guidesImage.cfgPop.setNeedsRebuild();
                        schematicsDialog.hide();
                     } catch (Throwable e) {
                        Vars.ui.showException(e);
                        Log.err(e);
                     }
                  }).size(MapFilterExt.buttonSize);
                  tt.button("\ue852", Styles.flatBordert, () -> {
                     try {
                        GuideSeqImage.guidesImage.guides.add((Seq)JsonIO.read(Seq.class, sche));
                        GuideSeqImage.guidesImage.cfgPop.setNeedsRebuild();
                        schematicsDialog.hide();
                     } catch (Throwable e) {
                        Vars.ui.showException(e);
                        Log.err(e);
                     }
                  }).size(MapFilterExt.buttonSize).with((tb) -> tb.add("+").color(Color.green).style(Styles.outlineLabel).name("memory").width(0.1F).with((l) -> {
                        l.setAlignment(20);
                        l.setFontScale(0.75F);
                     }).fillY());
                  tt.button("\ue815", Styles.flatBordert, () -> Vars.ui.showConfirm(Core.bundle.get("schematic.delete.confirm") + ": " + name, () -> {
                        schematics.remove(name);
                        save();
                        rebuild();
                     })).size(MapFilterExt.buttonSize).with((tb) -> tb.getLabel().setColor(Color.scarlet));
               }).left();
               b.row();
               b.labelWrap(sche).grow().maxHeight(150.0F).with((l) -> l.setAlignment(10)).color(Color.gray);
            }).size(400.0F, 250.0F).pad(4.0F);
            if (Mathf.mod(++co[0], Math.max(1, Core.graphics.getWidth() / 500)) == 0) {
               t.row();
            }
         });
      }).grow();
   }
   static {
      schematicsDialog.addCloseButton();
      schematicsDialog.shown(GuideSchematics::rebuild);
   }
}

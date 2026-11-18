package mfe;
import arc.Core;
import arc.Events;
import arc.func.Cons4;
import arc.func.Floatc;
import arc.func.Floatp;
import arc.func.Prov;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.scene.event.ElementGestureListener;
import arc.scene.event.InputEvent;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Label;
import arc.scene.ui.TextButton;
import arc.scene.ui.TextField;
import arc.scene.ui.TextField.TextFieldFilter;
import arc.util.Log;
import arc.util.Strings;
import java.util.Arrays;
import mfe.editor.MFEWaveInfoDialog;
import mfe.filters.AdvancedNoiseFilter;
import mfe.filters.AdvancedOreFilter;
import mfe.filters.AppliedRegionFilter;
import mfe.filters.CopyPasteFilter;
import mfe.filters.GridFilter;
import mfe.filters.PolarTransFilter;
import mfe.filters.RotateTransFilter;
import mfe.filters.ScalingTransFilter;
import mfe.filters.TranslateTransFilter;
import mfe.guides.BaseGuide;
import mfe.guides.CurveGuide;
import mfe.guides.ExpressionGuide;
import mfe.guides.GuideSchematics;
import mfe.guides.GuideSeqImage;
import mfe.guides.VanillaGridGuide;
import mindustry.Vars;
import mindustry.editor.MapEditor;
import mindustry.editor.MapInfoDialog;
import mindustry.game.EventType;
import mindustry.io.JsonIO;
import mindustry.maps.Maps;
import mindustry.maps.filters.GenerateFilter;
import mindustry.mod.Mod;
import mindustry.mod.Mods;
import mindustry.ui.Styles;
import mt.io.SettingHandler;
import mt.utils.RefUtils;
public class MapFilterExt extends Mod {
   public static float buttonSize = 32.0F;
   public static float step = 1.0F;
   public static SettingHandler setting;
   public static TextButton.TextButtonStyle titleTogglet;
   public static TextureRegionDrawable uiStep025;
   public static TextureRegionDrawable uiStep05;
   public static TextureRegionDrawable uiStrokeCenter;
   public static TextureRegionDrawable uiStrokeAdd;
   public static TextureRegionDrawable uiCoordsysRect;
   public static TextureRegionDrawable uiCoordsysPolar;
   public static TextField.TextFieldFilter filter = (field, c) -> TextFieldFilter.floatsOnly.acceptChar(field, c) || c == '-' && field.getCursorPosition() == 0 && !field.getText().contains("-");
   public static Cons4<Floatc, Floatp, Label, TextField> addDragableFloatInput = (setter, getter, l, f) -> {
      f.setText(String.valueOf(getter.get()));
      f.setFilter(filter);
      f.changed(() -> {
         if (f.isValid()) {
            setter.get(Strings.parseFloat(f.getText()));
         }
      });
      l.addListener(new ElementGestureListener() {
         public void pan(InputEvent event, float x, float y, float deltaX, float deltaY) {
            super.pan(event, x, y, deltaX, deltaY);
            GuideSeqImage.guidesImage.cfgPop.cancelScroll();
            setter.get((float)Mathf.floor(getter.get() + deltaX / 2.0F * MapFilterExt.step));
            f.setText(Strings.fixed(getter.get(), 0));
         }
         public void tap(InputEvent event, float x, float y, int count, KeyCode button) {
            super.tap(event, x, y, count, button);
            if (count >= 2) {
               setter.get(0.0F);
               f.setText(String.valueOf(getter.get()));
            }
         }
      });
   };
   public static void init() {
      initStyles();
      SettingHandler.registerJsonClass(BaseGuide.class, VanillaGridGuide.class, ExpressionGuide.class, CurveGuide.class);
      setting = new SettingHandler("MFE");
      setting.checkPref("cacheGraphsToMapTags", false);
      addFilters();
      GuideSchematics.load();
      Vars.ui.editor.shown(() -> GuideSeqImage.guidesImage.cfgPop.inject());
      if (Vars.editor.getClass() != MapEditor.class) Vars.ui.showInfo(Core.bundle.format("warning.novanillaeditor", Vars.editor.getClass().getName()));
      MapInfoDialog dialog = (MapInfoDialog)RefUtils.getValue(Vars.ui.editor, "infoDialog");
      dialog.shown(() -> {
         dialog.cont.row();
         dialog.cont.button("@mfe.waveinfo.button", () -> {
            dialog.hide();
            MFEWaveInfoDialog.mfewave.show();
         }).size(200.0F, 50.0F);
      });
   }

   public static void addFilters() {
      addFilter(TranslateTransFilter::new);
      addFilter(ScalingTransFilter::new);
      addFilter(RotateTransFilter::new);
      addFilter(PolarTransFilter::new);
      addFilter(AppliedRegionFilter::new);
      addFilter(AdvancedNoiseFilter::new);
      addFilter(AdvancedOreFilter::new);
      addFilter(GridFilter::new);
      addFilter(CopyPasteFilter::new);
   }
   public static void addFilter(Prov<GenerateFilter> filter) {
      Prov<GenerateFilter>[] newArr = (Prov[])Arrays.copyOf(Maps.allFilterTypes, Maps.allFilterTypes.length + 1);
      newArr[Maps.allFilterTypes.length] = filter;
      Maps.allFilterTypes = newArr;
      Log.debug("Adding New Filters... Filters Size: " + newArr.length);
      GenerateFilter ins = (GenerateFilter)filter.get();
      SettingHandler.registerJsonClass(ins.getClass());
      JsonIO.json.addClassTag(Strings.camelize(ins.getClass().getSimpleName().replace("Filter", "")), ins.getClass());
   }
   public static void initStyles() {
      titleTogglet = new TextButton.TextButtonStyle(Styles.squareTogglet);
      titleTogglet.up = Styles.black;
      uiStep025 = new TextureRegionDrawable(Core.atlas.find("mapfilterext-ui-step-025"));
      uiStep05 = new TextureRegionDrawable(Core.atlas.find("mapfilterext-ui-step-05"));
      uiStrokeCenter = new TextureRegionDrawable(Core.atlas.find("mapfilterext-ui-stroke-center"));
      uiStrokeAdd = new TextureRegionDrawable(Core.atlas.find("mapfilterext-ui-stroke-add"));
      uiCoordsysRect = new TextureRegionDrawable(Core.atlas.find("mapfilterext-ui-coordsys-rect"));
      uiCoordsysPolar = new TextureRegionDrawable(Core.atlas.find("mapfilterext-ui-coordsys-polar"));
   }
                }

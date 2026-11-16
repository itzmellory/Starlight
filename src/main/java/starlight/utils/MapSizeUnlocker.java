package starlight.utils;
import arc.Events;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.ui.Label;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import java.lang.reflect.Field;
import mindustry.Vars;
import mindustry.editor.MapEditorDialog;
import mindustry.editor.MapResizeDialog;
import mindustry.game.EventType.Trigger;
import mindustry.ui.Styles;
public class MapSizeUnlocker {
   public static void init() {
      unlockStaticBounds();
      Events.run(Trigger.update, MapSizeUnlocker::unlockDialogs);
   }
   private static void unlockStaticBounds() {
      for(Class<?> cls : new Class[]{MapEditorDialog.class, MapResizeDialog.class}) {
         for(String fName : new String[]{"minSize", "maxSize"}) {
            try {
               Field f = cls.getDeclaredField(fName);
               setFinalStaticInt(f, fName.equals("minSize") ? 1 : Integer.MAX_VALUE);
            } catch (Throwable var9) {
            }
         }
      }
   }
   private static void unlockDialogs() {
      if (Vars.ui != null && Vars.ui.editor != null) {
         try {
            Field rF = MapEditorDialog.class.getDeclaredField("resizeDialog");
            rF.setAccessible(true);
            MapResizeDialog rd = (MapResizeDialog)rF.get(Vars.ui.editor);
            if (rd != null) {
               for(String name : new String[]{"minSize", "maxSize"}) {
                  try {
                     Field f = MapResizeDialog.class.getDeclaredField(name);
                     f.setAccessible(true);
                     f.setInt(rd, name.equals("minSize") ? 1 : Integer.MAX_VALUE);
                  } catch (Throwable var7) {
                  }
               }
               if (rd.isShown()) {
                  patchTextFields(rd.cont);
               }
            }
         } catch (Throwable var8) {
         }
      }
   }
   private static void patchTextFields(Group root) {
      if (root != null) {
         for(Element child : root.getChildren()) {
            if (child instanceof TextField) {
               TextField tf = (TextField)child;
               tf.setMaxLength(0);
               if (!"ar-unlocked-field".equals(tf.name)) {
                  tf.name = "ar-unlocked-field";
                  Group parent = tf.parent;
                  if (parent instanceof Table) {
                     Table pt = (Table)parent;
                     Cell<?> cell = pt.getCell(tf);
                     if (cell != null) {
                        Table wrap = new Table();
                        wrap.defaults().left();
                        wrap.add(tf).width(Math.max(tf.getPrefWidth(), 175.0F)).row();
                        Label hint = new Label("Max: [accent]2,147,483,647[]", Styles.outlineLabel);
                        hint.setFontScale(0.75F);
                        wrap.add(hint).padTop(2.0F).left();
                        cell.setElement(wrap);
                        pt.invalidateHierarchy();
                     }
                  }
               }
            } else if (child instanceof Group && !"ar-unlocked-field".equals(child.name)) {
               patchTextFields((Group)child);
            }
         }
      }
   }
   private static void setFinalStaticInt(Field field, int newValue) {
      try {
         field.setAccessible(true);
         try {
            Field modField = Field.class.getDeclaredField("modifiers");
            modField.setAccessible(true);
            modField.setInt(field, field.getModifiers() & -17);
         } catch (Throwable var4) {
         }
         try {
            Field accField = Field.class.getDeclaredField("accessFlags");
            accField.setAccessible(true);
            accField.setInt(field, field.getModifiers() & -17);
         } catch (Throwable var3) {
         }
         field.setInt((Object)null, newValue);
      } catch (Throwable var5) {
      }
   }
}

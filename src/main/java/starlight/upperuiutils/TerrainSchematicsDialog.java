package starlight.upperuiutils;
import arc.Core;
import arc.files.Fi;
import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
public class TerrainSchematicsDialog {
   public static Fi dir() {
      Fi d = Core.settings.getDataDirectory().child("starlight-terrain-schematics");
      d.mkdirs();
      return d;
   }
   public static void show() {
      BaseDialog d = new BaseDialog(Core.bundle.get("upper.schematics", "Terrain Schematics"));
      Table list = new Table();
      list.margin(6.0F);
      String[] saveName = new String[]{"terrain_" + Time.millis() / 1000L};
      Runnable[] rebuild = new Runnable[1];
      rebuild[0] = () -> {
         list.clearChildren();
         list.defaults().pad(4.0F).left();
         Fi[] files = dir().list(".tmsch");
         if (files.length == 0) {
            list.add(Core.bundle.get("upper.noschematics", "No saved terrain schematics.")).color(Color.lightGray).pad(12.0F).row();
         }
         for(Fi f : files) {
            Table row = new Table(Tex.pane);
            row.margin(8.0F);
            row.add(f.nameWithoutExtension()).growX().left().padRight(10.0F);
            row.button(Icon.download, Styles.flati, () -> {
               LandCopyPast.Clip loaded = loadClip(f);
               if (loaded != null) {
                  if (LandCopyPast.clipboard != null) {
                     LandCopyPast.clipboard.dispose();
                  }
                  LandCopyPast.clipboard = loaded;
                  LandCopyPast.setMode(LandCopyPast.Mode.paste);
                  d.hide();
               }
            }).size(44.0F).tooltip("Load into Paste");
            row.button(Icon.trash, Styles.flati, () -> {
               f.delete();
               rebuild[0].run();
            }).size(44.0F).padLeft(4.0F).tooltip("Delete");
            list.add(row).growX().width(380.0F).row();
         }
      };
      Table top = new Table();
      top.defaults().pad(6.0F);
      top.add(Core.bundle.get("upper.schemname", "Name:"));
      top.field(saveName[0], (s) -> saveName[0] = s).width(200.0F);
      top.button(Core.bundle.get("upper.schemsave", "Save Clipboard"), Icon.save, Styles.flatt, () -> {
         if (LandCopyPast.clipboard == null) {
            Vars.ui.showErrorMessage("Copy terrain first before saving!");
         } else {
            String clean = saveName[0].trim().replaceAll("[^a-zA-Z0-9_\\-]", "_");
            if (clean.isEmpty()) {
               clean = "terrain";
            }
            saveClip(LandCopyPast.clipboard, dir().child(clean + ".tmsch"));
            rebuild[0].run();
         }
      }).height(40.0F).disabled((b) -> LandCopyPast.clipboard == null);
      d.cont.add(top).row();
      rebuild[0].run();
      d.cont.pane(list).size(Math.min((float)Core.graphics.getWidth() * 0.9F, 520.0F), Math.min((float)Core.graphics.getHeight() * 0.65F, 420.0F)).padTop(8.0F);
      d.addCloseButton();
      d.show();
   }
   public static void saveClip(LandCopyPast.Clip c, Fi file) {
      try {
         DataOutputStream out = new DataOutputStream(file.write(false, 8192));
         try {
            out.writeInt(c.width);
            out.writeInt(c.height);
            int len = c.width * c.height;
            for(int i = 0; i < len; ++i) {
               out.writeUTF(c.floorIds[i] >= 0 && Vars.content.block(c.floorIds[i]) != null ? Vars.content.block(c.floorIds[i]).name : "");
               out.writeUTF(c.overlayIds[i] >= 0 && Vars.content.block(c.overlayIds[i]) != null ? Vars.content.block(c.overlayIds[i]).name : "");
               out.writeUTF(c.blockIds[i] > 0 && Vars.content.block(c.blockIds[i]) != null ? Vars.content.block(c.blockIds[i]).name : "");
               out.writeBoolean(c.isCenter[i]);
               out.writeByte(c.teamIds[i]);
               out.writeByte(c.rotations[i]);
               out.writeByte(c.datas[i]);
               out.writeByte(c.floorDatas[i]);
               out.writeByte(c.overlayDatas[i]);
               out.writeInt(c.extraDatas[i]);
            }
         } catch (Throwable var6) {
            try {
               out.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
            throw var6;
         }
         out.close();
      } catch (Exception e) {
         Vars.ui.showException(e);
      }
   }
   public static LandCopyPast.Clip loadClip(Fi file) {
      try {
         DataInputStream in = new DataInputStream(file.read(8192));
         LandCopyPast.Clip var13;
         try {
            int w = in.readInt();
            int h = in.readInt();
            LandCopyPast.Clip c = new LandCopyPast.Clip(w, h);
            int len = w * h;
            for(int i = 0; i < len; ++i) {
               String fn = in.readUTF();
               String on = in.readUTF();
               String bn = in.readUTF();
               c.floorIds[i] = fn.isEmpty() ? -1 : (Vars.content.block(fn) != null ? Vars.content.block(fn).id : -1);
               c.overlayIds[i] = on.isEmpty() ? -1 : (Vars.content.block(on) != null ? Vars.content.block(on).id : 0);
               c.blockIds[i] = bn.isEmpty() ? 0 : (Vars.content.block(bn) != null ? Vars.content.block(bn).id : 0);
               c.isCenter[i] = in.readBoolean();
               c.teamIds[i] = in.readByte();
               c.rotations[i] = in.readByte();
               c.datas[i] = in.readByte();
               c.floorDatas[i] = in.readByte();
               c.overlayDatas[i] = in.readByte();
               c.extraDatas[i] = in.readInt();
            }
            var13 = c;
         } catch (Throwable var11) {
            try {
               in.close();
            } catch (Throwable var10) {
               var11.addSuppressed(var10);
            }
            throw var11;
         }
         in.close();
         return var13;
      } catch (Exception e) {
         Vars.ui.showException(e);
         return null;
      }
   }
}

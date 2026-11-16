package starlight.utils;
import arc.Core;
import arc.input.KeyCode;
import arc.util.Http;
import arc.util.Jval;
import arc.util.Log;
import arc.util.Strings;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.mod.Mods.LoadedMod;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.Vars;
public class UpdateChecker {
   private static final String REPOSITORY = "itzmellory/Starlight";
   private static boolean checking;
   public static void check() {
      if (checking) return;
      checking = true;
      Http.get("https://api.github.com/repos/" + REPOSITORY + "/releases?per_page=10", response -> {
         try {
            Jval.JsonArray releases = Jval.read(response.getResultAsString()).asArray();
            Jval latest = null;
            for (int i = 0; i < releases.size; i++) {
               Jval item = releases.get(i);
               if (!item.getBool("draft", false) && !item.getBool("prerelease", false)) {
                  latest = item;
                  break;
               }
            }
            if (latest == null) {
               Log.info("[Starlight] No stable GitHub release found.");
               return;
            }
            LoadedMod mod = Vars.mods.getMod("starlight");
            if (mod == null || compareVersions(latest.getString("tag_name", "0"), mod.meta.version) <= 0) return;
            showUpdateDialog(latest);
         } catch (Throwable error) {
            Log.err("[Starlight] Could not parse GitHub release information.", error);
         } finally {
            checking = false;
         }
      }, error -> {
         checking = false;
         Log.err("[Starlight] Update check failed.", error);
      });
   }
   private static int compareVersions(String first, String second) {
      int[] a = versionParts(first);
      int[] b = versionParts(second);
      for (int i = 0; i < Math.max(a.length, b.length); i++) {
         int av = i < a.length ? a[i] : 0;
         int bv = i < b.length ? b[i] : 0;
         if (av != bv) return Integer.compare(av, bv);
      }
      return 0;
   }
   private static int[] versionParts(String value) {
      String clean = value == null ? "" : value.trim().toLowerCase().replaceFirst("^[^0-9]*", "");
      String[] parts = clean.split("[^0-9]+");
      int[] result = new int[parts.length];
      for (int i = 0; i < parts.length; i++) {
         try { result[i] = Integer.parseInt(parts[i]); } catch (Exception ignored) { result[i] = 0; }
      }
      return result;
   }
   private static void showUpdateDialog(Jval release) {
      String tag = release.getString("tag_name", "new version");
      String notes = release.getString("body", "");
      String page = release.getString("html_url", "https://github.com/" + REPOSITORY + "/releases");
      String assetId = null;
      Jval.JsonArray assets = release.get("assets").asArray();
      for (int i = 0; i < assets.size; i++) {
         Jval asset = assets.get(i);
         if ("Starlight.jar".equalsIgnoreCase(asset.getString("name", ""))) {
            assetId = asset.getString("id");
            break;
         }
      }
      final String selectedAssetId = assetId;
      BaseDialog dialog = new BaseDialog("@starlight.update.title");
      dialog.cont.pane(table -> {
         table.add(Core.bundle.format("starlight.update.available", tag)).color(Pal.accent).wrap().width(Vars.mobile ? 420f : 560f).center().pad(8f).row();
         if (notes != null && !notes.trim().isEmpty()) {
            table.add("@starlight.update.notes").color(Pal.accent).padTop(12f).row();
            table.add(notes).wrap().width(Vars.mobile ? 420f : 560f).pad(8f).row();
         }
      }).width(Vars.mobile ? 460f : 600f);
      dialog.buttons.defaults().size(190f, 54f).pad(3f);
      dialog.buttons.button("@later", Icon.cancel, dialog::hide);
      if (selectedAssetId != null) {
         dialog.buttons.button("@starlight.update.install", Icon.download, () -> {
            dialog.hide();
            Vars.ui.mods.githubImportMod(REPOSITORY, true, selectedAssetId);
         });
      }
      dialog.buttons.button("@starlight.update.open", Icon.link, () -> {
         dialog.hide();
         Core.app.openURI(page);
      });
      dialog.keyDown(KeyCode.escape, dialog::hide);
      dialog.keyDown(KeyCode.back, dialog::hide);
      dialog.show();
   }
}

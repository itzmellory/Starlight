package starlight.upperuiutils;
import starlight.LeftSideUtils.StencilManager;
import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.ScissorStack;
import arc.graphics.g2d.TextureRegion;
import arc.input.GestureDetector;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.Table;
import arc.util.Reflect;
import arc.util.Tmp;
import java.lang.reflect.Field;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.editor.EditorTool;
import mindustry.editor.MapView;
import mindustry.game.Team;
import mindustry.game.EventType.Trigger;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.OverlayFloor;
public class LandCopyPast {
   public static Mode mode;
   public static Clip clipboard;
   public static boolean copyFloor;
   public static boolean copyOre;
   public static boolean copyBlock;
   public static boolean overrideBlock;
   public static boolean overrideOre;
   public static boolean useEditorTeam;
   private static boolean selecting;
   private static boolean drawingCustom;
   private static boolean drawingLine;
   private static boolean draggingPaste;
   private static int selStartX;
   private static int selStartY;
   private static int selEndX;
   private static int selEndY;
   private static int lineStartX;
   private static int lineStartY;
   private static int lineEndX;
   private static int lineEndY;
   private static int lastDrawX;
   private static int lastDrawY;
   private static int pasteOriginX;
   private static int pasteOriginY;
   private static int dragLastX;
   private static int dragLastY;
   private static boolean camLocked;
   private static float lockedOx;
   private static float lockedOy;
   private static float lockedZoom;
   private static Table pasteActionBar;
   private static Element overlayElement;
   private static MapView hookedView;
   private static EditorTool savedVanillaTool;
   private static Field extraDataField;
   private static Field floorDataField;
   private static Field overlayDataField;
   private static Field gestureField;
   private static final Vec2 tmpV1;
   private static final Vec2 tmpV2;
   private static final Point2 tmpP1;
   public static void init() {
      Events.run(Trigger.update, LandCopyPast::update);
   }
   public static Element getOverlayElement() {
      return overlayElement;
   }
   public static int getExtra(Tile t) {
      try {
         if (extraDataField != null) {
            return extraDataField.getInt(t);
         }
      } catch (Throwable var2) {
      }
      return 0;
   }
   public static void setExtra(Tile t, int val) {
      try {
         if (extraDataField != null) {
            extraDataField.setInt(t, val);
         }
      } catch (Throwable var3) {
      }
   }
   public static byte getFloorData(Tile t) {
      try {
         if (floorDataField != null) {
            return floorDataField.getByte(t);
         }
      } catch (Throwable var2) {
      }
      return 0;
   }
   public static void setFloorData(Tile t, byte val) {
      try {
         if (floorDataField != null) {
            floorDataField.setByte(t, val);
         }
      } catch (Throwable var3) {
      }
   }
   public static byte getOverlayData(Tile t) {
      try {
         if (overlayDataField != null) {
            return overlayDataField.getByte(t);
         }
      } catch (Throwable var2) {
      }
      return 0;
   }
   public static void setOverlayData(Tile t, byte val) {
      try {
         if (overlayDataField != null) {
            overlayDataField.setByte(t, val);
         }
      } catch (Throwable var3) {
      }
   }
   public static void cancelMapGestures(MapView view) {
      try {
         if (gestureField != null) {
            GestureDetector gd = (GestureDetector)gestureField.get(view);
            if (gd != null) {
               gd.cancel();
            }
         }
      } catch (Throwable var2) {
      }
   }
   public static void lockCamera(MapView view) {
      try {
         lockedOx = (Float)Reflect.get(MapView.class, view, "offsetx");
         lockedOy = (Float)Reflect.get(MapView.class, view, "offsety");
         lockedZoom = (Float)Reflect.get(MapView.class, view, "zoom");
         camLocked = true;
         cancelMapGestures(view);
      } catch (Throwable var2) {
      }
   }
   public static void unlockCamera() {
      camLocked = false;
   }
   public static void enforceCameraLock(MapView view) {
      if (camLocked && view != null) {
         try {
            Reflect.set(MapView.class, view, "offsetx", lockedOx);
            Reflect.set(MapView.class, view, "offsety", lockedOy);
            Reflect.set(MapView.class, view, "zoom", lockedZoom);
         } catch (Throwable var2) {
         }
      }
   }
   public static boolean calcGridBounds(MapView view, float[] out) {
      try {
         float ox = (Float)Reflect.get(MapView.class, view, "offsetx");
         float oy = (Float)Reflect.get(MapView.class, view, "offsety");
         float zoom = (Float)Reflect.get(MapView.class, view, "zoom");
         int mw = Vars.editor.width();
         int mh = Vars.editor.height();
         if (mw > 0 && mh > 0) {
            float ratio = 1.0F / ((float)mw / (float)mh);
            float size = Math.min(view.getWidth(), view.getHeight());
            float sclw = size * zoom;
            float sclh = size * zoom * ratio;
            float localBlX = view.getWidth() / 2.0F + ox * zoom - sclw / 2.0F;
            float localBlY = view.getHeight() / 2.0F + oy * zoom - sclh / 2.0F;
            out[0] = localBlX;
            out[1] = localBlY;
            out[2] = sclw / (float)mw;
            out[3] = sclh / (float)mh;
            return true;
         } else {
            return false;
         }
      } catch (Throwable var13) {
         return false;
      }
   }
   public static Point2 projectTile(MapView view, float localX, float localY) {
      return projectRaw(view, localX, localY);
   }
   public static Point2 projectRaw(MapView view, float x, float y) {
      try {
         float offsetx = (Float)Reflect.get(MapView.class, view, "offsetx");
         float offsety = (Float)Reflect.get(MapView.class, view, "offsety");
         float zoom = (Float)Reflect.get(MapView.class, view, "zoom");
         float ratio = 1.0F / ((float)Vars.editor.width() / (float)Vars.editor.height());
         float size = Math.min(view.getWidth(), view.getHeight());
         float sclwidth = size * zoom;
         float sclheight = size * zoom * ratio;
         float wx = (x - view.getWidth() / 2.0F + sclwidth / 2.0F - offsetx * zoom) / sclwidth * (float)Vars.editor.width();
         float wy = (y - view.getHeight() / 2.0F + sclheight / 2.0F - offsety * zoom) / sclheight * (float)Vars.editor.height();
         return tmpP1.set(Mathf.floor(wx), Mathf.floor(wy));
      } catch (Throwable var12) {
         return tmpP1.set(0, 0);
      }
   }
   public static Point2 currentMouseTile(MapView view) {
      Vec2 local = view.screenToLocalCoordinates(tmpV1.set((float)Core.input.mouseX(), (float)Core.input.mouseY()));
      return projectRaw(view, local.x, local.y);
   }
   public static Vec2 tileToOverlay(MapView view, Element overlay, float tx, float ty, Vec2 out) {
      try {
         float offsetx = (Float)Reflect.get(MapView.class, view, "offsetx");
         float offsety = (Float)Reflect.get(MapView.class, view, "offsety");
         float zoom = (Float)Reflect.get(MapView.class, view, "zoom");
         float ratio = 1.0F / ((float)Vars.editor.width() / (float)Vars.editor.height());
         float size = Math.min(view.getWidth(), view.getHeight());
         float sclwidth = size * zoom;
         float sclheight = size * zoom * ratio;
         float vx = tx / (float)Vars.editor.width() * sclwidth + offsetx * zoom - sclwidth / 2.0F + view.getWidth() / 2.0F;
         float vy = ty / (float)Vars.editor.height() * sclheight + offsety * zoom - sclheight / 2.0F + view.getHeight() / 2.0F;
         Vec2 stage = view.localToStageCoordinates(out.set(vx, vy));
         return overlay != null ? overlay.stageToLocalCoordinates(stage) : stage;
      } catch (Throwable var15) {
         return out.set(0.0F, 0.0F);
      }
   }
   private static void refreshRenderer() {
      try {
         Object r = Reflect.get(Vars.editor, "renderer");
         if (r != null) {
            Reflect.invoke(r, "resize", new Object[]{Vars.editor.width(), Vars.editor.height()}, new Class[]{Integer.TYPE, Integer.TYPE});
         }
      } catch (Throwable var1) {
      }
   }
   public static void toggleMode(Mode m) {
      setMode(mode == m ? LandCopyPast.Mode.none : m);
   }
   public static void setMode(Mode m) {
      MapView view = Vars.ui != null && Vars.ui.editor != null ? Vars.ui.editor.getView() : null;
      if (mode == LandCopyPast.Mode.none && m != LandCopyPast.Mode.none && view != null) {
         savedVanillaTool = view.getTool();
         view.setTool(EditorTool.pick);
      } else if (mode != LandCopyPast.Mode.none && m == LandCopyPast.Mode.none && view != null && savedVanillaTool != null) {
         view.setTool(savedVanillaTool);
      }
      mode = m;
      selecting = false;
      draggingPaste = false;
      unlockCamera();
      if (m != LandCopyPast.Mode.none) {
         CustomShapeManager.line45Mode = false;
      }
      if (m == LandCopyPast.Mode.paste && clipboard != null) {
         if (view != null) {
            Point2 c = projectRaw(view, view.getWidth() * 0.5F, view.getHeight() * 0.5F);
            pasteOriginX = c.x - clipboard.width / 2;
            pasteOriginY = c.y - clipboard.height / 2;
         } else {
            pasteOriginX = (Vars.editor.width() - clipboard.width) / 2;
            pasteOriginY = (Vars.editor.height() - clipboard.height) / 2;
         }
         rebuildPasteBar();
      } else {
         removePasteBar();
      }
   }
   private static void update() {
      if (Vars.ui != null && Vars.ui.editor != null && Vars.ui.editor.isShown()) {
         final MapView view = Vars.ui.editor.getView();
         if (view != null) {
            enforceCameraLock(view);
            if (mode != LandCopyPast.Mode.none && view.getTool() != EditorTool.pick) {
               savedVanillaTool = view.getTool();
               setMode(LandCopyPast.Mode.none);
            }
            if (overlayElement == null || overlayElement.parent != Vars.ui.editor) {
               if (overlayElement != null) {
                  overlayElement.remove();
               }
               overlayElement = new Element() {
                  public void draw() {
                     if (Vars.ui.editor.isShown()) {
                        LandCopyPast.enforceCameraLock(view);
                        Vec2 s1 = view.localToStageCoordinates(LandCopyPast.tmpV1.set(0.0F, 0.0F));
                        Vec2 s2 = view.localToStageCoordinates(LandCopyPast.tmpV2.set(view.getWidth(), view.getHeight()));
                        Vec2 l1 = this.stageToLocalCoordinates(s1);
                        Vec2 l2 = this.stageToLocalCoordinates(s2);
                        float cx = Math.min(l1.x, l2.x);
                        float cy = Math.min(l1.y, l2.y);
                        float cw = Math.abs(l2.x - l1.x);
                        float ch = Math.abs(l2.y - l1.y);
                        if (ScissorStack.push(Tmp.r1.set(cx, cy, cw, ch))) {
                           StencilManager.drawOnOverlay(view, this);
                           LandCopyPast.drawOverlayContent(view, this);
                           ScissorStack.pop();
                        }
                     }
                  }
               };
               overlayElement.touchable = Touchable.disabled;
               Vars.ui.editor.addChild(overlayElement);
            }
            overlayElement.setBounds(0.0F, 0.0F, Vars.ui.editor.getWidth(), Vars.ui.editor.getHeight());
            if (hookedView != view) {
               hookedView = view;
               injectViewHooks(view);
            }
         }
      } else {
         hookedView = null;
         if (overlayElement != null) {
            overlayElement.remove();
            overlayElement = null;
         }
         removePasteBar();
         unlockCamera();
      }
   }
   private static void rebuildPasteBar() {
      removePasteBar();
      if (Vars.ui != null && Vars.ui.editor != null && Vars.ui.editor.getView() != null) {
         Group parent = Vars.ui.editor.getView().parent;
         pasteActionBar = new Table();
         pasteActionBar.setFillParent(true);
         pasteActionBar.bottom();
         pasteActionBar.touchable = Touchable.childrenOnly;
         Table box = new Table(Tex.pane);
         box.margin(7.0F);
         box.defaults().height(46.0F).pad(4.0F);
         box.button(Core.bundle.get("upper.apply", "Paste Here"), Icon.ok, Styles.flatt, () -> pasteAt(pasteOriginX, pasteOriginY)).width(170.0F);
         box.button(Core.bundle.get("upper.cancel", "Cancel"), Icon.cancel, Styles.flatt, () -> setMode(LandCopyPast.Mode.none)).width(140.0F);
         pasteActionBar.add(box).padBottom(12.0F);
         parent.addChild(pasteActionBar);
      }
   }
   private static void removePasteBar() {
      if (pasteActionBar != null) {
         pasteActionBar.remove();
         pasteActionBar = null;
      }
   }
   private static boolean shouldInterceptVanillaTool(EditorTool tool) {
      if (tool != EditorTool.line || !CustomShapeManager.line45Mode && CustomShapeManager.currentShape == CustomShapeManager.BrushShape.circle && !(Vars.editor.brushSize > 20.0F)) {
         return (tool == EditorTool.pencil || tool == EditorTool.eraser || tool == EditorTool.spray) && CustomShapeManager.currentShape != CustomShapeManager.BrushShape.circle && (tool != EditorTool.pencil || tool.mode != 2 || CustomShapeManager.currentShape != CustomShapeManager.BrushShape.square);
      } else {
         return true;
      }
   }
   private static void injectViewHooks(final MapView view) {
      view.addCaptureListener(new InputListener() {
         public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
            if (pointer == 0 && button == KeyCode.mouseLeft) {
               if (StencilManager.onTouchDown(view, x, y)) {
                  LandCopyPast.lockCamera(view);
                  event.cancel();
                  event.stop();
                  return true;
               } else {
                  Point2 p = LandCopyPast.projectRaw(view, x, y);
                  if (LandCopyPast.mode != LandCopyPast.Mode.copy && LandCopyPast.mode != LandCopyPast.Mode.cut) {
                     if (LandCopyPast.mode == LandCopyPast.Mode.paste && LandCopyPast.clipboard != null) {
                        LandCopyPast.draggingPaste = true;
                        LandCopyPast.dragLastX = p.x;
                        LandCopyPast.dragLastY = p.y;
                        LandCopyPast.lockCamera(view);
                        event.cancel();
                        event.stop();
                        return true;
                     } else {
                        EditorTool tool = view.getTool();
                        if (LandCopyPast.shouldInterceptVanillaTool(tool)) {
                           if (tool == EditorTool.line) {
                              LandCopyPast.drawingLine = true;
                              LandCopyPast.lineStartX = LandCopyPast.lineEndX = p.x;
                              LandCopyPast.lineStartY = LandCopyPast.lineEndY = p.y;
                           } else {
                              LandCopyPast.drawingCustom = true;
                              LandCopyPast.lastDrawX = p.x;
                              LandCopyPast.lastDrawY = p.y;
                              LandCopyPast.applyToolStamp(tool, p.x, p.y);
                           }
                           LandCopyPast.lockCamera(view);
                           event.cancel();
                           event.stop();
                           return true;
                        } else {
                           return false;
                        }
                     }
                  } else {
                     LandCopyPast.selecting = true;
                     LandCopyPast.selStartX = LandCopyPast.selEndX = p.x;
                     LandCopyPast.selStartY = LandCopyPast.selEndY = p.y;
                     LandCopyPast.lockCamera(view);
                     event.cancel();
                     event.stop();
                     return true;
                  }
               }
            } else {
               return false;
            }
         }
         public void touchDragged(InputEvent event, float x, float y, int pointer) {
            if (pointer == 0) {
               LandCopyPast.enforceCameraLock(view);
               if (StencilManager.onTouchDragged(view, x, y)) {
                  LandCopyPast.enforceCameraLock(view);
                  event.cancel();
                  event.stop();
               } else {
                  Point2 p = LandCopyPast.projectRaw(view, x, y);
                  if (!LandCopyPast.selecting || LandCopyPast.mode != LandCopyPast.Mode.copy && LandCopyPast.mode != LandCopyPast.Mode.cut) {
                     if (LandCopyPast.mode == LandCopyPast.Mode.paste && LandCopyPast.draggingPaste) {
                        LandCopyPast.pasteOriginX = p.x - LandCopyPast.dragLastX;
                        LandCopyPast.pasteOriginY = p.y - LandCopyPast.dragLastY;
                        LandCopyPast.dragLastX = p.x;
                        LandCopyPast.dragLastY = p.y;
                        event.cancel();
                        event.stop();
                     } else if (LandCopyPast.drawingLine) {
                        if (CustomShapeManager.line45Mode) {
                           Point2 snapped = CustomShapeManager.snap45(LandCopyPast.lineStartX, LandCopyPast.lineStartY, p.x, p.y);
                           LandCopyPast.lineEndX = snapped.x;
                           LandCopyPast.lineEndY = snapped.y;
                        } else {
                           LandCopyPast.lineEndX = p.x;
                           LandCopyPast.lineEndY = p.y;
                        }
                        event.cancel();
                        event.stop();
                     } else {
                        if (LandCopyPast.drawingCustom) {
                           EditorTool tool = view.getTool();
                           CustomShapeManager.traceLine(LandCopyPast.lastDrawX, LandCopyPast.lastDrawY, p.x, p.y, (lx, ly) -> LandCopyPast.applyToolStamp(tool, lx, ly));
                           LandCopyPast.lastDrawX = p.x;
                           LandCopyPast.lastDrawY = p.y;
                           event.cancel();
                           event.stop();
                        }
                     }
                  } else {
                     LandCopyPast.selEndX = p.x;
                     LandCopyPast.selEndY = p.y;
                     event.cancel();
                     event.stop();
                  }
               }
            }
         }
         public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
            if (pointer == 0 && button == KeyCode.mouseLeft) {
               LandCopyPast.enforceCameraLock(view);
               LandCopyPast.unlockCamera();
               if (StencilManager.onTouchUp()) {
                  event.cancel();
                  event.stop();
               } else {
                  Point2 p = LandCopyPast.projectRaw(view, x, y);
                  if (!LandCopyPast.selecting || LandCopyPast.mode != LandCopyPast.Mode.copy && LandCopyPast.mode != LandCopyPast.Mode.cut) {
                     if (LandCopyPast.draggingPaste) {
                        LandCopyPast.draggingPaste = false;
                        event.cancel();
                        event.stop();
                     } else if (LandCopyPast.drawingLine) {
                        EditorTool tool = view.getTool();
                        CustomShapeManager.traceLine(LandCopyPast.lineStartX, LandCopyPast.lineStartY, LandCopyPast.lineEndX, LandCopyPast.lineEndY, (lx, ly) -> LandCopyPast.applyToolStamp(tool, lx, ly));
                        LandCopyPast.drawingLine = false;
                        Vars.editor.flushOp();
                        Vars.ui.editor.resetSaved();
                        event.cancel();
                        event.stop();
                     } else {
                        if (LandCopyPast.drawingCustom) {
                           LandCopyPast.drawingCustom = false;
                           Vars.editor.flushOp();
                           Vars.ui.editor.resetSaved();
                           event.cancel();
                           event.stop();
                        }
                     }
                  } else {
                     LandCopyPast.selEndX = p.x;
                     LandCopyPast.selEndY = p.y;
                     LandCopyPast.captureSelection(LandCopyPast.mode == LandCopyPast.Mode.cut);
                     LandCopyPast.selecting = false;
                     LandCopyPast.setMode(LandCopyPast.Mode.paste);
                     event.cancel();
                     event.stop();
                  }
               }
            }
         }
      });
   }
   private static void applyToolStamp(EditorTool tool, int cx, int cy) {
      if (tool == EditorTool.eraser) {
         boolean eraseOres = tool.mode == 0;
         CustomShapeManager.iterateBrush(cx, cy, Vars.editor.brushSize, CustomShapeManager.currentShape, (t) -> {
            if (eraseOres) {
               t.clearOverlay();
            } else {
               t.remove();
            }
         });
      } else if (tool == EditorTool.spray) {
         double chance = 0.012;
         CustomShapeManager.iterateBrush(cx, cy, Vars.editor.brushSize, CustomShapeManager.currentShape, (t) -> {
            if (Mathf.chance(chance)) {
               placeCurrentDrawBlock(t, false, false);
            }
         });
      } else {
         boolean replace = tool == EditorTool.pencil && tool.mode == 0 || tool == EditorTool.line && tool.mode == 0;
         boolean forceOverlay = tool == EditorTool.pencil && tool.mode == 3;
         Block db = Vars.editor.drawBlock;
         if (db != null && db.isMultiblock()) {
            if (cx >= 0 && cy >= 0 && cx < Vars.editor.width() && cy < Vars.editor.height()) {
               Vars.editor.drawBlocks(cx, cy);
            }
         } else {
            CustomShapeManager.iterateBrush(cx, cy, Vars.editor.brushSize, CustomShapeManager.currentShape, (t) -> placeCurrentDrawBlock(t, replace, forceOverlay));
         }
      }
   }
   private static void placeCurrentDrawBlock(Tile tile, boolean replace, boolean forceOverlay) {
      Block db = Vars.editor.drawBlock;
      if (db != null && tile != null) {
         if (!replace || tile.block() != Blocks.air || db.isFloor()) {
            if (db.isFloor() && db != Blocks.air) {
               if (!forceOverlay && !db.isOverlay()) {
                  if (!db.asFloor().wallOre || tile.block().solid) {
                     tile.setFloor(db.asFloor());
                     if (!(tile.overlay() instanceof OverlayFloor) && !db.asFloor().supportsOverlay) {
                        tile.setOverlay(Blocks.air);
                     }
                  }
               } else {
                  tile.setOverlay(db);
               }
            } else if (!tile.block().isMultiblock() || db.isMultiblock()) {
               tile.setBlock(db, Vars.editor.drawTeam, Vars.editor.rotation);
            }
         }
      }
   }
   private static void drawOverlayContent(MapView view, Element overlay) {
      try {
         if (selecting && (mode == LandCopyPast.Mode.copy || mode == LandCopyPast.Mode.cut)) {
            int minX = Math.min(selStartX, selEndX);
            int maxX = Math.max(selStartX, selEndX) + 1;
            int minY = Math.min(selStartY, selEndY);
            int maxY = Math.max(selStartY, selEndY) + 1;
            Vec2 a = tileToOverlay(view, overlay, (float)minX, (float)minY, tmpV1);
            Vec2 b = tileToOverlay(view, overlay, (float)maxX, (float)maxY, tmpV2);
            float rx = Math.min(a.x, b.x);
            float ry = Math.min(a.y, b.y);
            float rw = Math.abs(b.x - a.x);
            float rh = Math.abs(b.y - a.y);
            Draw.color(Pal.accent, 0.22F);
            Fill.crect(rx, ry, rw, rh);
            Draw.color(Pal.accent);
            Lines.stroke(2.0F);
            Lines.rect(rx, ry, rw, rh);
            Draw.reset();
         }
         if (mode == LandCopyPast.Mode.paste && clipboard != null) {
            clipboard.ensureTexture();
            Vec2 a = tileToOverlay(view, overlay, (float)pasteOriginX, (float)pasteOriginY, tmpV1);
            Vec2 b = tileToOverlay(view, overlay, (float)(pasteOriginX + clipboard.width), (float)(pasteOriginY + clipboard.height), tmpV2);
            float rx = Math.min(a.x, b.x);
            float ry = Math.min(a.y, b.y);
            float rw = Math.abs(b.x - a.x);
            float rh = Math.abs(b.y - a.y);
            if (clipboard.previewRegion != null) {
               Draw.color(1.0F, 1.0F, 1.0F, 0.65F);
               Draw.rect(clipboard.previewRegion, rx + rw / 2.0F, ry + rh / 2.0F, rw, rh);
            }
            Draw.color(draggingPaste ? Color.sky : Pal.accent);
            Lines.stroke(2.0F);
            Lines.rect(rx, ry, rw, rh);
            Draw.reset();
         }
         if (mode == LandCopyPast.Mode.none && !StencilManager.moveMode) {
            EditorTool tool = view.getTool();
            Point2 mp = currentMouseTile(view);
            if (drawingLine && tool == EditorTool.line) {
               CustomShapeManager.drawLineSteppedOutline(view, overlay, lineStartX, lineStartY, lineEndX, lineEndY, Vars.editor.brushSize, CustomShapeManager.currentShape);
            } else if ((tool == EditorTool.pencil || tool == EditorTool.eraser || tool == EditorTool.line || tool == EditorTool.spray) && (Vars.editor.brushSize > 20.0F || CustomShapeManager.currentShape != CustomShapeManager.BrushShape.circle || CustomShapeManager.line45Mode)) {
               CustomShapeManager.drawSteppedOutline(view, overlay, mp.x, mp.y, Vars.editor.brushSize, CustomShapeManager.currentShape);
            }
         }
      } catch (Throwable var12) {
      }
   }
   public static void captureSelection(boolean cut) {
      int minX = Math.min(selStartX, selEndX);
      int minY = Math.min(selStartY, selEndY);
      int maxX = Math.max(selStartX, selEndX);
      int maxY = Math.max(selStartY, selEndY);
      if (minX <= maxX && minY <= maxY) {
         int w = maxX - minX + 1;
         int h = maxY - minY + 1;
         int mapW = Vars.editor.width();
         int mapH = Vars.editor.height();
         Clip next = new Clip(w, h);
         for(int x = 0; x < w; ++x) {
            for(int y = 0; y < h; ++y) {
               int wx = minX + x;
               int wy = minY + y;
               int idx = x + y * w;
               if (wx >= 0 && wy >= 0 && wx < mapW && wy < mapH) {
                  Tile t = Vars.editor.tile(wx, wy);
                  if (t == null) {
                     next.floorIds[idx] = -1;
                     next.overlayIds[idx] = -1;
                  } else {
                     next.floorIds[idx] = copyFloor ? t.floorID() : -1;
                     next.overlayIds[idx] = copyOre ? t.overlayID() : -1;
                     boolean center = t.isCenter() && copyBlock;
                     next.blockIds[idx] = center ? t.blockID() : 0;
                     next.isCenter[idx] = center;
                     next.teamIds[idx] = (byte)t.getTeamID();
                     next.rotations[idx] = (byte)(t.build != null ? t.build.rotation : 0);
                     next.datas[idx] = t.data;
                     next.floorDatas[idx] = getFloorData(t);
                     next.overlayDatas[idx] = getOverlayData(t);
                     next.extraDatas[idx] = getExtra(t);
                  }
               } else {
                  next.floorIds[idx] = -1;
                  next.overlayIds[idx] = -1;
               }
            }
         }
         if (cut) {
            Vars.editor.load(() -> {
               for(int x = 0; x < w; ++x) {
                  for(int y = 0; y < h; ++y) {
                     int wx = minX + x;
                     int wy = minY + y;
                     if (wx >= 0 && wy >= 0 && wx < mapW && wy < mapH) {
                        Tile t = Vars.editor.tile(wx, wy);
                        if (t != null) {
                           if (copyBlock && t.isCenter()) {
                              t.remove();
                           }
                           if (copyOre) {
                              t.clearOverlay();
                           }
                        }
                     }
                  }
               }
            });
            refreshRenderer();
            Vars.editor.clearOp();
         }
         if (clipboard != null) {
            clipboard.dispose();
         }
         clipboard = next;
      }
   }
   public static void pasteAt(int ox, int oy) {
      if (clipboard != null) {
         int w = clipboard.width;
         int h = clipboard.height;
         int mapW = Vars.editor.width();
         int mapH = Vars.editor.height();
         Vars.editor.load(() -> {
            for(int x = 0; x < w; ++x) {
               for(int y = 0; y < h; ++y) {
                  int tx = ox + x;
                  int ty = oy + y;
                  if (tx >= 0 && ty >= 0 && tx < mapW && ty < mapH) {
                     int idx = x + y * w;
                     Tile tile = Vars.editor.tile(tx, ty);
                     if (tile != null) {
                        short fid = clipboard.floorIds[idx];
                        if (fid >= 0) {
                           Block fb = Vars.content.block(fid);
                           if (fb instanceof Floor) {
                              tile.setFloor((Floor)fb);
                           }
                           tile.data = clipboard.datas[idx];
                           setFloorData(tile, clipboard.floorDatas[idx]);
                           setOverlayData(tile, clipboard.overlayDatas[idx]);
                           setExtra(tile, clipboard.extraDatas[idx]);
                        }
                        short oid = clipboard.overlayIds[idx];
                        if (oid >= 0) {
                           Block ob = Vars.content.block(oid);
                           if (ob != null && (overrideOre || tile.overlay() == Blocks.air)) {
                              tile.setOverlay(ob);
                           }
                        }
                     }
                  }
               }
            }
            for(int x = 0; x < w; ++x) {
               for(int y = 0; y < h; ++y) {
                  int tx = ox + x;
                  int ty = oy + y;
                  if (tx >= 0 && ty >= 0 && tx < mapW && ty < mapH) {
                     int idx = x + y * w;
                     if (clipboard.isCenter[idx] && clipboard.blockIds[idx] > 0) {
                        Block bb = Vars.content.block(clipboard.blockIds[idx]);
                        Tile tile = Vars.editor.tile(tx, ty);
                        if (bb != null && tile != null && (overrideBlock || tile.block() == Blocks.air)) {
                           Team tm = useEditorTeam ? Vars.editor.drawTeam : Team.get(clipboard.teamIds[idx] & 255);
                           tile.setBlock(bb, tm, clipboard.rotations[idx]);
                        }
                     }
                  }
               }
            }
         });
         refreshRenderer();
         Vars.editor.clearOp();
         Vars.ui.editor.resetSaved();
      }
   }
   public static void rotateClipboard() {
      if (clipboard != null) {
         int w = clipboard.width;
         int h = clipboard.height;
         Clip next = new Clip(h, w);
         for(int x = 0; x < w; ++x) {
            for(int y = 0; y < h; ++y) {
               int src = x + y * w;
               int nx = h - 1 - y;
               int dst = nx + x * h;
               next.floorIds[dst] = clipboard.floorIds[src];
               next.overlayIds[dst] = clipboard.overlayIds[src];
               next.blockIds[dst] = clipboard.blockIds[src];
               next.isCenter[dst] = clipboard.isCenter[src];
               next.teamIds[dst] = clipboard.teamIds[src];
               next.rotations[dst] = (byte)(clipboard.rotations[src] + 1 & 3);
               next.datas[dst] = clipboard.datas[src];
               next.floorDatas[dst] = clipboard.floorDatas[src];
               next.overlayDatas[dst] = clipboard.overlayDatas[src];
               next.extraDatas[dst] = clipboard.extraDatas[src];
            }
         }
         clipboard.dispose();
         clipboard = next;
      }
   }
   public static void flipX() {
      if (clipboard != null) {
         int w = clipboard.width;
         int h = clipboard.height;
         Clip next = new Clip(w, h);
         for(int x = 0; x < w; ++x) {
            for(int y = 0; y < h; ++y) {
               int src = x + y * w;
               int dst = w - 1 - x + y * w;
               next.floorIds[dst] = clipboard.floorIds[src];
               next.overlayIds[dst] = clipboard.overlayIds[src];
               next.blockIds[dst] = clipboard.blockIds[src];
               next.isCenter[dst] = clipboard.isCenter[src];
               next.teamIds[dst] = clipboard.teamIds[src];
               byte r = clipboard.rotations[src];
               if (r == 0) {
                  r = 2;
               } else if (r == 2) {
                  r = 0;
               }
               next.rotations[dst] = r;
               next.datas[dst] = clipboard.datas[src];
               next.floorDatas[dst] = clipboard.floorDatas[src];
               next.overlayDatas[dst] = clipboard.overlayDatas[src];
               next.extraDatas[dst] = clipboard.extraDatas[src];
            }
         }
         clipboard.dispose();
         clipboard = next;
      }
   }
   public static void flipY() {
      if (clipboard != null) {
         int w = clipboard.width;
         int h = clipboard.height;
         Clip next = new Clip(w, h);
         for(int x = 0; x < w; ++x) {
            for(int y = 0; y < h; ++y) {
               int src = x + y * w;
               int dst = x + (h - 1 - y) * w;
               next.floorIds[dst] = clipboard.floorIds[src];
               next.overlayIds[dst] = clipboard.overlayIds[src];
               next.blockIds[dst] = clipboard.blockIds[src];
               next.isCenter[dst] = clipboard.isCenter[src];
               next.teamIds[dst] = clipboard.teamIds[src];
               byte r = clipboard.rotations[src];
               if (r == 1) {
                  r = 3;
               } else if (r == 3) {
                  r = 1;
               }
               next.rotations[dst] = r;
               next.datas[dst] = clipboard.datas[src];
               next.floorDatas[dst] = clipboard.floorDatas[src];
               next.overlayDatas[dst] = clipboard.overlayDatas[src];
               next.extraDatas[dst] = clipboard.extraDatas[src];
            }
         }
         clipboard.dispose();
         clipboard = next;
      }
   }
   public static void showCopyPasteSettings() {
      BaseDialog d = new BaseDialog(Core.bundle.get("upper.settings", "Copy / Paste Options"));
      Table t = new Table(Tex.pane);
      t.margin(12.0F);
      t.defaults().left().pad(7.0F);
      t.add("[accent]▸ Copy Layers[]").padBottom(4.0F).row();
      t.check("Copy Floor", copyFloor, (v) -> copyFloor = v).row();
      t.check("Copy Ore / Overlay", copyOre, (v) -> copyOre = v).row();
      t.check("Copy Blocks / Walls", copyBlock, (v) -> copyBlock = v).row();
      t.add("[accent]▸ Paste Rules[]").padTop(12.0F).padBottom(4.0F).row();
      t.check("Override Existing Blocks", overrideBlock, (v) -> overrideBlock = v).row();
      t.check("Override Existing Ores", overrideOre, (v) -> overrideOre = v).row();
      t.check("Use Active Editor Team", useEditorTeam, (v) -> useEditorTeam = v).row();
      d.cont.add(t);
      d.addCloseButton();
      d.show();
   }
   static {
      mode = LandCopyPast.Mode.none;
      copyFloor = true;
      copyOre = true;
      copyBlock = true;
      overrideBlock = true;
      overrideOre = true;
      useEditorTeam = false;
      selecting = false;
      drawingCustom = false;
      drawingLine = false;
      draggingPaste = false;
      lastDrawX = -1;
      lastDrawY = -1;
      pasteOriginX = 0;
      pasteOriginY = 0;
      camLocked = false;
      tmpV1 = new Vec2();
      tmpV2 = new Vec2();
      tmpP1 = new Point2();
      try {
         extraDataField = Tile.class.getDeclaredField("extraData");
         extraDataField.setAccessible(true);
      } catch (Throwable var4) {
      }
      try {
         floorDataField = Tile.class.getDeclaredField("floorData");
         floorDataField.setAccessible(true);
      } catch (Throwable var3) {
      }
      try {
         overlayDataField = Tile.class.getDeclaredField("overlayData");
         overlayDataField.setAccessible(true);
      } catch (Throwable var2) {
      }
      try {
         gestureField = MapView.class.getDeclaredField("gestureDetector");
         gestureField.setAccessible(true);
      } catch (Throwable var1) {
      }
   }
   public static enum Mode {
      none,
      copy,
      cut,
      paste;
      private static Mode[] $values() {
         return new Mode[]{none, copy, cut, paste};
      }
   }
   public static class Clip {
      public final int width;
      public final int height;
      public final short[] floorIds;
      public final short[] overlayIds;
      public final short[] blockIds;
      public final boolean[] isCenter;
      public final byte[] teamIds;
      public final byte[] rotations;
      public final byte[] datas;
      public final byte[] floorDatas;
      public final byte[] overlayDatas;
      public final int[] extraDatas;
      public Texture previewTex;
      public TextureRegion previewRegion;
      public Clip(int w, int h) {
         this.width = w;
         this.height = h;
         int len = w * h;
         this.floorIds = new short[len];
         this.overlayIds = new short[len];
         this.blockIds = new short[len];
         this.isCenter = new boolean[len];
         this.teamIds = new byte[len];
         this.rotations = new byte[len];
         this.datas = new byte[len];
         this.floorDatas = new byte[len];
         this.overlayDatas = new byte[len];
         this.extraDatas = new int[len];
      }
      public void ensureTexture() {
         if (this.previewRegion == null) {
            Pixmap pm = new Pixmap(this.width, this.height);
            for(int x = 0; x < this.width; ++x) {
               for(int y = 0; y < this.height; ++y) {
                  int idx = x + y * this.width;
                  int col = 0;
                  int extra = this.extraDatas[idx];
                  if (extra != 0) {
                     col = extra | 255;
                  } else if (this.blockIds[idx] > 0) {
                     Block b = Vars.content.block(this.blockIds[idx]);
                     if (b != null && b.mapColor != null) {
                        col = b.mapColor.rgba8888();
                     }
                  } else if (this.overlayIds[idx] > 0) {
                     Block o = Vars.content.block(this.overlayIds[idx]);
                     if (o != null && o.mapColor != null) {
                        col = o.mapColor.rgba8888();
                     }
                  } else if (this.floorIds[idx] >= 0) {
                     Block f = Vars.content.block(this.floorIds[idx]);
                     if (f != null && f.mapColor != null) {
                        col = f.mapColor.rgba8888();
                     }
                  }
                  pm.setRaw(x, this.height - 1 - y, col);
               }
            }
            this.previewTex = new Texture(pm);
            this.previewRegion = new TextureRegion(this.previewTex);
            pm.dispose();
         }
      }
      public void dispose() {
         if (this.previewTex != null) {
            this.previewTex.dispose();
            this.previewTex = null;
            this.previewRegion = null;
         }
      }
   }
                            }

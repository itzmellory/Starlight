package mt.utils;
import arc.struct.ObjectMap;
import java.lang.reflect.Field;
public class RefUtils {
   public static ObjectMap<String, Field> fields = new ObjectMap();
   public static Field getField(Class<?> clazz, String name) {
      Field f = (Field)fields.get(clazz.getName() + "." + name);
      if (f == null) {
         try {
            Field f2 = clazz.getDeclaredField(name);
            f2.setAccessible(true);
            fields.put(clazz.getName() + "." + name, f2);
            return f2;
         } catch (Exception var4) {
            return null;
         }
      } else {
         return f;
      }
   }
   public static <T> T getValue(Class<?> clazz, String name) {
      try {
         return (T)getField(clazz, name).get(clazz);
      } catch (Exception var3) {
         return null;
      }
   }
   public static <T> T getValue(Object obj, String name) {
      try {
         return (T)getField(obj.getClass(), name).get(obj);
      } catch (Exception var3) {
         return null;
      }
   }
   public static <T> T getValue(Field field, Object obj) {
      try {
         return (T)field.get(obj);
      } catch (Exception var3) {
         return null;
      }
   }
   public static <T> T getValue(Field field, Class<?> clazz) {
      try {
         return (T)field.get(clazz);
      } catch (Exception var3) {
         return null;
      }
   }
   public static void setValue(Object obj, String name, Object value) {
      if (obj != null && value != null) {
         try {
            getField(obj.getClass(), name).set(obj, value);
         } catch (Exception var4) {
         }
      }
   }
}

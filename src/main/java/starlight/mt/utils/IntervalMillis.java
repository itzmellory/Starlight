package mt.utils;
import arc.util.Time;
import java.util.Arrays;
public class IntervalMillis {
   long[] times;
   public IntervalMillis(int capacity) {
      this.times = new long[capacity];
   }
   public IntervalMillis() {
      this(1);
   }
   public boolean get(long time) {
      return this.get(0, time);
   }
   public boolean get(int id, long time) {
      if (id >= this.times.length) {
         throw new RuntimeException("Out of bounds! Max timer size is " + this.times.length + "!");
      } else {
         boolean got = this.check(id, time);
         if (got) {
            this.times[id] = Time.millis();
         }
         return got;
      }
   }
   public boolean check(int id, long time) {
      return Time.millis() - this.times[id] >= time || Time.millis() < this.times[id];
   }
   public void reset(int id, long time) {
      this.times[id] = Time.millis() - time;
   }
   public void clear() {
      Arrays.fill(this.times, 0L);
   }
   public long getTime(int id) {
      return Time.millis() - this.times[id];
   }
   public long[] getTimes() {
      return this.times;
   }
}

package com.poornima.ratelimiter.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

public class MutableClock extends Clock{
   private Instant currentInstant;
   private final ZoneId zoneId;

   public MutableClock(Instant initialInstant, ZoneId zoneId){
    this.currentInstant = initialInstant;
    this.zoneId = zoneId;
   }

   @Override
   public ZoneId getZone(){
    return zoneId;
   }

   public Clock withZone(ZoneId zone){
    return new MutableClock(currentInstant, zone);
   }

   @Override
    public Instant instant() {
        return currentInstant;
    }

    public void advance(Duration duration) {
        currentInstant = currentInstant.plus(duration);
    }
}

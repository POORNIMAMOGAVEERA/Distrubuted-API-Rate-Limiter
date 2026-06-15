package com.poornima.ratelimiter.benchmark;

public final class MemoryUtil {

    private MemoryUtil() {}

    public static long usedMemoryInBytes() {
        Runtime runtime = Runtime.getRuntime();

        return runtime.totalMemory() - runtime.freeMemory();
    }

    public static double bytesToMB(long bytes) {
        return bytes / (1024.0 * 1024.0);
    }
}
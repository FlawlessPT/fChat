package pt.flawless.fchat.chat;

public final class RangeFilter {
    private RangeFilter() {}

    public static boolean within(String senderWorld, double sx, double sy, double sz,
                                 String world, double x, double y, double z, double radius) {
        if (!senderWorld.equals(world)) return false;
        double dx = sx - x, dy = sy - y, dz = sz - z;
        return dx * dx + dy * dy + dz * dz <= radius * radius;
    }
}

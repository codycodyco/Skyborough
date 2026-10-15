package us.talabrek.ultimateskyblock.borough;

/** Inclusive horizontal bounds; reservations cover the entire world height. */
public record BoroughBounds(int minX, int minZ, int maxX, int maxZ) {
    public BoroughBounds {
        if (minX > maxX || minZ > maxZ) throw new IllegalArgumentException("Invalid city bounds");
    }

    public boolean intersects(int x, int z, int radius, int gap) {
        return (long) x + radius - 1 >= (long) minX - gap
            && (long) x - radius <= (long) maxX + gap
            && (long) z + radius - 1 >= (long) minZ - gap
            && (long) z - radius <= (long) maxZ + gap;
    }

    public int centerX(int distance) {
        return (int) (Math.round(((minX + (long) maxX) / 2.0) / distance) * distance);
    }

    public int centerZ(int distance) {
        return (int) (Math.round(((minZ + (long) maxZ) / 2.0) / distance) * distance);
    }
}

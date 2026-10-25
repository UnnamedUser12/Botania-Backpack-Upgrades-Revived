package velrondevs.botaniabackpack.util;

public final class ManaStorageMath {
	public static int clamp(int mana, int capacity) {
		return (int) Math.max(0L, Math.min((long) mana, Math.max(0L, capacity)));
	}

	public static int applyDelta(int mana, int delta, int capacity) {
		long clampedCapacity = Math.max(0L, capacity);
		return (int) Math.max(0L, Math.min((long) mana + delta, clampedCapacity));
	}

	private ManaStorageMath() {}
}

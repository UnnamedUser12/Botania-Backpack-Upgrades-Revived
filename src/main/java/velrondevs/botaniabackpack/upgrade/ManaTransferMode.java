package velrondevs.botaniabackpack.upgrade;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import java.util.Arrays;

public enum ManaTransferMode implements StringRepresentable {
	BOTH("both", true, true),
	INPUT("input", true, false),
	OUTPUT("output", false, true);

	public static final Codec<ManaTransferMode> CODEC = StringRepresentable.fromEnum(ManaTransferMode::values);
	public static final StreamCodec<RegistryFriendlyByteBuf, ManaTransferMode> STREAM_CODEC = ByteBufCodecs.idMapper(
			id -> values()[Math.floorMod(id, values().length)], ManaTransferMode::ordinal).cast();

	private final String serializedName;
	private final boolean acceptsMana;
	private final boolean providesMana;

	ManaTransferMode(String serializedName, boolean acceptsMana, boolean providesMana) {
		this.serializedName = serializedName;
		this.acceptsMana = acceptsMana;
		this.providesMana = providesMana;
	}

	@Override
	public String getSerializedName() {
		return serializedName;
	}

	public boolean acceptsMana() {
		return acceptsMana;
	}

	public boolean providesMana() {
		return providesMana;
	}

	public ManaTransferMode next() {
		ManaTransferMode[] values = values();
		return values[(ordinal() + 1) % values.length];
	}

	public static ManaTransferMode fromName(String name) {
		return Arrays.stream(values()).filter(value -> value.serializedName.equals(name)).findFirst().orElse(BOTH);
	}
}

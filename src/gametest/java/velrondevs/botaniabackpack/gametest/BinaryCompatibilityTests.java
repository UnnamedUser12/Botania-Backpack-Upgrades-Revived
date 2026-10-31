package velrondevs.botaniabackpack.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import velrondevs.botaniabackpack.BotaniaBackpackUpgrades;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Static link check of every class of this mod against the Sophisticated Core / Sophisticated Backpacks / Botania jars
 * that are actually installed. The dedicated test server cannot load client-only classes (so the GUI code is never run),
 * therefore this reads the class files as bytes and verifies that every referenced class, method and field exists. A
 * NoClassDefFoundError / NoSuchMethodError caused by an API change of those mods is reported here, client code included.
 */
@GameTestHolder(BotaniaBackpackUpgrades.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BinaryCompatibilityTests {
	private static final String TEMPLATE = "empty";
	private static final List<String> CHECKED_PREFIXES = List.of("net/p3pp3rf1y/", "velrondevs/botania/");
	private static final List<String> UNCHECKED_PARENT_PREFIXES = List.of("java/", "net/minecraft/", "net/neoforged/", "com/mojang/", "it/unimi/");
	private static final String OWN_PREFIX = "velrondevs/botaniabackpack/";
	private static final Pattern DESCRIPTOR_CLASS = Pattern.compile("L([^;]+);");

	@GameTest(template = TEMPLATE)
	public static void everyReferenceToSophisticatedAndBotaniaResolves(GameTestHelper helper) throws Exception {
		ClassLoader loader = BinaryCompatibilityTests.class.getClassLoader();
		List<String> ownClasses = findOwnClasses();
		helper.assertTrue(ownClasses.size() > 20, "found the classes of this mod, found " + ownClasses.size());

		Map<String, ClassInfo> cache = new HashMap<>();
		Set<String> problems = new TreeSet<>();
		int checkedMembers = 0;
		for (String own : ownClasses) {
			ClassInfo info = read(loader, own, cache);
			for (String referenced : info.referencedClasses) {
				if (isChecked(referenced) && read(loader, referenced, cache) == null) {
					problems.add(own + " -> missing class " + referenced);
				}
			}
			for (MemberRef ref : info.referencedMembers) {
				if (!isChecked(ref.owner)) {
					continue;
				}
				checkedMembers++;
				ClassInfo target = read(loader, ref.owner, cache);
				if (target == null) {
					problems.add(own + " -> missing class " + ref.owner);
				} else if (!hasMember(loader, target, ref, cache, new HashSet<>())) {
					problems.add(own + " -> missing member " + ref.owner + "." + ref.name + ref.descriptor);
				}
			}
		}
		// Negative controls: the checker must notice the API of the previous Sophisticated Core line (what crashed in 1.0.0)
		ClassInfo guiManager = read(loader, "net/p3pp3rf1y/sophisticatedcore/client/gui/UpgradeGuiManager", cache);
		helper.assertTrue(guiManager != null && hasMember(loader, guiManager, new MemberRef(guiManager.name, "registerInventoryControl",
				"(Lnet/p3pp3rf1y/sophisticatedcore/common/gui/UpgradeContainerType;Lnet/p3pp3rf1y/sophisticatedcore/client/gui/UpgradeGuiManager$IUpgradeInventoryControlFactory;)V"),
				cache, new HashSet<>()), "positive control: registerInventoryControl exists");
		helper.assertTrue(read(loader, "net/p3pp3rf1y/sophisticatedcore/client/gui/UpgradeGuiManager$IUpgradeInventoryPartFactory", cache) == null,
				"negative control: the old IUpgradeInventoryPartFactory is detected as missing");
		helper.assertTrue(checkedMembers > 50,"the check actually inspected member references: " + checkedMembers);
		helper.assertTrue(problems.isEmpty(), problems.size() + " unresolved reference(s):\n  " + String.join("\n  ", problems));
		helper.succeed();
	}

	private static boolean isChecked(String internalName) {
		if (internalName.startsWith(OWN_PREFIX)) {
			return false;
		}
		for (String prefix : CHECKED_PREFIXES) {
			if (internalName.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}

	private static boolean hasMember(ClassLoader loader, ClassInfo cls, MemberRef ref, Map<String, ClassInfo> cache, Set<String> seen)
			throws IOException {
		if (!seen.add(cls.name)) {
			return false;
		}
		if (cls.members.contains(ref.name + ref.descriptor)) {
			return true;
		}
		List<String> parents = new ArrayList<>(cls.interfaces);
		if (cls.superName != null) {
			parents.add(cls.superName);
		}
		for (String parent : parents) {
			if (UNCHECKED_PARENT_PREFIXES.stream().anyMatch(parent::startsWith)) {
				// Platform parent (Minecraft, NeoForge, JDK): look the member up by reflection. Client-only parents cannot be
				// loaded on the dedicated test server; there the member is given the benefit of the doubt.
				try {
					if (platformHas(Class.forName(parent.replace('/', '.'), false, loader), ref)) {
						return true;
					}
				} catch (LinkageError | RuntimeException | ClassNotFoundException unloadable) {
					return true;
				}
				continue;
			}
			ClassInfo parentInfo = read(loader, parent, cache);
			if (parentInfo != null && hasMember(loader, parentInfo, ref, cache, seen)) {
				return true;
			}
		}
		return false;
	}

	private static boolean platformHas(Class<?> type, MemberRef ref) {
		for (Class<?> current = type; current != null; current = current.getSuperclass()) {
			for (java.lang.reflect.Method method : current.getDeclaredMethods()) {
				if (method.getName().equals(ref.name) && java.lang.invoke.MethodType.methodType(method.getReturnType(), method.getParameterTypes())
						.toMethodDescriptorString().equals(ref.descriptor)) {
					return true;
				}
			}
			for (java.lang.reflect.Field field : current.getDeclaredFields()) {
				if (field.getName().equals(ref.name) && field.getType().descriptorString().equals(ref.descriptor)) {
					return true;
				}
			}
			for (Class<?> iface : current.getInterfaces()) {
				if (platformHas(iface, ref)) {
					return true;
				}
			}
		}
		return false;
	}

	private static List<String> findOwnClasses() throws Exception {
		Path root = Path.of(BotaniaBackpackUpgrades.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		List<String> classes = new ArrayList<>();
		if (Files.isDirectory(root)) {
			try (Stream<Path> walk = Files.walk(root.resolve(OWN_PREFIX))) {
				walk.filter(p -> p.toString().endsWith(".class"))
						.forEach(p -> classes.add(root.relativize(p).toString().replace('\\', '/').replaceAll("\\.class$", "")));
			}
		} else {
			try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(root.toFile())) {
				zip.stream().map(java.util.zip.ZipEntry::getName).filter(n -> n.startsWith(OWN_PREFIX) && n.endsWith(".class"))
						.forEach(n -> classes.add(n.replaceAll("\\.class$", "")));
			}
		}
		return classes;
	}

	private static ClassInfo read(ClassLoader loader, String internalName, Map<String, ClassInfo> cache) throws IOException {
		if (cache.containsKey(internalName)) {
			return cache.get(internalName);
		}
		byte[] bytes = null;
		try (InputStream in = loader.getResourceAsStream(internalName + ".class")) {
			if (in != null) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				in.transferTo(out);
				bytes = out.toByteArray();
			}
		}
		ClassInfo info = bytes == null ? null : parse(bytes);
		cache.put(internalName, info);
		return info;
	}

	private record MemberRef(String owner, String name, String descriptor) {}

	private static final class ClassInfo {
		String name;
		String superName;
		final List<String> interfaces = new ArrayList<>();
		final Set<String> members = new HashSet<>();
		final Set<String> referencedClasses = new TreeSet<>();
		final List<MemberRef> referencedMembers = new ArrayList<>();
	}

	/** Minimal class file reader: constant pool, hierarchy and the declared fields/methods. */
	private static ClassInfo parse(byte[] bytes) throws IOException {
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
		in.readInt(); // magic
		in.readUnsignedShort();
		in.readUnsignedShort();
		int count = in.readUnsignedShort();
		Object[] constants = new Object[count];
		int[][] refs = new int[count][];
		int[] tags = new int[count];
		for (int i = 1; i < count; i++) {
			int tag = in.readUnsignedByte();
			tags[i] = tag;
			switch (tag) {
				case 1 -> constants[i] = in.readUTF();
				case 3, 4 -> in.readInt();
				case 5, 6 -> {
					in.readLong();
					i++;
				}
				case 7, 8, 16, 19, 20 -> refs[i] = new int[] {in.readUnsignedShort()};
				case 9, 10, 11, 12, 17, 18 -> refs[i] = new int[] {in.readUnsignedShort(), in.readUnsignedShort()};
				case 15 -> {
					in.readUnsignedByte();
					in.readUnsignedShort();
				}
				default -> throw new IOException("Unknown constant tag " + tag);
			}
		}
		ClassInfo info = new ClassInfo();
		in.readUnsignedShort(); // access flags
		info.name = className(constants, refs, in.readUnsignedShort());
		int superIndex = in.readUnsignedShort();
		info.superName = superIndex == 0 ? null : className(constants, refs, superIndex);
		int interfaceCount = in.readUnsignedShort();
		for (int i = 0; i < interfaceCount; i++) {
			info.interfaces.add(className(constants, refs, in.readUnsignedShort()));
		}
		for (int kind = 0; kind < 2; kind++) { // fields, then methods
			int members = in.readUnsignedShort();
			for (int m = 0; m < members; m++) {
				in.readUnsignedShort();
				String name = (String) constants[in.readUnsignedShort()];
				String descriptor = (String) constants[in.readUnsignedShort()];
				info.members.add(name + descriptor);
				int attributes = in.readUnsignedShort();
				for (int a = 0; a < attributes; a++) {
					in.readUnsignedShort();
					in.skipBytes(in.readInt());
				}
			}
		}
		for (int i = 1; i < count; i++) {
			if (tags[i] == 7) {
				String name = (String) constants[refs[i][0]];
				if (name.startsWith("[")) {
					Matcher matcher = DESCRIPTOR_CLASS.matcher(name);
					while (matcher.find()) {
						info.referencedClasses.add(matcher.group(1));
					}
				} else {
					info.referencedClasses.add(name);
				}
			} else if (tags[i] == 9 || tags[i] == 10 || tags[i] == 11) {
				String owner = className(constants, refs, refs[i][0]);
				int[] nameAndType = refs[refs[i][1]];
				if (!owner.startsWith("[")) {
					info.referencedMembers.add(new MemberRef(owner, (String) constants[nameAndType[0]], (String) constants[nameAndType[1]]));
				}
			}
		}
		return info;
	}

	private static String className(Object[] constants, int[][] refs, int classIndex) {
		return (String) constants[refs[classIndex][0]];
	}
}

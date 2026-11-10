package velrondevs.botaniabackpack.gametest;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import velrondevs.botaniabackpack.BotaniaBackpackUpgrades;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

@EventBusSubscriber(modid = BotaniaBackpackUpgrades.MOD_ID)
public final class GameTestRegistration {
	private static final List<Class<?>> TEST_CLASSES = List.of(
			BackpackPersistenceTests.class,
			BinaryCompatibilityTests.class,
			BotaniaNetworkIntegrationTests.class,
			IntegrationExtrasTests.class,
			ManaInfusionTests.class,
			ManaSupplyTests.class,
			ManaTankTests.class,
			ManaTransferTests.class,
			RecipeTests.class);

	private GameTestRegistration() {}

	@SubscribeEvent
	public static void registerFunctions(RegisterEvent event) {
		event.register(Registries.TEST_FUNCTION, helper -> {
			for (TestMethod test : findTests()) {
				Consumer<GameTestHelper> function = gameTestHelper -> invoke(test.method(), gameTestHelper);
				helper.register(test.id(), function);
			}
		});
	}

	@SubscribeEvent
	public static void registerTests(RegisterGameTestsEvent event) {
		Map<String, Holder<TestEnvironmentDefinition<?>>> environments = new HashMap<>();
		for (TestMethod test : findTests()) {
			GameTest annotation = test.annotation();
			Holder<TestEnvironmentDefinition<?>> environment = environments.computeIfAbsent(annotation.batch(),
					batch -> event.registerEnvironment(id("env_" + snake(batch))));
			TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData<>(environment, id(annotation.template()),
					annotation.timeoutTicks(), 0, true);
			event.registerTest(test.id(), new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, test.id()), data));
		}
	}

	private static List<TestMethod> findTests() {
		List<TestMethod> tests = new ArrayList<>();
		for (Class<?> testClass : TEST_CLASSES) {
			for (Method method : testClass.getDeclaredMethods()) {
				GameTest annotation = method.getAnnotation(GameTest.class);
				if (annotation != null && Modifier.isStatic(method.getModifiers())) {
					tests.add(new TestMethod(id(snake(testClass.getSimpleName()) + "/" + snake(method.getName())), method, annotation));
				}
			}
		}
		return tests;
	}

	private static void invoke(Method method, GameTestHelper helper) {
		try {
			method.setAccessible(true);
			method.invoke(null, helper);
		} catch (InvocationTargetException exception) {
			Throwable cause = exception.getCause();
			if (cause instanceof RuntimeException runtime) {
				throw runtime;
			}
			if (cause instanceof Error error) {
				throw error;
			}
			throw new IllegalStateException(cause);
		} catch (IllegalAccessException exception) {
			throw new IllegalStateException(exception);
		}
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(BotaniaBackpackUpgrades.MOD_ID, path);
	}

	private static String snake(String name) {
		return name.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
	}

	private record TestMethod(Identifier id, Method method, GameTest annotation) {}
}

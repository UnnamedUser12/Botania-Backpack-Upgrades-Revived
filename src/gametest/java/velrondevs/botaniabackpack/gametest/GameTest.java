package velrondevs.botaniabackpack.gametest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GameTest {
	String template() default "empty";

	String batch() default "default";

	int timeoutTicks() default 100;
}

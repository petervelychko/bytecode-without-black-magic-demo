package demo.transform;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class TransformedCalculatorLoader {

    static void main() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException,
            InstantiationException {
        byte[] bytes = Files.readAllBytes(Path.of("target/classes/demo/target/Calculator.class"));
        Class<?> transformedClass = new TransformedCalculatorLoader.ByteArrayClassLoader().
                define("demo.target.Calculator", bytes);
        Object newCalculator = Arrays.stream(transformedClass.getDeclaredConstructors()).findFirst().orElseThrow()
                .newInstance();
        Object result = transformedClass.getMethod("add", int.class, int.class).invoke(newCalculator, 2, 3);
        System.out.println("New Calculator.answer() returned " + result);
    }

    public static final class ByteArrayClassLoader extends ClassLoader {
        public ByteArrayClassLoader() {
            super(null);
        }

        public Class<?> define(String className, byte[] code) {
            return defineClass(className, code, 0, code.length);
        }
    }
}

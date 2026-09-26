package demo.generate;

import java.lang.classfile.ClassFile;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.constant.ConstantDescs.CD_int;

public final class GenerateDemo {

    private static final String GENERATED_NAME = "demo.generated.Generated";
    private static final ClassDesc CD_GENERATED = ClassDesc.of(GENERATED_NAME);
    private static final MethodTypeDesc MTD_INT = MethodTypeDesc.of(CD_int);

    private static final Path OUTPUT = Path.of("target/classes/demo/generated/Generated.class");

    private GenerateDemo() {
    }

    public static void main(String[] args) throws Exception {
        var classFile = ClassFile.of();

        byte[] bytes = classFile.build(
                CD_GENERATED,
                classBuilder -> classBuilder
                        .withVersion(ClassFile.JAVA_25_VERSION, 0)
                        .withFlags(AccessFlag.PUBLIC, AccessFlag.SUPER)
                        .withMethodBody(
                                "answer",
                                MTD_INT,
                                ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                                code -> code
                                        .loadConstant(42)
                                        .ireturn()
                        )
        );

        var verificationErrors = classFile.verify(bytes);
        if (!verificationErrors.isEmpty()) {
            throw new IllegalStateException("Generated class did not verify: " + verificationErrors);
        }

        Files.createDirectories(OUTPUT.getParent());
        Files.write(OUTPUT, bytes);

        Class<?> generated = new ByteArrayClassLoader().define(GENERATED_NAME, bytes);
        Object answer = generated.getMethod("answer").invoke(null);

        System.out.println("Class-file API: generate");
        System.out.println("========================");
        System.out.println("Wrote:    " + OUTPUT);
        System.out.println("Bytes:    " + bytes.length);
        System.out.println("Verify:   OK");
        System.out.println("answer(): " + answer);
        System.out.println("\nTry:");
        System.out.println("  javap -c -p " + OUTPUT);
    }

    private static final class ByteArrayClassLoader extends ClassLoader {
        private ByteArrayClassLoader() {
            super(null);
        }

        private Class<?> define(String binaryName, byte[] bytes) {
            return defineClass(binaryName, bytes, 0, bytes.length);
        }
    }
}

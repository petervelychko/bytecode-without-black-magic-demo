package demo.transform;

import java.lang.classfile.*;
import java.lang.classfile.instruction.ConstantInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.constant.ConstantDescs.CD_String;
import static java.lang.constant.ConstantDescs.CD_void;

public class TransformDemoSimple {

    private static final Path CALCULATOR_PATH = Path.of("target/classes/demo/target/Calculator.class");

    private static final ClassDesc CD_SYSTEM = ClassDesc.of("java.lang.System");

    private static final ClassDesc CD_PRINT_STREAM = ClassDesc.of("java.io.PrintStream");

    private static final MethodTypeDesc PRINTLN_STRING = MethodTypeDesc.of(CD_void, CD_String);

    private TransformDemoSimple() {
    }

    static void main() throws Exception {
        ClassFile classFile = ClassFile.of();

        ClassModel original = classFile.parse(CALCULATOR_PATH);

        /*
         * ---------------------------------------------------------
         * Transformation
         * Change:
         *     return 42;
         * into:
         *     return 43;
         * ---------------------------------------------------------
         */
        CodeTransform replace42With43 = (builder, element) -> {

            if (element instanceof ConstantInstruction constant && Integer.valueOf(42).equals(constant.constantValue())) {
                // Drop the original constant instruction and emit a new one.
                builder.loadConstant(43);
            } else {
                // Preserve every other code element.
                builder.with(element);
            }
        };

        /*
         * ---------------------------------------------------------
         * Class transformation
         *
         * For each MethodModel:
         *
         * answer() -> replace 42 with 43
         *
         * Everything else is preserved unchanged.
         * ---------------------------------------------------------
         */
        ClassTransform transform = (builder, element) -> {

            if (element instanceof MethodModel method) {
                if (method.methodName().equalsString("answer")) {
                    builder.transformMethod(method, MethodTransform.transformingCode(replace42With43));

                } else {
                    /*
                     * Preserve methods that we don't want to transform.
                     */
                    builder.with(element);
                }

            } else {

                /*
                 * Preserve fields, attributes, interfaces etc.
                 */
                builder.with(element);
            }
        };

        /*
         * ---------------------------------------------------------
         * Apply transformation
         * ---------------------------------------------------------
         */
        byte[] transformedBytes = classFile.transformClass(original, transform);

        /*
         * Verify the generated class before replacing Calculator.class.
         */
        var verificationErrors = classFile.verify(transformedBytes);

        if (!verificationErrors.isEmpty()) {
            throw new IllegalStateException("Transformed class did not verify: " + verificationErrors);
        }

        /*
         * Replace the compiled Calculator.class.
         */
        Files.write(CALCULATOR_PATH, transformedBytes);

        System.out.println("Class-File API: transform");
        System.out.println("=========================");
        System.out.println("Replaced: " + CALCULATOR_PATH);

        System.out.println("Verify:   OK");
        System.out.println();
        System.out.println("Inspect it with:");
        System.out.println("  javap -c -p " + CALCULATOR_PATH);
        System.out.println();
        System.out.println("Re-run:");
        System.out.println("  mvn clean compile");
        System.out.println("before running this transform again.");
    }
}
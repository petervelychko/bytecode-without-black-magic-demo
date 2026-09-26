package demo.transform;

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.CodeElement;
import java.lang.classfile.CodeTransform;
import java.lang.classfile.Instruction;
import java.lang.classfile.MethodModel;
import java.lang.classfile.MethodTransform;
import java.lang.classfile.Opcode;
import java.lang.classfile.instruction.ConstantInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.constant.ConstantDescs.CD_String;
import static java.lang.constant.ConstantDescs.CD_void;

public final class TransformDemo {

    private static final Path CALCULATOR_PATH = Path.of("target/classes/demo/target/Calculator.class");

    private static final ClassDesc CD_SYSTEM = ClassDesc.of("java.lang.System");

    private static final ClassDesc CD_PRINT_STREAM = ClassDesc.of("java.io.PrintStream");

    private static final MethodTypeDesc PRINTLN_STRING = MethodTypeDesc.of(CD_void, CD_String);

    private TransformDemo() {
    }

    static void main() throws Exception {
        ClassFile classFile = ClassFile.of();

        ClassModel original = classFile.parse(CALCULATOR_PATH);

        /*
         * ---------------------------------------------------------
         * Transformation #1
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
         * Transformation #2
         *
         * Instrument:
         *     add(int a, int b)
         * so it behaves conceptually like:
         *     System.out.println("[enter] add()");
         *
         *     ... original method ...
         *
         *     System.out.println("[exit] add()");
         *     return result;
         * ---------------------------------------------------------
         */
        CodeTransform instrumentAdd = new CodeTransform() {

            @Override
            public void atStart(CodeBuilder builder) {
                emitPrintln(builder, "[enter] add()");
            }

            @Override
            public void accept(CodeBuilder builder, CodeElement element) {

                /*
                 * Insert the exit message immediately
                 * before the original IRETURN.
                 */
                if (element instanceof Instruction instruction && instruction.opcode() == Opcode.IRETURN) {
                    emitPrintln(builder, "[exit] add()");
                }

                /*
                 * Preserve the original element.
                 * This also preserves the original IRETURN.
                 */
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
         * add()    -> add entry/exit logging
         *
         * Everything else is preserved unchanged.
         * ---------------------------------------------------------
         */
        ClassTransform transform = (builder, element) -> {

            if (element instanceof MethodModel method) {
                if (method.methodName().equalsString("answer")) {
                    builder.transformMethod(method, MethodTransform.transformingCode(replace42With43));

                } else if (method.methodName().equalsString("add")) {
                    builder.transformMethod(method, MethodTransform.transformingCode(instrumentAdd));

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

    /**
     * Emits:
     * <p>
     * System.out.println(text);
     */
    private static void emitPrintln(CodeBuilder builder, String text) {
        builder.getstatic(CD_SYSTEM, "out", CD_PRINT_STREAM)
                .ldc(builder.constantPool().stringEntry(text))
                .invokevirtual(CD_PRINT_STREAM, "println", PRINTLN_STRING);
    }
}

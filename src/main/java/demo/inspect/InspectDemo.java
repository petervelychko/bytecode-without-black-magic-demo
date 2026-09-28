package demo.inspect;

import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.Instruction;
import java.lang.classfile.MethodModel;
import java.lang.classfile.instruction.ConstantInstruction;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.classfile.instruction.LoadInstruction;
import java.lang.classfile.instruction.StoreInstruction;

public final class InspectDemo {

    private static final String CALCULATOR_RESOURCE = "demo/target/Calculator.class";

    private InspectDemo() {
    }

    public static void main(String[] args) throws Exception {
        var classFile = ClassFile.of();
        ClassModel model = classFile.parse(readCalculatorBytes());

        System.out.println("Class-file API: inspect");
        System.out.println("=======================");
        System.out.println("Input:       " + CALCULATOR_RESOURCE + " (from runtime classpath)");
        System.out.println("Class:       " + model.thisClass().asInternalName());
        System.out.println("Version:     " + model.majorVersion() + "." + model.minorVersion());
        System.out.println("Superclass:  " + model.superclass()
                .map(entry -> entry.asInternalName())
                .orElse("<none>"));

        System.out.println();
        System.out.println("Methods:");
        for (MethodModel method : model.methods()) {
            System.out.printf("  %-12s %s%n",
                    method.methodName().stringValue(),
                    method.methodType().stringValue());
        }

        MethodModel add = model.methods().stream()
                .filter(method -> method.methodName().equalsString("add"))
                .findFirst()
                .orElseThrow();

        System.out.println();
        System.out.println("Instructions in add(int, int):");
        add.code().orElseThrow().forEach(element -> {
            if (element instanceof Instruction instruction) {
                System.out.println("  " + describe(instruction));
            }
        });
    }

    private static byte[] readCalculatorBytes() throws IOException {
        var classLoader = InspectDemo.class.getClassLoader();
        try (var input = classLoader.getResourceAsStream(CALCULATOR_RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException(
                        "Could not find " + CALCULATOR_RESOURCE + " on the runtime classpath");
            }
            return input.readAllBytes();
        }
    }

    private static String describe(Instruction instruction) {
        if (instruction instanceof LoadInstruction load) {
            return "%s local[%d]".formatted(instruction.opcode(), load.slot());
        }
        if (instruction instanceof StoreInstruction store) {
            return "%s local[%d]".formatted(instruction.opcode(), store.slot());
        }
        if (instruction instanceof ConstantInstruction constant) {
            return "%s %s".formatted(instruction.opcode(), constant.constantValue());
        }
        if (instruction instanceof InvokeInstruction invoke) {
            return "%s %s.%s%s".formatted(
                    instruction.opcode(),
                    invoke.owner().asInternalName(),
                    invoke.name().stringValue(),
                    invoke.type().stringValue());
        }
        return instruction.opcode().toString();
    }
}

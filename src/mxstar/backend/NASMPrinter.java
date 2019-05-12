package mxstar.backend;

import mxstar.ir.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import static mxstar.ir.IRBinaryOp.Ops.*;
import static mxstar.utility.GlobalSymbols.REG_SIZE;

public class NASMPrinter implements IRVisitor {
    private PrintStream out;
    private Map<String, Integer> idCounter = new HashMap<>();
    private Map<Object, String> idMap = new HashMap<>();
    private PhysicalReg tempPhsiycalReg;

    public NASMPrinter(PrintStream out) {
        this.out = out;
    }

    private boolean isBssSection, isDataSection;

    private String newId(String id) {
        int nowCnt = idCounter.getOrDefault(id, 0) + 1;
        idCounter.put(id, nowCnt);
        return id + "_" + nowCnt;
    }

    private String dataId(StaticData data) {
        String id = idMap.get(data);
        if (id == null) {
            id = "__static_data_" + newId(data.getName());
            idMap.put(data, id);
        }
        return id;
    }

    private String bbId(BasicBlock bb) {
        String id = idMap.get(bb);
        if (id == null) {
            id = "__block_" + newId(bb.getName());
            idMap.put(bb, id);
        }
        return id;
    }

    @Override
    public void visit(IRRoot node) {
        tempPhsiycalReg = node.getReversedRegister();

        idMap.put(node.getFunction("main").getBeginBB(), "main");

        out.println("\t\tglobal\tmain");
        out.println();

        out.println("\t\textern\tmalloc");
        out.println();

        if (node.getStaticDataList().size() > 0) {
            isBssSection = true;
            out.println("\t\tsection\t.bss");
            for (StaticData staticData : node.getStaticDataList()) {
                staticData.accept(this);
            }
            out.println();
            isBssSection = false;
        }

        if (node.staticStrMap.size() > 0) {
            isDataSection = true;
            out.println("\t\tsection\t.data");
            for (StaticStr staticString : node.staticStrMap.values()) {
                staticString.accept(this);
            }
            out.println();
            isDataSection = false;
        }

        out.println("\t\tsection\t.text\n");
        for (IRFunction irFunction : node.getFunctionList()) {
            irFunction.accept(this);
        }
        out.println();

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("lib/builtin_functions.asm"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                out.println(line);
            }
        } catch (IOException e) {
            throw new Error("IO exception when reading builtin functions from file");
        }
    }

    @Override
    public void visit(IRFunction node) {
        out.printf("# function %s\n\n", node.getName());

        for (BasicBlock bb : node.getAllBB()) {
            bb.accept(this);
        }
    }

    @Override
    public void visit(BasicBlock node) {
        out.printf("%s:\n", bbId(node));
        for (IRInstruction inst = node.getHeadInst(); inst != null; inst = inst.getNextInst()) {
            inst.accept(this);
        }
        out.println();
    }

    @Override
    public void visit(IRBranch node) {
        if (node.getCond() instanceof IntImm) {
            int boolValue = ((IntImm) node.getCond()).getValue();
            out.printf("\t\tjmp\t\t%s\n", boolValue == 1 ? bbId(node.getThenBB()) : bbId(node.getElseBB()));
            return;
        }
        out.print("\t\tcmp\t\t");
        node.getCond().accept(this);
        out.println(", 0");
        out.printf("\t\tje\t\t%s\n", bbId(node.getElseBB()));
        if(node.getThenBB().getIdx() != node.getParentBB().getIdx() + 1){
            out.printf("\t\tjmp\t\t%s\n", bbId(node.getThenBB()));
        }
    }

    @Override
    public void visit(IRJump node) {
        if(node.getTargetBB().getIdx() != node.getParentBB().getIdx() + 1) {
            out.printf("\t\tjmp\t\t%s\n", bbId(node.getTargetBB()));
        }
    }

    @Override
    public void visit(IRReturn node) {
        out.println("\t\tret");
    }

    @Override
    public void visit(IRUnaryOp node) {
        String op = "";
        switch (node.getOp()) {
            case BIT_NOT:
                op = "not";
                break;
            case NEG:
                op = "neg";
                break;
            default:
                assert false;
        }
        if(!((node.getDest() instanceof PhysicalReg && node.getRhs() instanceof PhysicalReg)
             && ((PhysicalReg) node.getDest()).getName().equals(((PhysicalReg) node.getRhs()).getName()))) {
                out.print("\t\tmov\t\t");
                node.getDest().accept(this);
                out.print(", ");
                node.getRhs().accept(this);
                out.println();
        }

        out.print("\t\t" + op + "\t\t");
        node.getDest().accept(this);
        out.println();
    }

    @Override
    public void visit(IRBinaryOp node) {
        if (node.getOp() == DIV || node.getOp() == MOD) {
            if(!(node.getRhs() instanceof PhysicalReg && ((PhysicalReg) node.getRhs()).getName().equals("rbx"))) {
                out.print("\t\tmov\t\trbx, ");
                node.getRhs().accept(this);
                out.println();
            }

            if(!(node.getLhs() instanceof PhysicalReg && ((PhysicalReg) node.getLhs()).getName().equals("rax"))) {
                out.print("\t\tmov\t\trax, ");
                node.getLhs().accept(this);
                out.println();
            }

            out.println("\t\tmov\t\t" + tempPhsiycalReg.getName() + ", rdx");

            out.println("\t\tcqo");
            out.println("\t\tidiv\trbx");
            if (node.getOp() == DIV) {
                if(!(node.getDest() instanceof PhysicalReg && ((PhysicalReg) node.getDest()).getName().equals("rax"))) {
                    out.print("\t\tmov\t\t");
                    node.getDest().accept(this);
                    out.println(", rax");
                }
            } else {
                if(!(node.getDest() instanceof PhysicalReg && ((PhysicalReg) node.getDest()).getName().equals("rdx"))) {
                    out.print("\t\tmov\t\t");
                    node.getDest().accept(this);
                    out.println(", rdx");
                }
            }

            out.println("\t\tmov\t\trdx, " + tempPhsiycalReg.getName());

        } else if (node.getOp() == SHL ||
                node.getOp() == SHR) {

            if(node.getRhs() instanceof IntImm) {
                if (node.getOp() == SHL) {
                    out.print("\t\tsal\t\t");
                } else {
                    out.print("\t\tsar\t\t");
                }
                node.getLhs().accept(this);
                out.print(", ");
                node.getRhs().accept(this);
                out.println();
            }
            else {
                out.println("\t\tmov\t\trbx, rcx");
                out.print("\t\tmov\t\trcx, ");
                node.getRhs().accept(this);
                if (node.getOp() == SHL) {
                    out.print("\n\t\tsal\t\t");
                } else {
                    out.print("\n\t\tsar\t\t");
                }
                node.getLhs().accept(this);
                out.println(", cl");
                out.println("\t\tmov\t\trcx, rbx");
            }
        } else {
            String op = "";
            switch (node.getOp()) {
                case ADD:
                    if (node.getRhs() instanceof IntImm && ((IntImm) node.getRhs()).getValue() == 1) {
                        out.print("\t\tinc\t\t");
                        node.getLhs().accept(this);
                        out.println();
                        return;
                    }
                    op = "add\t";
                    break;
                case SUB:
                    if (node.getRhs() instanceof IntImm && ((IntImm) node.getRhs()).getValue() == 1) {
                        out.print("\t\tdec\t\t");
                        node.getLhs().accept(this);
                        out.println();
                        return;
                    }
                    op = "sub\t";
                    break;
                case MUL:
                    if (node.getRhs() instanceof IntImm && ((IntImm) node.getRhs()).getValue() == 1) {
                        return;
                    }
                    op = "imul";
                    break;
                case BIT_OR:
                    op = "or\t";
                    break;
                case BIT_XOR:
                    op = "xor\t";
                    break;
                case BIT_AND:
                    op = "and\t";
                    break;
                default:
                    assert false;
            }
            out.print("\t\t" + op + "\t");
            node.getLhs().accept(this);
            out.print(", ");
            node.getRhs().accept(this);
            out.println();
        }
    }

    @Override
    public void visit(IRComparison node) {
        if (node.getLhs() instanceof PhysicalReg) {
            out.print("\t\tand\t\t");
            node.getLhs().accept(this);
            out.println(", -1");
        }
        if (node.getRhs() instanceof PhysicalReg) {
            out.print("\t\tand\t\t");
            node.getRhs().accept(this);
            out.println(", -1");
        }
        out.println("\t\txor\t\trax, rax");
        out.print("\t\tcmp\t\t");
        node.getLhs().accept(this);
        out.print(", ");
        node.getRhs().accept(this);
        out.println();
        String op = "";
        switch (node.getOp()) {
            case EQ:
                op = "sete";
                break;
            case NEQ:
                op = "setne";
                break;
            case LT:
                op = "setl";
                break;
            case LEQ:
                op = "setle";
                break;
            case GT:
                op = "setg";
                break;
            case GEQ:
                op = "setge";
                break;
            default:
                assert false;
        }
        out.println("\t\t" + op + "\tal");
        out.print("\t\tmov\t\t");
        node.getDest().accept(this);
        out.println(", rax");
    }

    @Override
    public void visit(IRMove node) {
        out.print("\t\tmov\t\t");
        node.getDest().accept(this);
        out.print(", ");
        node.getSrc().accept(this);
        out.println();
    }

    private String sizeStr(int memSize) {
        String sizeStr = "";
        switch (memSize) {
            case 1:
                sizeStr = "byte";
                break;
            case 2:
                sizeStr = "word";
                break;
            case 4:
                sizeStr = "dword";
                break;
            case 8:
                sizeStr = "qword";
                break;
            default:
                assert false;
        }
        return sizeStr;
    }

    @Override
    public void visit(IRLoad node) {
        if (node.getAddr() instanceof StaticStr) {
            out.print("\t\tmov\t\t");
            node.getDest().accept(this);
            out.print(", " + sizeStr(REG_SIZE) + " ");
            node.getAddr().accept(this);
            out.println();
            return;
        }
        out.print("\t\tmov\t\t");
        node.getDest().accept(this);
        out.print(", " + sizeStr(REG_SIZE) + " [");
        node.getAddr().accept(this);
        if (node.getOffset() < 0) {
            out.print(node.getOffset());
        } else if (node.getOffset() > 0) {
            out.print("+" + node.getOffset());
        }
        out.println("]");
    }

    @Override
    public void visit(IRStore node) {
        if (node.getAddr() instanceof StaticStr) {
            out.print("\t\tmov\t\t" + sizeStr(REG_SIZE) + " ");
            node.getAddr().accept(this);
            out.print(" ");
            node.getValue().accept(this);
            out.println();
            return;
        }
        out.print("\t\tmov\t\t" + sizeStr(REG_SIZE) + " [");
        node.getAddr().accept(this);
        if (node.getOffset() < 0) {
            out.print(node.getOffset());
        } else if (node.getOffset() > 0) {
            out.print("+" + node.getOffset());
        }
        out.print("], ");
        node.getValue().accept(this);
        out.println();
    }

    @Override
    public void visit(IRFunctionCall node) {
        if (node.getFunc().isBuiltIn()) out.println("\t\tcall\t" + getBuiltInLabel(node.getFunc().getName()));
        else out.println("\t\tcall\t" + bbId(node.getFunc().getBeginBB()));
    }

    @Override
    public void visit(IRHeapAlloc node) {
        out.println("\t\tcall\tmalloc");
    }

    @Override
    public void visit(IRPush node) {
        out.print("\t\tpush\t");
        node.getValue().accept(this);
        out.println();
    }

    @Override
    public void visit(IRPop node) {
        out.print("\t\tpop\t\t");
        node.getPhysicalReg().accept(this);
        out.println();
    }

    @Override
    public void visit(VirtualReg node) {
        assert false;
    }

    @Override
    public void visit(PhysicalReg node) {
        out.print(node.getName());
    }

    @Override
    public void visit(IntImm node) {
        out.print(node.getValue());
    }

    @Override
    public void visit(StaticVar node) {
        if (isBssSection) {
            String op = "";
            switch (node.getSize()) {
                case 1: op = "resb"; break;
                case 2: op = "resw"; break;
                case 4: op = "resd"; break;
                case 8: op = "resq"; break;
                default: assert false;
            }
            out.printf("%s:\t%s\t1\n", dataId(node), op);
        }
        else out.print(dataId(node));
    }

    private String staticStrDataSection(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, n = str.length(); i < n; ++i) {
            char c = str.charAt(i);
            sb.append((int) c);
            sb.append(", ");
        }
        sb.append(0);
        return sb.toString();
    }

    @Override
    public void visit(StaticStr node) {
        if (isDataSection) {
            out.printf("%s:\n", dataId(node));
            out.printf("\t\tdq\t\t%d\n", node.getValue().length());
            out.printf("\t\tdb\t\t%s\n", staticStrDataSection(node.getValue()));
        } else {
            out.print(dataId(node));
        }
    }

    private String getBuiltInLabel(String str) {
        switch (str) {
            case "__string_concat":
                return "__builtin_string_concat";
            case "__string_eq" :
                return "__builtin_string_equal";
            case "__string_neq" :
                return "__builtin_string_inequal";
            case "__string_lt":
                return "__builtin_string_less";
            case "__string_leq":
                return "__builtin_string_less_equal";
            case "print" :
                return "_Z5printPc";
            case "println":
                return "_Z7printlnPc";
            case "getString":
                return "_Z9getStringv";
            case "getInt":
                return "_Z6getIntv";
            case "toString":
                return "_Z8toStringi";
            case "__string_class__substring":
                return "_Z27__member___string_substringPcii";
            case "__string_class__parseInt":
                return "_Z26__member___string_parseIntPc";
            case "__string_class__ord":
                return "_Z21__member___string_ordPci";
            default:
                return null;
        }
    }

    @Override
    public void visit(StackSlot node) {
        throw new Error("wtf");
    }
}

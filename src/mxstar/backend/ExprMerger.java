package mxstar.backend;

import mxstar.frontend.IRPrinter;
import mxstar.ir.*;

import java.util.HashSet;
import java.util.Set;


public class ExprMerger {
    private IRRoot ir;
    private static final int STEP_LIM = 20;

    public ExprMerger(IRRoot ir) {
        this.ir = ir;
    }

    private boolean checkSame(RegValue lhs,RegValue rhs) {

        if(lhs instanceof IntImm && rhs instanceof IntImm) {
            return ((IntImm) lhs).getValue() == ((IntImm) rhs).getValue();
        }
        return lhs == rhs;
    }
    public void run() {
        for(IRFunction irFunction : ir.getFunctionList()) {
            for(BasicBlock bb : irFunction.getAllBB()) {
                for (IRInstruction inst = bb.getHeadInst(), nextInst;
                     inst != null;
                     inst = nextInst) {
                    nextInst = inst.getNextInst();

                    IRInstruction knownInst = inst.getPrevInst();

                    if (inst instanceof IRBinaryOp && ((IRBinaryOp) inst).getOp() == IRBinaryOp.Ops.ADD) {

                        if (((IRBinaryOp) inst).getDest() instanceof StaticData
                                || ((IRBinaryOp) inst).getLhs() instanceof StaticData
                                || ((IRBinaryOp) inst).getRhs() instanceof StaticData) continue;

                        Set<IRRegister> changedRegs = new HashSet<>();
                        Set<IRRegister> usedRegs = new HashSet<>();

                        for (int i = 0; knownInst != null && i < STEP_LIM; i++) {
                            if (knownInst instanceof IRBinaryOp
                                    && ((IRBinaryOp) knownInst).getOp() == IRBinaryOp.Ops.ADD
                                    && checkSame(((IRBinaryOp) knownInst).getLhs(), ((IRBinaryOp) inst).getLhs())
                                    && checkSame(((IRBinaryOp) knownInst).getRhs(), ((IRBinaryOp) inst).getRhs())
                                    && !changedRegs.contains(knownInst.getDefinedReg())
                            ) {
                                RegValue lhs = ((IRBinaryOp) knownInst).getLhs();
                                RegValue rhs = ((IRBinaryOp) knownInst).getRhs();
                                if (lhs instanceof IRRegister && changedRegs.contains(lhs)) continue;
                                if (rhs instanceof IRRegister && changedRegs.contains(rhs)) continue;

                                if (((IRBinaryOp) knownInst).getDest() instanceof StaticData) continue;
                                if (checkSame(lhs, ((IRBinaryOp) knownInst).getDest())) continue;
                                if (checkSame(rhs, ((IRBinaryOp) knownInst).getDest())) continue;

                                IRMove rInst = new IRMove(((IRBinaryOp) inst).getDest(), knownInst.getDefinedReg(), bb);

                                if (changedRegs.contains(inst.getDefinedReg()) || usedRegs.contains(inst.getDefinedReg())) {
                                    bb.insertInst(inst, rInst);
                                } else {
                                    bb.insertInst(knownInst, rInst);
                                }
                                bb.removeInst(inst);
                                break;
                            }

                            if (knownInst.getDefinedReg() != null)
                                changedRegs.add(knownInst.getDefinedReg());

                            if (knownInst.getUsedRegisterList() != null)
                                usedRegs.addAll(knownInst.getUsedRegisterList());


                            knownInst = knownInst.getPrevInst();
                        }
                    }
                }
            }

            for(BasicBlock bb : irFunction.getAllBB()) {
                for(IRInstruction inst = bb.getHeadInst(), nextInst;
                    inst != null;
                    inst = nextInst) {
                    nextInst = inst.getNextInst();

                    IRInstruction knownInst = inst.getPrevInst();

                    if(inst instanceof IRMove) {

                        if(((IRMove) inst).getSrc() instanceof StaticData
                                || ((IRMove) inst).getDest() instanceof StaticData) continue;

                        Set<IRRegister> changedRegs = new HashSet<>();
                        Set<IRRegister> usedRegs = new HashSet<>();

                        for(int i = 0; knownInst != null && i < STEP_LIM; i++) {
                            if(knownInst instanceof IRMove
                                    && checkSame(((IRMove) knownInst).getDest(), ((IRMove) inst).getSrc())
                                    && !changedRegs.contains(knownInst.getDefinedReg())
                            ) {
                                IRMove rInst = new IRMove(((IRMove) inst).getDest(), knownInst.getDefinedReg(), bb);

                                if(changedRegs.contains(inst.getDefinedReg()) || usedRegs.contains(inst.getDefinedReg())) {
                                    bb.insertInst(inst, rInst);
                                }
                                else {
                                    bb.insertInst(knownInst, rInst);
                                }
                                bb.removeInst(inst);
                                break;
                            }

                            if(knownInst.getDefinedReg() != null)
                                changedRegs.add(knownInst.getDefinedReg());

                            if(knownInst.getUsedRegisterList() != null)
                                usedRegs.addAll(knownInst.getUsedRegisterList());

                            knownInst = knownInst.getPrevInst();
                        }
                    }
                }
            }
        }
    }
}

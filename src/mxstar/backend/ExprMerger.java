package mxstar.backend;

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
                for(IRInstruction inst = bb.getHeadInst(), nextInst;
                    inst != null;
                    inst = nextInst) {
                    nextInst = inst.getNextInst();

                    IRInstruction knownInst = inst.getPrevInst();

                    if(inst instanceof IRBinaryOp) {
                        Set<IRRegister> changedReg = new HashSet<>();

                        for(int i = 0; knownInst != null && i < STEP_LIM; i++) {
                            if(knownInst instanceof IRBinaryOp
                                && checkSame(((IRBinaryOp) knownInst).getLhs(), ((IRBinaryOp) inst).getLhs())
                                && checkSame(((IRBinaryOp) knownInst).getRhs(), ((IRBinaryOp) inst).getRhs())
                                && !changedReg.contains(knownInst.getDefinedReg())
                            ) {
                                RegValue lhs = ((IRBinaryOp) knownInst).getLhs();
                                RegValue rhs = ((IRBinaryOp) knownInst).getRhs();
                                if(lhs instanceof IRRegister && changedReg.contains(lhs)) continue;
                                if(rhs instanceof IRRegister && changedReg.contains(rhs)) continue;

                                IRMove rInst = new IRMove(((IRBinaryOp) inst).getDest(), knownInst.getDefinedReg(), bb);

                                if(changedReg.contains(inst.getDefinedReg())) {
                                    bb.insertInst(inst, rInst);
                                }
                                else {
                                    bb.insertInst(knownInst, rInst);
                                }
                                bb.removeInst(inst);
                                break;
                            }

                            if(knownInst.getDefinedReg() != null)
                                changedReg.add(knownInst.getDefinedReg());

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
                        Set<IRRegister> changedReg = new HashSet<>();

                        for(int i = 0; knownInst != null && i < STEP_LIM; i++) {
                            if(knownInst instanceof IRMove
                                    && checkSame(((IRMove) knownInst).getDest(), ((IRMove) inst).getSrc())
                                    && !changedReg.contains(knownInst.getDefinedReg())
                            ) {
                                IRMove rInst = new IRMove(((IRMove) inst).getDest(), knownInst.getDefinedReg(), bb);

                                if(changedReg.contains(inst.getDefinedReg())) {
                                    bb.insertInst(inst, rInst);
                                }
                                else {
                                    bb.insertInst(knownInst, rInst);
                                }
                                bb.removeInst(inst);
                                break;
                            }

                            if(knownInst.getDefinedReg() != null)
                                changedReg.add(knownInst.getDefinedReg());

                            knownInst = knownInst.getPrevInst();
                        }
                    }
                }
            }
        }
    }
}

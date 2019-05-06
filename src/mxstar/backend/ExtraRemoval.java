package mxstar.backend;

import mxstar.ir.*;

public class ExtraRemoval {
    private IRRoot ir;

    public ExtraRemoval(IRRoot ir) {
        this.ir = ir;
    }

    public void run() {
        for (IRFunction func : ir.getFunctionList()) {
            for (BasicBlock bb : func.getAllBB()) {
                for (IRInstruction inst = bb.getHeadInst(), lastInst = null; inst != null; inst = inst.getNextInst()) {
                    boolean remove = false;
                    if (inst instanceof IRMove) {
                        IRMove moveInst = (IRMove) inst;
                        if (moveInst.getDest() == moveInst.getSrc()) remove = true;
                        else if (lastInst instanceof IRMove &&
                                moveInst.getDest() == ((IRMove) lastInst).getSrc() &&
                                moveInst.getSrc() == ((IRMove) lastInst).getDest()) remove = true;
                    } else if (inst instanceof IRLoad) {
                        if (lastInst instanceof IRStore &&
                                ((IRStore) lastInst).getValue() == ((IRLoad) inst).getDest() &&
                                ((IRStore) lastInst).getAddr() == ((IRLoad) inst).getAddr() &&
                                ((IRStore) lastInst).getOffset() == ((IRLoad) inst).getOffset()) remove = true;
                    } else if (inst instanceof IRStore) {
                        if (lastInst instanceof IRLoad &&
                                ((IRLoad) lastInst).getDest() == ((IRStore) inst).getValue() &&
                                ((IRLoad) lastInst).getAddr() == ((IRStore) inst).getAddr() &&
                                ((IRLoad) lastInst).getOffset() == ((IRStore) inst).getOffset()) remove = true;
                    }
                    if (remove) inst.remove();
                    else lastInst = inst;
                }
            }
        }
    }
}
package mxstar.ir;

import java.util.Map;

public class IRPop extends IRInstruction {
    private PhysicalReg physicalReg;

    public IRPop(BasicBlock parentBB, PhysicalReg physicalReg) {
        super(parentBB);
        this.physicalReg = physicalReg;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public IRInstruction copyRename(Map<Object, Object> renameMap) {
        return null;
    }

    @Override
    public void reloadRegLists() {

    }

    @Override
    public IRRegister getDefinedReg() {
        return null;
    }

    @Override
    public void setUsedRegisterList(Map<IRRegister, IRRegister> renameMap) {

    }

    @Override
    public void setDefinedRegister(IRRegister vreg) {
        // no actions
    }

    public PhysicalReg getPhysicalReg() {
        return physicalReg;
    }
}

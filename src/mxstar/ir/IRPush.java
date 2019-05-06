package mxstar.ir;

import java.util.Map;

public class IRPush extends IRInstruction{
    private RegValue value;

    public IRPush(RegValue value, BasicBlock bb) {
        super(bb);
        this.value = value;
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

    public RegValue getValue() {
        return value;
    }
}

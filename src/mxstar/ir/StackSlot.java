package mxstar.ir;

public class StackSlot extends IRRegister {
    private String name;

    public StackSlot(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void accept(IRVisitor visitor) {}

    @Override
    public RegValue copy() {
        return null;
    }
}

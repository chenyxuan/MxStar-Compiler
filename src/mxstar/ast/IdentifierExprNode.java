package mxstar.ast;

import mxstar.scope.Entity;
import mxstar.utility.Location;

public class IdentifierExprNode extends EntityExprNode {
    private String ID;
    public IdentifierExprNode(String ID, Location location) {
        this.ID = ID;
        this.location = location;
    }

    public String getID() {
        return ID;
    }

    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

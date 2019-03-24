package mxstar.backend;

import mxstar.ir.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StaticDataProcessor {
    private IRRoot ir;

    public StaticDataProcessor(IRRoot ir) {
        this.ir = ir;
    }

    private class StaticDataInfo {
    }

    private Map<IRFunction, StaticDataInfo> infoMap = new HashMap<>();


    private void staticDataProcess(IRFunction irFunction) {
        StaticDataInfo info = new StaticDataInfo();
        infoMap.put(irFunction, info);
    }

    public void run() {
        for(IRFunction irFunction : ir.getFunctionList()) {
            staticDataProcess(irFunction);
        }
    }
}

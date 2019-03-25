package com.g8A.CPEN431.A11.server.distribution;

import com.g8A.CPEN431.A11.protocol.Util;
import com.google.protobuf.ByteString;

public class VirtualNode implements Node {

    private final ByteString pNode;
    private final int pNodeId;
    private final int vIndex;

    public VirtualNode(ByteString pNode, int pNodeId, int vIndex) {
        this.pNode = pNode;
        this.pNodeId = pNodeId;
        this.vIndex = vIndex;
    }

    public byte[] getKey() {
        return getKey(pNode.toByteArray(), vIndex);
    }

    public ByteString getPNode() {
        return this.pNode;
    }

    public int getPNodeId() {
        return this.pNodeId;
    }

    public boolean isVirtualNodeOf(ByteString physicalNode) {
        return physicalNode.equals(this.pNode);
    }
    
    public static byte[] getKey(byte[] pNodeBytes, int vIndex) {
        byte[] buf = new byte[pNodeBytes.length + 8];
        System.arraycopy(pNodeBytes, 0, buf, 0, pNodeBytes.length);
        Util.longToBytes(vIndex, buf, pNodeBytes.length);
        return buf;
    }
}
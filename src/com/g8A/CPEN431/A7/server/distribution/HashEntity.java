package com.g8A.CPEN431.A7.server.distribution;

import com.google.protobuf.ByteString;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Given the key in the key/value request, this class applies
 * a secure hash function to the key and uses the return value
 * to map to a location on a circle, which is then used to find
 * the destination node.
 * key -> secure hash -> node id from circle
 *
 */
public class HashEntity {
    private int numPNodes = 0;
    private final SortedMap<Long, VirtualNode> ring = new TreeMap<>();
    private static HashEntity mHashEntity;
    private static HashFunction hashFunction;
    private static class HashFunction {
        MessageDigest instance;
        public HashFunction() {
            try {
                instance = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        /**
         * Maps a SHA256 hash of the entry byte array to a value on the hash circle (0...2^32-1)
         * @param entry the byte array to be hashed
         * @return long in the range of the hash circle
         */
        public long hash(byte[] entry) {
            instance.reset();
            instance.update(entry);
            byte[] hash = instance.digest();
            return ((long) ByteBuffer.wrap(hash).getInt() & 0xffffffffL);
        }
    }

    private HashEntity() {
        this.hashFunction = new HashFunction();
    }

    /**
     * Gets the node id of the physical node that should store the given key.
     * @param key the key used in the key/value store.
     * @return the unique physical node id.
     */
    public int getKVNodeId(ByteString key) {
        if(ring.isEmpty()) return -1;

        long hash = hashFunction.hash(key.toByteArray());
        if(!ring.containsKey(hash)) {
            SortedMap<Long, VirtualNode> tailMap = ring.tailMap(hash);
            hash = tailMap.isEmpty() ?
                    ring.firstKey() : tailMap.firstKey();
        }

        return ring.get(hash).getPNodeId();
    }

    /**
     * Adds the node and its replicas of virtual nodes to the ring
     * @param pNode the ByteString representing hostname+port of the node
     * @return the unique physical node id
     */
    public synchronized int addNode(ByteString pNode, int numVNodes) {
        int pNodeId = numPNodes;
        for(int i=0; i<numVNodes; i++) {
            VirtualNode vNode = new VirtualNode(pNode, pNodeId, i);
            long hash = hashFunction.hash(vNode.getKey());

            ring.put(hash, vNode);
        }

        numPNodes++;
        return pNodeId;
    }

    /**
     * Removes the node and its replicas of virtual nodes from the ring
     * @param pNode the node id representing the physical node that should be removed
     */
    public synchronized void removeNode(ByteString pNode) {
        Iterator<Long> it = ring.keySet().iterator();
        while (it.hasNext()) {
            long hash = it.next();
            VirtualNode vNode = ring.get(hash);
            if (vNode.isVirtualNodeOf(pNode)) {
                ring.remove(hash);
            }
        }
    }
    
    public static synchronized HashEntity getInstance() {
        if (mHashEntity == null) {
        	mHashEntity = new HashEntity();
        }
        return mHashEntity;
    }
}

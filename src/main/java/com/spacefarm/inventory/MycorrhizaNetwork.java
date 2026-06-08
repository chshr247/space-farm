package com.spacefarm.inventory;

public class MycorrhizaNetwork extends Item {
    public MycorrhizaNetwork() {
        super("Mycorrhiza Net", "Tree phase 3 item. Shop price: $2000");
    }
    @Override
    public ItemType getType() { return ItemType.MYCORRHIZA_NETWORK; }
}

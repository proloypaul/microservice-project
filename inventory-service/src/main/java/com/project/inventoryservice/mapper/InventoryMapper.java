package com.project.inventoryservice.mapper;
import com.project.inventoryservice.model.Inventory;
import inventory.InventoryRequest;

public class InventoryMapper {

    public static Inventory toModel(InventoryRequest inventoryRequest){
        Inventory inventory = new Inventory();

        inventory.setSkuCode(inventoryRequest.getSkuCode());
        inventory.setQuantity(inventoryRequest.getQuantity());

        return inventory;
    }
}

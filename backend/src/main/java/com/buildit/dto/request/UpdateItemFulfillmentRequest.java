package com.buildit.dto.request;

import com.buildit.enums.ItemFulfillmentStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateItemFulfillmentRequest {
    @NotNull
    private ItemFulfillmentStatus status;

    public ItemFulfillmentStatus getStatus() { return status; }
    public void setStatus(ItemFulfillmentStatus status) { this.status = status; }
}

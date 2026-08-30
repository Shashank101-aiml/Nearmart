package com.buildit.dto.response;

import java.util.List;

public class CartResponse {
    private Long cartId;
    private List<CartItemResponse> items;
    private Double total;

    public CartResponse(Long cartId, List<CartItemResponse> items, Double total) {
        this.cartId = cartId;
        this.items = items;
        this.total = total;
    }

    public Long getCartId() { return cartId; }
    public void setCartId(Long cartId) { this.cartId = cartId; }
    public List<CartItemResponse> getItems() { return items; }
    public void setItems(List<CartItemResponse> items) { this.items = items; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
}

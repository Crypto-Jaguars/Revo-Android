import { createSlice, PayloadAction } from "@reduxjs/toolkit";

interface Order {
  id: string;
  items: string[];
  totalAmount: number;
  status: "pending" | "shipped" | "delivered";
}

interface OrderState {
  orders: Order[];
}

const initialState: OrderState = {
  orders: [],
};

const orderSlice = createSlice({
  name: "order",
  initialState,
  reducers: {
    addOrder: (state, action: PayloadAction<Order>) => {
      state.orders.push(action.payload);
    },
    updateOrderStatus: (
      state,
      action: PayloadAction<{ id: string; status: "shipped" | "delivered" }>
    ) => {
      const order = state.orders.find((order) => order.id === action.payload.id);
      if (order) {
        order.status = action.payload.status;
      }
    },
    removeOrder: (state, action: PayloadAction<string>) => {
      state.orders = state.orders.filter((order) => order.id !== action.payload);
    },
  },
});

export const { addOrder, updateOrderStatus, removeOrder } = orderSlice.actions;
export default orderSlice.reducer;

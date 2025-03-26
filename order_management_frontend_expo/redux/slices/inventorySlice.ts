import { createSlice, PayloadAction } from "@reduxjs/toolkit";

interface InventoryItem {
  id: string;
  name: string;
  quantity: number;
}

interface InventoryState {
  items: InventoryItem[];
}

const initialState: InventoryState = {
  items: [],
};

const inventorySlice = createSlice({
  name: "inventory",
  initialState,
  reducers: {
    addItem: (state, action: PayloadAction<InventoryItem>) => {
      state.items.push(action.payload);
    },
    updateItemQuantity: (
      state,
      action: PayloadAction<{ id: string; quantity: number }>
    ) => {
      const item = state.items.find((item) => item.id === action.payload.id);
      if (item) {
        item.quantity = action.payload.quantity;
      }
    },
    removeItem: (state, action: PayloadAction<string>) => {
      state.items = state.items.filter((item) => item.id !== action.payload);
    },
  },
});

export const { addItem, updateItemQuantity, removeItem } = inventorySlice.actions;
export default inventorySlice.reducer;

import { configureStore } from "@reduxjs/toolkit";
import authReducer from "./features/authSlice";
import chatReducer from "./slices/ChatSlice";
import escrowReducer from "./slices/escrowSlice";
import inventoryReducer from "./slices/inventorySlice";
import orderReducer from "./slices/orderSlice";

export const store = configureStore({
  reducer: {
    auth: authReducer,
    chat: chatReducer,
    escrow: escrowReducer,
    inventory: inventoryReducer,
    order: orderReducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

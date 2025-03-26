import { createSlice, PayloadAction } from "@reduxjs/toolkit";

interface EscrowTransaction {
  id: string;
  amount: number;
  status: "pending" | "released" | "disputed";
}

interface EscrowState {
  transactions: EscrowTransaction[];
}

const initialState: EscrowState = {
  transactions: [],
};

const escrowSlice = createSlice({
  name: "escrow",
  initialState,
  reducers: {
    addEscrowTransaction: (state, action: PayloadAction<EscrowTransaction>) => {
      state.transactions.push(action.payload);
    },
    updateEscrowStatus: (
      state,
      action: PayloadAction<{ id: string; status: "released" | "disputed" }>
    ) => {
      const transaction = state.transactions.find((t) => t.id === action.payload.id);
      if (transaction) {
        transaction.status = action.payload.status;
      }
    },
  },
});

export const { addEscrowTransaction, updateEscrowStatus } = escrowSlice.actions;
export default escrowSlice.reducer;

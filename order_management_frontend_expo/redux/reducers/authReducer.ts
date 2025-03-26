import { setAuthentication, logout } from "../features/authSlice";

const initialState = {
  isAuthenticated: false,
  user: null,
  token: null,
};

export default function authReducer(state = initialState, action) {
  switch (action.type) {
    case setAuthentication.type:
      return {
        ...state,
        isAuthenticated: action.payload.isAuthenticated,
        user: action.payload.user || null,
        token: action.payload.token || null,
      };
    case logout.type:
      return initialState;
    default:
      return state;
  }
}

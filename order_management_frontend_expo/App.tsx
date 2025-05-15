// App.tsx
import React, { useEffect } from "react";
import { SafeAreaView, ActivityIndicator, View } from "react-native";
import AsyncStorage from "@react-native-async-storage/async-storage";
import { NavigationContainer } from "@react-navigation/native";
import { createStackNavigator } from "@react-navigation/stack";
import { Provider, useDispatch, useSelector } from "react-redux";
import { store } from "./redux/store";
import { setAuthentication } from "./redux/features/authSlice";
import { RootState } from "./redux/store";
import { globalStyles } from "./styles";
import axios from "axios";

// Screens
import LoginScreen from "./screens/LoginScreens";
import SignUpScreen from "./screens/SignupScreen";

import FarmerDashboardScreen from "./screens/Farmers/FarmerDashboardScreen";
import InventoryScreen from "./screens/Farmers/InventoryScreen";
import OrdersToProcessScreen from "./screens/Farmers/OrdersToProcessScreen";
import PackingScreen from "./screens/Farmers/PackingScreen";
import ShippingScreen from "./screens/Farmers/ShippingScreen";
import EscrowReleaseScreen from "./screens/Farmers/EscrowReleaseScreen";
import UserProfileScreen from "./screens/Farmers/UserProfileScreen";
import OrderTrackingScreen from "./screens/Farmers/OrderTrackingScreen";

import BuyerDashboardScreen from "./screens/Buyers/BuyerDashboardScreen";
import ProfileScreen from "./screens/Buyers/ProfileScreen";
import OrderListScreen from "./screens/Buyers/OrderListScreen";
import OrderPlacementScreen from "./screens/Buyers/OrderPlacementScreen";
import OrdersDetailScreen from "./screens/Buyers/OrdersDetailsScreen";
import PlaceOrderScreen from "./screens/Buyers/PlaceOrderScreen";
import ViewOrdersScreen from "./screens/Buyers/ViewOrdersScreen";
import OrderTrackingScreenB from "./screens/Buyers/OrderTrackingScreen";
import EscrowPaymentScreen from "./screens/Buyers/EscrowPaymentScreen";

import NotificationSystem from "./screens/NotificationsSystem";
import ChatListScreen from "./screens/Chat/ChatListScreen";
import ChatDetailScreen from "./screens/Chat/ChatDetailScreen";

axios.defaults.baseURL = "http://127.0.0.1:8000/api";
axios.defaults.headers.post["Content-Type"] = "application/json";
axios.defaults.withCredentials = true;

const Stack = createStackNavigator();

const MainApp = () => {
  const dispatch = useDispatch();
  const isAuthenticated = useSelector((state: RootState) => state.auth.isAuthenticated);

  useEffect(() => {
    const checkAuth = async () => {
      const token = await AsyncStorage.getItem("userToken");
      dispatch(setAuthentication(!!token));
    };
    checkAuth();
  }, [dispatch]);

  if (isAuthenticated === null) {
    return (
      <View style={globalStyles.container}>
        <ActivityIndicator size="large" color="#FFFFFF" />
      </View>
    );
  }

  return (
    <NavigationContainer>
      <SafeAreaView style={{ flex: 1 }}>
        <Stack.Navigator initialRouteName={isAuthenticated ? "FarmerDashboard" : "Login"} screenOptions={{ headerShown: false }}>
          <Stack.Screen name="Login" component={LoginScreen} />
          <Stack.Screen name="SignUp" component={SignUpScreen} />
          <Stack.Screen name="FarmerDashboard" component={FarmerDashboardScreen} />
          <Stack.Screen name="Inventory" component={InventoryScreen} />
          <Stack.Screen name="OrdersToProcess" component={OrdersToProcessScreen} />
          <Stack.Screen name="Packing" component={PackingScreen} />
          <Stack.Screen name="Shipping" component={ShippingScreen} />
          <Stack.Screen name="UserProfile" component={UserProfileScreen} />
          <Stack.Screen name="OrderTracking" component={OrderTrackingScreen} />
          <Stack.Screen name="EscrowRelease" component={EscrowReleaseScreen} />
          <Stack.Screen name="BuyerDashboard" component={BuyerDashboardScreen} />
          <Stack.Screen name="Profile" component={ProfileScreen} />
          <Stack.Screen name="OrderList" component={OrderListScreen} />
          <Stack.Screen name="OrderPlacement" component={OrderPlacementScreen} />
          <Stack.Screen name="OrdersDetail" component={OrdersDetailScreen} />
          <Stack.Screen name="PlaceOrder" component={PlaceOrderScreen} />
          <Stack.Screen name="ViewOrders" component={ViewOrdersScreen} />
          <Stack.Screen name="OrderTrackingBuyer" component={OrderTrackingScreenB} />
          <Stack.Screen name="EscrowPayment" component={EscrowPaymentScreen} />
          <Stack.Screen name="Notifications" component={NotificationSystem} />
          <Stack.Screen name="ChatList" component={ChatListScreen} />
          <Stack.Screen name="ChatDetail" component={ChatDetailScreen} />
        </Stack.Navigator>
      </SafeAreaView>
    </NavigationContainer>
  );
};

const App = () => (
  <Provider store={store}>
    <MainApp />
  </Provider>
);

export default App;

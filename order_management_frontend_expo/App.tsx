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


import BuyerDashboardScreen from "./screens/Buyers/BuyerDashboardScreen";
import ProfileScreen from "./screens/Buyers/ProfileScreen";
import OrderListScreen from "./screens/Buyers/OrderListScreen";
import OrderPlacementScreen from "./screens/Buyers/OrderPlacementScreen";
import OrdersDetailScreen from "./screens/Buyers/OrdersDetailsScreen";
import PlaceOrderScreen from "./screens/Buyers/PlaceOrderScreen";
import ViewOrdersScreen from "./screens/Buyers/ViewOrdersScreen";
import OrderTrackingScreen from "./screens/Buyers/OrderTrackingScreen";
import EscrowPaymentScreen from "./screens/Buyers/EscrowPaymentScreen";

import FarmerDashboardScreen from "./screens/Farmers/FarmerDashboardScreen";
import InventoryScreen from "./screens/Farmers/InventoryScreen";
import OrdersToProcessScreen from "./screens/Farmers/OrdersToProcessScreen";
import PackingScreen from "./screens/Farmers/PackingScreen";
import ShippingScreen from "./screens/Farmers/ShippingScreen";
import UserProfileScreen from "./screens/Farmers/UserProfileScreen";
import FarmerOrderTrackingScreen from "./screens/Farmers/OrderTrackingScreen";
import EscrowReleaseScreen from "./screens/Farmers/EscrowReleaseScreen";

import LoginScreen from "./screens/LoginScreens";
import SignUpScreen from "./screens/SignupScreen";

import ChatScreen from "./screens/Farmers/ChatScreen"; 
import NotificationSystem from "./screens/NotificationsSystem";

import AppNavigator from "./navigation/AppNavigator";

axios.defaults.baseURL = "http://your-backend-ip:8000/api";
axios.defaults.headers.post["Content-Type"] = "application/json";
axios.defaults.withCredentials = true;

const Stack = createStackNavigator();

const MainApp: React.FC = () => {
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
        <Stack.Navigator initialRouteName={isAuthenticated ? "FarmerDashboard" : "Login"}>
          
          <Stack.Screen name="Login" component={LoginScreen} options={{ headerShown: false }} />
          <Stack.Screen name="SignUp" component={SignUpScreen} options={{ headerShown: false }} />

          <Stack.Screen name="ChatScreen" component={ChatScreen} options={{ title: "Chat" }} />
          <Stack.Screen name="Notifications" component={NotificationSystem} options={{ title: "Notifications" }} />

          <Stack.Screen name="FarmerDashboard" component={FarmerDashboardScreen} options={{ headerShown: false }} />
          <Stack.Screen name="InventoryScreen" component={InventoryScreen} />
          <Stack.Screen name="OrdersToProcessScreen" component={OrdersToProcessScreen} />
          <Stack.Screen name="PackingScreen" component={PackingScreen} />
          <Stack.Screen name="ShippingScreen" component={ShippingScreen} />
          <Stack.Screen name="UserProfileScreen" component={UserProfileScreen} options={{ title: "UserProfileScreen" }} />
          <Stack.Screen name="FarmerOrderTracking" component={FarmerOrderTrackingScreen} />
          <Stack.Screen name="EscrowReleaseScreen" component={EscrowReleaseScreen} />

          <Stack.Screen name="BuyerDashboard" component={BuyerDashboardScreen} options={{ headerShown: false }} />
          <Stack.Screen name="Profile" component={ProfileScreen} options={{ title: "Profile" }} />
          <Stack.Screen name="OrderList" component={OrderListScreen} />
          <Stack.Screen name="OrderPlacement" component={OrderPlacementScreen} />
          <Stack.Screen name="OrdersDetail" component={OrdersDetailScreen} />
          <Stack.Screen name="PlaceOrder" component={PlaceOrderScreen} />
          <Stack.Screen name="ViewOrders" component={ViewOrdersScreen} />
          <Stack.Screen name="OrderTracking" component={OrderTrackingScreen} />
          <Stack.Screen name="EscrowPayment" component={EscrowPaymentScreen} options={{ title: "EscrowPaymentScreen" }} />
          
          <Stack.Screen name="AppNavigator" component={AppNavigator} options={{ headerShown: false }} />
        </Stack.Navigator>
      </SafeAreaView>
    </NavigationContainer>
  );
};

const App: React.FC = () => (
  <Provider store={store}>
    <MainApp />
  </Provider>
);

export default App;

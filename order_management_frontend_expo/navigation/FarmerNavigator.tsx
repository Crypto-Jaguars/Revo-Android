import React from "react";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import FarmerDashboard from "../screens/Farmers/FarmerDashboardScreen";
import InventoryScreen from "../screens/Farmers/InventoryScreen";
import OrdersToProcessScreen from "../screens/Farmers/OrdersToProcessScreen";
import PackingScreen from "../screens/Farmers/PackingScreen";
import ShippingScreen from "../screens/Farmers/ShippingScreen";
import EscrowReleaseScreen from "../screens/Farmers/EscrowReleaseScreen";
import ChatScreen from "../screens/ChatScreen";
const Stack = createNativeStackNavigator();

const FarmerNavigator = () => {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="FarmerDashboard" component={FarmerDashboard} />
      <Stack.Screen name="Inventory" component={InventoryScreen} />
      <Stack.Screen name="OrdersToProcess" component={OrdersToProcessScreen} />
      <Stack.Screen name="Packing" component={PackingScreen} />
      <Stack.Screen name="Shipping" component={ShippingScreen} />
      <Stack.Screen name="EscrowRelease" component={EscrowReleaseScreen} />
      <Stack.Screen name="Chat" component={ChatScreen} />

    </Stack.Navigator>
  );
};

export default FarmerNavigator;

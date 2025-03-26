import React from "react";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import BuyerDashboard from "../screens/Buyers/BuyerDashboardScreen";
import ProfileScreen from "../screens/Buyers/ProfileScreen";
import OrderListScreen from "../screens/Buyers/OrderListScreen";
import OrderDetailScreen from "../screens/Buyers/OrdersDetailsScreen";
import OrderPlacementScreen from "../screens/Buyers/OrderPlacementScreen";
import PlaceOrderScreen from "../screens/Buyers/PlaceOrderScreen";
import EscrowPaymentScreen from "../screens/Buyers/EscrowPaymentScreen";
import ChatScreen from "../screens/ChatScreen";


const Stack = createNativeStackNavigator();

const BuyerNavigator = () => {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="BuyerDashboard" component={BuyerDashboard} />
      <Stack.Screen name="Profile" component={ProfileScreen} />
      <Stack.Screen name="OrderList" component={OrderListScreen} />
      <Stack.Screen name="OrderDetail" component={OrderDetailScreen} />
      <Stack.Screen name="OrderPlacement" component={OrderPlacementScreen} />
      <Stack.Screen name="PlaceOrder" component={PlaceOrderScreen} />
      <Stack.Screen name="EscrowPayment" component={EscrowPaymentScreen} />
      <Stack.Screen name="Chat" component={ChatScreen} />

    </Stack.Navigator>
  );
};

export default BuyerNavigator;

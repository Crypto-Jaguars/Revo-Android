import React from "react";
import { createStackNavigator } from "@react-navigation/stack";
import LoginScreen from "../screens/LoginScreens";
import SignupScreen from "../screens/SignupScreen";
import BuyerNavigator from "./BuyerNavigator";
import FarmerNavigator from "./FarmerNavigator";

const Stack = createStackNavigator();

const AppNavigator = () => {
  return (
    <Stack.Navigator initialRouteName="Login" screenOptions={{ headerShown: false }}>
      <Stack.Screen name="Login" component={LoginScreen} />
      <Stack.Screen name="SignUp" component={SignupScreen} />
      <Stack.Screen name="BuyerDashboard" component={BuyerNavigator} />
      <Stack.Screen name="FarmerDashboard" component={FarmerNavigator} />
    </Stack.Navigator>
  );
};

export default AppNavigator;

import React, { useEffect, useState } from "react";
import { 
  View, Text, Button, StyleSheet, 
  ImageBackground, ActivityIndicator 
} from "react-native";
import { useNavigation } from "@react-navigation/native";
import type { StackNavigationProp } from "@react-navigation/stack";
import axios from "axios";

// Define types for navigation routes
type BuyerStackParamList = {
  OrderList: undefined;
  Profile: undefined;
  PlaceOrder: undefined;
  Chat: undefined;
  EscrowPaymentScreen: undefined;
  NotificationsSystem: undefined;
};

const BuyerDashboard = () => {
  const navigation = useNavigation<StackNavigationProp<BuyerStackParamList>>();
  const [buyerData, setBuyerData] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  const backgroundImage = {
    uri: "https://images.pexels.com/photos/440731/pexels-photo-440731.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
  };

  // Fetch buyer dashboard data
  useEffect(() => {
    axios.get("http://127.0.0.1:8000/api/buyer/dashboard/") // Use your correct API endpoint
      .then((response) => {
        setBuyerData(response.data);
        setLoading(false);
      })
      .catch((error) => {
        console.error("Error fetching buyer data:", error);
        setLoading(false);
      });
  }, []);

  return (
    <ImageBackground source={backgroundImage} style={styles.background}>
      <View style={styles.overlay}>
        <Text style={styles.title}>Buyer Dashboard</Text>

        {/* Show loading indicator while fetching data */}
        {loading ? (
          <ActivityIndicator size="large" color="#00ff00" />
        ) : (
          <Text style={styles.info}>Welcome, {buyerData?.name || "Buyer"}!</Text>
        )}

        <View style={styles.buttonContainer}>
          <Button title="View Orders" color="green" onPress={() => navigation.navigate("OrderList")} />
        </View>

        <View style={styles.buttonContainer}>
          <Button title="Profile" color="green" onPress={() => navigation.navigate("Profile")} />
        </View>

        <View style={styles.buttonContainer}>
          <Button title="Place Order" color="green" onPress={() => navigation.navigate("PlaceOrder")} />
        </View>

        <View style={styles.buttonContainer}>
          <Button title="Chat" color="green" onPress={() => navigation.navigate("Chat")} />
        </View>

        <View style={styles.buttonContainer}>
          <Button title="Escrow Payment" onPress={() => navigation.navigate("EscrowPaymentScreen")} />
        </View>

        <View style={styles.buttonContainer}>
          <Button title="View Notifications" onPress={() => navigation.navigate("NotificationsSystem")} />
        </View>
      </View>
    </ImageBackground>
  );
};

// Styles
const styles = StyleSheet.create({
  background: {
    flex: 1,
    resizeMode: "cover",
    justifyContent: "flex-start",
  },
  overlay: {
    flex: 1,
    alignItems: "center",
    paddingTop: 50, // Adjust to move content higher
    backgroundColor: "rgba(0, 0, 0, 0.5)", // Dark overlay for readability
  },
  title: {
    fontSize: 24,
    fontWeight: "bold",
    color: "white",
    marginBottom: 20,
  },
  info: {
    fontSize: 18,
    color: "white",
    marginBottom: 15,
  },
  buttonContainer: {
    width: "80%",
    marginVertical: 10,
  },
});

export default BuyerDashboard;

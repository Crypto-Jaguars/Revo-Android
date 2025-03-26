import React, { useEffect, useState } from "react";
import { View, Text, Button, StyleSheet, ImageBackground, ActivityIndicator } from "react-native";
import { useNavigation } from "@react-navigation/native";
import axios from "axios";

const FarmerDashboard = () => {
  const navigation = useNavigation();
  const [farmerData, setFarmerData] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  const backgroundImage = {
    uri: "https://images.pexels.com/photos/440731/pexels-photo-440731.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
  };

  // Fetch farmer dashboard data
  useEffect(() => {
    axios.get("http://127.0.0.1:8000/api/farmer/dashboard/") // Use your correct API endpoint
      .then((response) => {
        setFarmerData(response.data);
        setLoading(false);
      })
      .catch((error) => {
        console.error("Error fetching farmer data:", error);
        setLoading(false);
      });
  }, []);

  return (
    <ImageBackground source={backgroundImage} style={styles.background}>
      <View style={styles.overlay}>
        <Text style={styles.title}>Farmer Dashboard</Text>

        {/* Show loading indicator while fetching data */}
        {loading ? (
          <ActivityIndicator size="large" color="orange" />
        ) : (
          <Text style={styles.info}>Welcome, {farmerData?.name || "Farmer"}!</Text>
        )}

        <View style={styles.buttonContainer}>
          <Button title="Profile" color="purple" onPress={() => navigation.navigate("ProfileScreen")} />
        </View>

        <View style={styles.buttonContainer}>
          <Button title="Inventory" color="green" onPress={() => navigation.navigate("InventoryScreen")} />
        </View>

        <View style={styles.buttonContainer}>
          <Button title="Orders To Process" color="green" onPress={() => navigation.navigate("OrdersToProcessScreen")} />
        </View>

        <View style={styles.buttonContainer}>
          <Button title="Packing" color="green" onPress={() => navigation.navigate("PackingScreen")} />
        </View>

        <View style={styles.buttonContainer}>
          <Button title="Shipping" color="green" onPress={() => navigation.navigate("ShippingScreen")} />
        </View>

        <View style={styles.buttonContainer}>
          <Button title="Chat with Buyers" color="blue" onPress={() => navigation.navigate("ChatScreen")} />
        </View>

        <View style={styles.buttonContainer}>
          <Button title="Escrow Release" color="blue" onPress={() => navigation.navigate("EscrowReleaseScreen")} />
        </View>

        <View style={styles.buttonContainer}>
          <Button title="View Notifications" color="orange" onPress={() => navigation.navigate("NotificationsSystem")} />
        </View>
      </View>
    </ImageBackground>
  );
};

const styles = StyleSheet.create({
  background: {
    flex: 1,
    resizeMode: "cover",
    justifyContent: "flex-start",
  },
  overlay: {
    flex: 1,
    alignItems: "center",
    paddingTop: 50, // Moves content to the top
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

export default FarmerDashboard;

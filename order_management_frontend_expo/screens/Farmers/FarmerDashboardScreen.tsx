import React, { useEffect, useState } from "react";
import {
  View,
  Text,
  Button,
  StyleSheet,
  ImageBackground,
  ActivityIndicator,
  Alert,
} from "react-native";
import { useNavigation } from "@react-navigation/native";
import axios from "axios";
import AsyncStorage from "@react-native-async-storage/async-storage";

const FarmerDashboardScreen = () => {
  const navigation = useNavigation<any>();
  const [farmerData, setFarmerData] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  const backgroundImage = {
    uri: "https://images.pexels.com/photos/440731/pexels-photo-440731.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1",
  };

  useEffect(() => {
    const fetchFarmerData = async () => {
      try {
        const token = await AsyncStorage.getItem("token");
        if (!token) {
          Alert.alert("Authentication Error", "No token found. Please log in again.");
          return;
        }

        const response = await axios.get("http://your-backend-ip:8000/api/farmer/dashboard/", {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        setFarmerData(response.data);
      } catch (error) {
        console.error("Error fetching farmer data:", error);
        Alert.alert("Error", "Failed to load farmer dashboard data.");
      } finally {
        setLoading(false);
      }
    };

    fetchFarmerData();
  }, []);

  return (
    <ImageBackground source={backgroundImage} style={styles.background}>
      <View style={styles.overlay}>
        <Text style={styles.title}>Farmer Dashboard</Text>

        {loading ? (
          <ActivityIndicator size="large" color="orange" />
        ) : (
          <Text style={styles.info}>
            Welcome, {farmerData?.name || "Farmer"}!
          </Text>
        )}

        <View style={styles.buttonContainer}>
          <Button title="Profile" color="purple" onPress={() => navigation.navigate("Profile")} />
        </View>

        <View style={styles.buttonContainer}>
          <Button title="Inventory" color="green" onPress={() => navigation.navigate("Inventory")} />
        </View>

        <View style={styles.buttonContainer}>
          <Button
            title="Orders To Process"
            color="green"
            onPress={() => navigation.navigate("OrdersToProcess")}
          />
        </View>

        <View style={styles.buttonContainer}>
          <Button title="Packing" color="green" onPress={() => navigation.navigate("Packing")} />
        </View>

        <View style={styles.buttonContainer}>
          <Button title="Shipping" color="green" onPress={() => navigation.navigate("Shipping")} />
        </View>

        <View style={styles.buttonContainer}>
          <Button title="Chat with Buyers" color="blue" onPress={() => navigation.navigate("Chat")} />
        </View>

        <View style={styles.buttonContainer}>
          <Button
            title="Escrow Release"
            color="blue"
            onPress={() => navigation.navigate("EscrowRelease")}
          />
        </View>

        {/* Optional: Add Notifications later if the screen exists */}
        {/* <View style={styles.buttonContainer}>
          <Button
            title="View Notifications"
            color="orange"
            onPress={() => navigation.navigate("NotificationsSystem")}
          />
        </View> */}
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
    paddingTop: 50,
    backgroundColor: "rgba(0, 0, 0, 0.5)",
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

export default FarmerDashboardScreen;

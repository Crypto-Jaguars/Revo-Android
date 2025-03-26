import React, { useState } from "react";
import { View, Text, FlatList, StyleSheet, Button } from "react-native";

const FarmerOrderTrackingScreen = () => {
  const [orders, setOrders] = useState([
    { id: "1", buyer: "John Doe", product: "Tomatoes", status: "New Order" },
    { id: "2", buyer: "Jane Smith", product: "Rice", status: "Packing" },
  ]);

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Orders to Process</Text>
      <FlatList
        data={orders}
        keyExtractor={(item) => item.id}
        renderItem={({ item }) => (
          <View style={styles.orderCard}>
            <Text>Buyer: {item.buyer}</Text>
            <Text>Product: {item.product}</Text>
            <Text>Status: {item.status}</Text>
          </View>
        )}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, padding: 20 },
  title: { fontSize: 24, fontWeight: "bold", marginBottom: 20 },
  orderCard: { padding: 15, marginVertical: 10, borderWidth: 1, borderRadius: 5 },
});

export default FarmerOrderTrackingScreen;

import React, { useState } from "react";
import { View, Text, FlatList, StyleSheet, Button } from "react-native";

const OrderTrackingScreen = () => {
  const [orders, setOrders] = useState([
    { id: "1", product: "Tomatoes", status: "Processing" },
    { id: "2", product: "Rice", status: "Shipped" },
    { id: "3", product: "Corn", status: "Delivered" },
  ]);

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Your Orders</Text>
      <FlatList
        data={orders}
        keyExtractor={(item) => item.id}
        renderItem={({ item }) => (
          <View style={styles.orderCard}>
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

export default OrderTrackingScreen;

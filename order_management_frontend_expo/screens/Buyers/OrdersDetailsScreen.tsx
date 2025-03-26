import React from "react";
import { View, Text, StyleSheet } from "react-native";
import { useRoute } from "@react-navigation/native";

const OrdersDetailScreen = () => {
  const route = useRoute();
  const { orderId } = route.params as { orderId: string };

  return (
    <View style={styles.container}>
      <Text style={styles.header}>Order Details</Text>
      <Text>Order ID: {orderId}</Text>
      <Text>Status: Pending</Text>
      <Text>Total Price: $50</Text>
      <Text>Expected Delivery: 3 days</Text>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, padding: 20 },
  header: { fontSize: 22, fontWeight: "bold", marginBottom: 10 }
});

export default OrdersDetailScreen;
